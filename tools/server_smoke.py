#!/usr/bin/env python3
"""Exercise a real, isolated Fabric server. Requires explicit EULA acceptance flag."""
import argparse
import json
import os
from pathlib import Path
import selectors
import sqlite3
import subprocess
import sys
import tempfile
import time

parser = argparse.ArgumentParser()
parser.add_argument('--accept-eula', action='store_true', required=True)
parser.parse_args()
root = Path(__file__).resolve().parent.parent
reports = root / 'build/server-smoke'
reports.mkdir(parents=True, exist_ok=True)
(reports / 'result.json').unlink(missing_ok=True)

class Server:
    def __init__(self, runtime, label, profile=False):
        self.label = label
        self.log = reports / (label + '.log')
        self.output = ''
        command = [sys.executable, str(root / 'tools/gradle_cloud.py'),
            '-PshirokaiRunDir=' + os.path.relpath(runtime, root)]
        if profile:
            command += ['-PprofileSmoke']
        command += ['runServer', '--args=nogui']
        self.process = subprocess.Popen(command, cwd=root, stdin=subprocess.PIPE,
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        self.selector = selectors.DefaultSelector()
        self.selector.register(self.process.stdout, selectors.EVENT_READ)
    def wait(self, token, offset=0, timeout=150):
        deadline = time.monotonic() + timeout
        while token not in self.output[offset:]:
            if time.monotonic() > deadline:
                raise RuntimeError(f'{self.label}: timeout waiting for {token}; see {self.log}')
            for key, _ in self.selector.select(0.2):
                chunk = os.read(key.fileobj.fileno(), 65536)
                if chunk:
                    self.output += chunk.decode('utf-8', errors='replace')
                    self.log.write_text(self.output)
                else:
                    self.selector.unregister(key.fileobj)
            if self.process.poll() is not None and token not in self.output[offset:]:
                raise RuntimeError(f'{self.label}: exit {self.process.returncode} before {token}; see {self.log}')
    def command(self, command, expected):
        cursor = len(self.output)
        self.process.stdin.write((command + '\n').encode())
        self.process.stdin.flush()
        self.wait(expected, cursor, 30)
    def stop(self):
        if self.process.poll() is None:
            self.command('stop', 'Stopping server')
            self.wait('BUILD SUCCESSFUL', timeout=60)
            self.process.wait(timeout=20)
        if self.process.returncode != 0:
            raise RuntimeError(f'{self.label}: server did not stop cleanly')
        self.selector.close()

checks = []
with tempfile.TemporaryDirectory(prefix='runtime-', dir=reports) as tmp:
    runtime = Path(tmp)
    (runtime / 'eula.txt').write_text('eula=true\n')
    (runtime / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nview-distance=2\nsimulation-distance=2\nmax-tick-time=60000\n')
    server = Server(runtime, 'first-boot', profile=True)
    try:
        server.wait('Done (')
        server.command('shirokaicore status', 'SQLite=READY')
        server.command('shirokaicore modules', 'administration=RUNNING')
        server.command('shirokaicore version', 'Shirokai Core 0.3.0-dev')
        server.command('shirokaicore permissions', 'vanilla-op4')
        server.command('shirokaicore placeholders %shirokai:online%/%shirokai:dollars%', '0/N/D')
        server.command('shirokaicore debug', 'SQLite integrity_check=ok')
        checks += ['real Fabric startup', 'module status', 'version', 'permission fallback', 'literal placeholders', 'SQLite integrity']
        server.command('shirokai-profile-smoke', 'PROFILE_GUI_SMOKE_PASS')
        checks.append('native menu server-side interactions with synthetic players')
        subject = '11111111-1111-1111-1111-111111111111'
        server.command('shirokaicore titles list', 'traveler')
        server.command(f'shirokaicore titles grant {subject} traveler', 'grants={traveler=0}')
        server.command(f'shirokaicore titles grant {subject} traveler 120', 'grants={traveler=')
        server.command(f'shirokaicore titles inspect {subject}', 'grants={traveler=')
        server.command(f'shirokaicore titles revoke {subject} traveler', 'grants={}')
        server.command(f'shirokaicore titles grant {subject} traveler', 'grants={traveler=0}')
        server.command('perfil', '/perfil exige um jogador')
        checks += ['title catalog registered', 'permanent and temporary grants', 'revoke persisted', 'profile rejects console']

        config_file = runtime / 'config/shirokai-core/core.json'
        valid = config_file.read_text()
        config = json.loads(valid)
        config['unavailablePlaceholder'] = 'Unavailable'
        customized = json.dumps(config)
        config_file.write_text(customized)
        server.command('shirokaicore reload', 'Configuração recarregada; estados ativos preservados')
        server.command('shirokaicore placeholders %shirokai:dollars%', 'Unavailable')
        config_file.write_text('{broken JSON')
        server.command('shirokaicore reload', 'Shirokai Core:')
        server.command('shirokaicore placeholders %shirokai:dollars%', 'Unavailable')
        config_file.write_text(customized)
        titles_file = runtime / 'config/shirokai-core/titles.json'
        original_titles = titles_file.read_text()
        titles_file.write_text('{broken')
        altered_config = dict(config)
        altered_config['unavailablePlaceholder'] = 'MUST_NOT_APPLY'
        config_file.write_text(json.dumps(altered_config))
        server.command('shirokaicore reload', 'Shirokai Core:')
        server.command('shirokaicore placeholders %shirokai:dollars%', 'Unavailable')
        titles_file.write_text(original_titles)
        config_file.write_text(customized)
        checks.append('invalid titles catalog rejects complete reload atomically')
        config['modules']['storage'] = False
        config_file.write_text(json.dumps(config))
        server.command('shirokaicore reload', 'exige reinício; configuração ativa preservada')
        server.command('shirokaicore status', 'SQLite=READY')
        config_file.write_text(customized)
        server.command('shirokaicore backup', 'Backup criado:')
        checks += ['valid reload', 'invalid reload preserves active state', 'restart-only change rejected', 'consistent backup']
    finally:
        server.stop()
    checks.append('clean shutdown')
    database = runtime / 'shirokai-core/shirokai.db'
    with sqlite3.connect(database) as connection:
        assert connection.execute('PRAGMA integrity_check').fetchone()[0] == 'ok'
        assert connection.execute('PRAGMA user_version').fetchone()[0] == 2
        assert connection.execute('SELECT count(*) FROM admin_audit').fetchone()[0] >= 2
    backups = list((runtime / 'shirokai-core/backups').glob('*.db'))
    assert len(backups) == 1
    with sqlite3.connect(backups[0]) as connection:
        assert connection.execute('PRAGMA integrity_check').fetchone()[0] == 'ok'
    server = Server(runtime, 'restart')
    try:
        server.wait('Done (')
        server.command('shirokaicore status', 'SQLite=READY')
        server.command('shirokaicore placeholders %shirokai:dollars%', 'Unavailable')
        server.command('shirokaicore debug', 'SQLite integrity_check=ok')
        server.command(f'shirokaicore titles inspect {subject}', 'grants={traveler=0}')
        checks.append('title grant survives server restart')
    finally:
        server.stop()
    checks += ['database survives restart', 'custom config survives restart', 'backup independently readable', 'audit persisted']
    with sqlite3.connect(database) as connection:
        connection.execute('PRAGMA user_version=42')
    server = Server(runtime, 'storage-failure')
    try:
        server.wait('Done (')
        server.command('shirokaicore status', 'SQLite=FAILED')
        server.command('shirokaicore placeholders %shirokai:online%', ') 0')
        server.command('shirokaicore debug', 'Banco mais recente que o mod: schema 42')
    finally:
        server.stop()
    with sqlite3.connect(database) as connection:
        assert connection.execute('PRAGMA user_version').fetchone()[0] == 42
        assert connection.execute('SELECT count(*) FROM admin_audit').fetchone()[0] >= 2
    checks += ['storage failure isolated from server and placeholders', 'future schema preserved']

result = {'passed': checks, 'count': len(checks), 'not_tested': ['player connections', 'LuckPerms runtime', 'Cobblemon', 'Impactor', 'visual GUI and packets from a real client', 'TAB/scoreboard rendering', 'clans/chat']}
(reports / 'result.json').write_text(json.dumps(result, indent=2) + '\n')
print(json.dumps(result, indent=2))
