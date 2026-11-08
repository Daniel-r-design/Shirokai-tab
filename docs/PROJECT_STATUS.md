# Estado real do projeto — 0.3.0-dev

## Situação

Infraestrutura da fase2 mantida; núcleo de títulos/identificações e /perfil da fase3
implementado. Projeto completo NÃO entregue. TAB, scoreboard visual, chat, clãs,
menus de clãs e integrações completas permanecem pendentes. Nenhum requisito foi descartado.

## Implementado nesta etapa

- Catálogo titles.json versionado/validado, defaults sem overwrite e reload atômico
  junto com core.json. Categoria ROLE vinculada à permissão; SPECIAL por concessão/default.
- Desbloqueios permanentes/temporários, expiração e estados UNLOCKED/LOCKED/EXPIRED/UNAVAILABLE.
- Uma selected_visual persistente para ambos TAB e scoreboard futuros; nenhum segundo campo.
- No máximo duas identificações totais no chat, incluindo cargos, com ordem de categoria
  e prioridade. Não há ainda renderização de chat; esta etapa fornece seleção e dados.
- Seleção revalida permissões/expiração/superfícies antes de gravar; invalidação no cache
  e limpeza persistente online. Diagnóstico offline não apaga escolhas por permissões não carregadas.
- Cache removido no disconnect; verificação a cada100ticks sem SQL por tick.
- SQLite schema2: perfis, concessões e slots0/1 com unicidade, revisão otimista CAS,
  backup antes de upgrade1→2 e auditoria na mesma transação de grant/revoke.
- /perfil: container vanilla 9x6 com paginação, estados, título único, contador n/2,
  clique esquerdo visual/direito chat, refresh, limpar e voltar ao /menu como jogador.
- Menu bloqueia transferência de ícones, outros usuários, ações inválidas e tela fechada;
  callbacks não reabrem uma tela trocada/fechada.
- Comandos administrativos titles list/inspect/grant/revoke com permissões distintas e UUID.
- Placeholders selected_title/chat_tag_1/chat_tag_2 do serviço nativo.

## Validação final executada

| Verificação | Resultado |
|---|---|
| `python3 tools/gradle_cloud.py build smokeClasses` | PASSOU: compilação Java21, testes e remap do JAR |
| JUnit | 31 executados; 0 falhas; 0 erros; 0 ignorados |
| `python3 tools/server_smoke.py --accept-eula` | 24 verificações funcionais passaram em três boots Fabric reais |
| Menu nativo no servidor | PROFILE_GUI_SMOKE_PASS com jogadores sintéticos e handler real |
| Proteções GUI | quickMove/shift/swap/throw/clone/quick-craft/pickup-all, slots inválidos, outro dono e menu fechado bloqueados |
| Ações GUI | Seleção visual, dois slots, terceira rejeitada, limpar/voltar e inventário preservado |
| Catálogo/concessões | Grant permanente/temporário, inspect, revoke e restart passaram |
| Reload | Catálogo inválido rejeita mudança inteira sem aplicar novo core.json |
| Migração1→2 | Backup legível, prefs preservadas e nenhum backup redundante no próximo boot |
| Concorrência | Snapshot antigo não sobrescreve seleção mais nova; terceira identificação rejeitada |
| Atomicidade | Falha de auditoria reverte grant/revisão |
| Build de produção | ProfileMenu presente; classes do mod de teste ausentes do JAR |
| Shutdown | stop, mundos salvos, serviços STOPPED e Gradle exit0 |
| Cliente real/login online | NÃO EXECUTADOS |
| LuckPerms Fabric runtime | NÃO EXECUTADO; API5.4 compilada e teste de contrato apenas |
| Cobblemon/Impactor/Dex/PAPI | Ainda não implementados/validados |
| GitHub Actions | Workflow atualizado para compilar testes de servidor; não executado remotamente |

Suites: ModuleManagerTest(6), ConfigServiceTest(4), SqliteStorageTest(5),
PlaceholderServiceTest(2), LuckPermsPermissionsTest(1), TitleRulesTest(4),
TitleCatalogTest(2), TitleStorageTest(4), TitleServiceTest(3).

O teste do menu usa ServerPlayerEntity sintético dentro do servidor real e uma conexão
sem transporte; isso valida o handler e persistência, NÃO autenticação, pacotes vindos
de um cliente ou aparência visual. Login online continua pendente por falha observada
na busca de chaves Yggdrasil. Autenticação não foi desativada.

## Artefato e evidências

JAR: `build/libs/shirokai-core-0.3.0-dev.jar`

SHA-256: `a75a86ee5d07b7d794fec4e2753495ef61371b0559b1f381c601acc0e7ede5f9`

Relatórios: `build/reports/tests/test/index.html`, `build/server-smoke/result.json`.
Logs: `build/server-smoke/first-boot.log`, `restart.log`, `storage-failure.log`.
Mod de teste: `src/smoke`, ativado só com -PprofileSmoke no runServer.
Diretórios temporários do smoke são apagados; run de desenvolvimento é preservado.
Nenhum servidor de teste permanece rodando. EULA segue aceita pela autorização anterior.

Artefato de desenvolvimento, NÃO release estável nem projeto completo.

## Limitações restantes da fase3

Gradientes/animações/condições avançadas dependem do motor de estilos futuro;
display é literal. Títulos CLAN são rejeitados até existir pertencimento real validado.
Painel de cargo real/clã/treinador depende das integrações. Eventos LuckPerms específicos
não foram assinados ainda; há revalidação periódica e imediata nas ações/leituras.
A renderização de TAB/scoreboard/chat ainda não existe, embora os dados compartilhados existam.
Fallback atual é ausência de seleção e texto de fallback do consumidor, sem título automático.

## Arquivos alterados/criados nesta etapa

- build.gradle, workflow build.yml: versão0.3, source set smoke separado e compilação CI.
- core/CoreRuntime.java, ShirokaiCore.java, config/CoreConfig.java: módulos, eventos e reload.
- titles/*: catálogo, definições, regras, estado e serviço.
- profiles/*: menu e comandos de jogador.
- administration/*, placeholders/PlaceholderService.java: comandos e dados adicionais.
- storage/SqliteStorage.java: schema2, backups, revisão e transações de títulos.
- src/test/.../titles/*, src/smoke/* e tools/server_smoke.py: testes e runner ampliados.
- README.md, AGENTS.md, docs/*.md, incluindo novo TITLES.md: uso e continuidade.

Os arquivos continuam locais e não commitados (checkout original sem commits).
Não houve push, release, publicação ou alteração do rascunho do ambiente.
Próxima implementação: clãs com transações, permissões e confirmações, conforme NEXT_STEPS.
