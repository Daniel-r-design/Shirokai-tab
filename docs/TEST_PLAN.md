# Plano de testes

## Infraestrutura

JUnit: isolamento de falhas de inicialização, limpeza, módulos desativados,
registro duplicado, módulo inexistente, reinicialização indevida e encerramento inverso.
Build deve compilar, executar testes e remapear JAR. Não confundir build com startup.

## Próximas suítes obrigatórias

- SQLite: migração, rollback, concorrência, integridade e restart.
- Títulos: expiração e perda de permissão; título único para TAB/scoreboard;
  zero/uma/duas identificações e rejeição da terceira, ordem e fallback.
- Clãs: unicidade case-insensitive, criação gratuita, sigla manual, capacidade
  na aceitação, expiração, entrada concorrente, cooldowns independentes,
  redução de limite sem expulsão, transferência aceita e exclusão confirmada.
- Chat: alcance por dimensão/distância, canais, staff, ignore, mute, antispam,
  texto literal sem injeção de markup e conflitos de aliases.
- Servidor real Fabric 1.21.1: boot, shutdown, login/reconexão, mundos, pacotes,
  GUI contra cliques manipulados, reload, LuckPerms e integrações exatas.
- Múltiplos jogadores: tráfego incremental, caches removidos no disconnect,
  ausência de SQLite por tick e latência da thread principal.

Inventário completo de cenários: seção 19 de REQUIREMENTS.md.
Nunca marcar testes não executados como aprovados.


## Suítes adicionadas na 0.2.0-dev

ConfigServiceTest: defaults sem overwrite, JSON/tipos/schema/caminhos inválidos,
preservação em falha, snapshot imutável e distinção hot reload/restart.
SqliteStorageTest: restart/backup, concorrência/drenagem, rejeição de schema futuro,
rollback de migração e cancelamento de resposta sem perder gravação aceita.
LuckPermsPermissionsTest: contrato API5.4, perda de permissão, UNDEFINED e usuário não carregado.
PlaceholderServiceTest: valores reais do contexto, fallback, substituição literal e não recursiva.
ModuleManagerTest: dependências bloqueiam somente consumidores; suites anteriores mantidas.

Smoke real: `python3 tools/server_smoke.py --accept-eula` cria diretório temporário
sob build/server-smoke, limita bind a loopback com porta dinâmica, exerce comandos,
reload válido/inválido/restart-only, backup, auditoria, reinício e schema futuro.
Preserva logs e resultado JSON; não altera configuração/world do run de desenvolvimento.
EULA aceita nesse teste somente com flag explícita. Nenhum login de jogador é simulado.


## Suítes 0.3.0-dev

TitleRulesTest: zero/uma/duas identificações, terceira rejeitada, slots compartilhados
ROLE/SPECIAL, ordem de categorias/prioridade, título único, permissões/expiração/superfícies.
TitleCatalogTest: defaults preservados, validação de schema/tipos/IDs/categoria suportada.
TitleStorageTest: upgrade schema1 com backup, CAS, seleção/revogação/restart e rollback
quando auditoria falha. TitleServiceTest: executor da thread do servidor, perda de
permissão, expiração/limpeza e diagnóstico offline sem apagar escolhas.

ProfileSmoke é mod de teste em src/smoke, ativado apenas por -PprofileSmoke e fora
do JAR de produção. Cria ServerPlayerEntity sintéticos no servidor real, com conexão
sem transporte. Abre ProfileMenu real e testa quickMove, ações não permitidas,
slots inválidos, outro dono, seleção visual, dois slots, terceiro bloqueado, limpeza,
retorno, menu fechado e preservação de itens. Não simula login ou valida renderização cliente.
Runner server_smoke habilita esse mod somente no primeiro boot e exige PASS explícito.
CI compila smokeClasses; execução de servidor é local nesta sessão, não no GitHub.


## Suítes0.4

ClanRepositoryTest: unicidade case-insensitive, sigla manual, cooldowns independentes,
concorrência na última vaga, redução sem expulsão, transferência confirmada, expiração,
roles contra escalada, rollback de auditoria, persistência e upgrade2→3 com backup.
ClanServiceTest: criação com permissão/tempo mínimo, limites VIP dinâmicos, mudança de
líder para limite menor, recuperação de VIP e validação/preservação de configuração.
ClanSmoke em src/smoke: mesmo serviço/repositório no servidor Fabric real com provedor
de permissões de teste. Não testa LuckPerms real nem representa clãs/multiclientes em produção.
