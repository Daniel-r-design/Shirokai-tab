package com.shirokai.core.titles;

import com.shirokai.core.storage.SqliteStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class TitleStorageTest {
    @TempDir Path directory;
    @Test void upgradesSchemaOneWithVerifiedBackupAndPreservesPreferences() throws Exception {
        Path file=directory.resolve("old.db");UUID player=UUID.randomUUID();Class.forName("org.sqlite.JDBC");
        try(var c=DriverManager.getConnection("jdbc:sqlite:"+file);var s=c.createStatement()) {
            s.execute("CREATE TABLE player_preferences(player_uuid TEXT PRIMARY KEY,scoreboard_enabled INTEGER)");
            s.execute("CREATE TABLE admin_audit(id INTEGER PRIMARY KEY,actor_uuid TEXT,action TEXT,created_at TEXT)");
            s.execute("INSERT INTO player_preferences VALUES('"+player+"',0)");s.execute("PRAGMA user_version=1");
        }
        try(var storage=new SqliteStorage(file)) {
            storage.ready().get();assertFalse(storage.preferences(player).get().scoreboardEnabled());
            assertEquals(0,storage.titleState(player).get().revision());assertEquals("ok",storage.integrityCheck().get());
        }
        List<Path> backups;
        try(var paths=Files.list(directory)) { backups=paths.filter(p->p.getFileName().toString().contains(".schema1-")).toList(); }
        assertEquals(1,backups.size());
        try(var c=DriverManager.getConnection("jdbc:sqlite:"+backups.getFirst());var s=c.createStatement();var rows=s.executeQuery("PRAGMA user_version")) {
            assertTrue(rows.next());assertEquals(1,rows.getInt(1));
        }
        try(var storage=new SqliteStorage(file)) { storage.ready().get(); }
        try(var paths=Files.list(directory)) { assertEquals(1,paths.filter(p->p.getFileName().toString().contains(".schema1-")).count()); }
    }
    @Test void concurrentSelectionsCannotOverwriteEachOtherOrAcceptThirdSlot() throws Exception {
        try(var storage=new SqliteStorage(directory.resolve("data.db"))) {
            UUID player=UUID.randomUUID();var initial=storage.titleState(player).get();
            var first=new TitleState(player,initial.revision(),Map.of(),"a",List.of("a","b"));
            var other=new TitleState(player,initial.revision(),Map.of(),"c",List.of("c"));
            storage.saveTitles(first).get();assertThrows(ExecutionException.class,()->storage.saveTitles(other).get());
            assertEquals("a",storage.titleState(player).get().selectedVisual());
            assertThrows(ExecutionException.class,()->storage.saveTitles(new TitleState(player,1,Map.of(),null,List.of("a","b","c"))).get());
        }
    }
    @Test void grantRevokeAndSelectionsPersistAndRevocationClearsOnlyMatchingId() throws Exception {
        Path file=directory.resolve("data.db");UUID player=UUID.randomUUID();
        try(var storage=new SqliteStorage(file)) {
            storage.grantTitle(player,"a",0,null).get();var state=storage.grantTitle(player,"b",1234,null).get();
            storage.saveTitles(new TitleState(player,state.revision(),state.grants(),"a",List.of("a","b"))).get();
        }
        try(var storage=new SqliteStorage(file)) {
            assertEquals("a",storage.titleState(player).get().selectedVisual());
            var state=storage.revokeTitle(player,"a",null).get();assertNull(state.selectedVisual());assertEquals(List.of("b"),state.chatIds());
            assertEquals(Map.of("b",1234L),state.grants());
        }
    }
    @Test void auditFailureRollsBackGrantAndRevisionAtomically() throws Exception {
        Path file=directory.resolve("data.db");UUID player=UUID.randomUUID();
        try(var storage=new SqliteStorage(file)) {
            storage.ready().get();
            try(var c=DriverManager.getConnection("jdbc:sqlite:"+file);var s=c.createStatement()) {
                s.execute("CREATE TRIGGER reject_audit BEFORE INSERT ON admin_audit BEGIN SELECT RAISE(ABORT,'audit failure'); END");
            }
            assertThrows(ExecutionException.class,()->storage.grantTitle(player,"test",0,null).get());
            var state=storage.titleState(player).get();assertEquals(0,state.revision());assertTrue(state.grants().isEmpty());
        }
    }
}
