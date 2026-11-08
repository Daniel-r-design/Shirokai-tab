# Estado real do projeto — 0.7.0-dev

## Situação

Motor de estilos e TAB nativo implementados na fase6; infraestrutura, perfil,
títulos, clãs e chat anteriores preservados. Projeto completo NÃO entregue.
Scoreboard visual, integrações externas completas, estilos em todas as superfícies,
features adicionais de TAB e validação com cliente real continuam pendentes.
SQLite schema5 adiciona animações sem alterar preferência anterior de scoreboard.

## Implementado nesta etapa

- StyleEngine com AST allowlisted, cores Minecraft/HEX, gradiente por ponto de código
  Unicode, estilos aninhados, templates e animações de frames com relógio compartilhado.
- Placeholder é inserido literalmente, sem reanalisar markup ou permitir click/run_command.
  Fontes/expansão/recursão são limitadas; caches LRU de compilação e render.
- styles.json/tab.json validados e recompilados antes de aplicar snapshot atômico.
- Header/footer multilinha e layouts por permissão, grupo primário e dimensão/prioridade.
- Nome de TAB usa título visual único do perfil, nunca todos os títulos desbloqueados.
- Nome por regra e dados atuais de prefixo/sufixo/grupo/peso LuckPerms via API5.4.
- UPDATE_DISPLAY_NAME envia apenas entradas alteradas por viewer; não muda roster,
  ping, profile, gamemode, assinatura de chat ou visibilidade/listed.
- Controle de teams opt-in (defaultfalse), prioridade invertida e mesmo rank compartilhado,
  preservando desempate vanilla por nome. Nenhum team externo é adotado/movido/removido.
- Colisão/replacement e integrantes externos preservados; limpeza de próprios teams
  em SERVER_STOPPING, antes do save. Nome externo/getPlayerListName é respeitado.
- Preferência individual de animação persistente, frame0 até carregar/desativar;
  snapshots/nomes comuns reutilizados e caches limpos no disconnect.
- /tab refresh, /tab animations e /shirokaicore tab com estatísticas reais.
- Accessor Mixin mínimo e version-pinned de entries do packetvanilla, com refmap gerado.
- Migração1/2/3/4→5 com backup consistente; preferência de scoreboard preservada.

## Resultados finais executados

| Verificação | Resultado |
|---|---|
| python3 tools/gradle_cloud.py build smokeClasses | PASSOU: Java21, testes, compilação smoke e remap do JAR |
| JUnit | 73 executados; 0 falhas; 0 erros; 0 ignorados |
| python3 tools/server_smoke.py --accept-eula | 37 verificações funcionais passaram em três boots reais |
| StyleEngineTest | Cores/HEX/Unicode, segurança de placeholder, tags inválidas, frames/freeze/cache/limites passaram |
| TabPlanTest | JSON/condições/prioridade/default/restart e rejeição de animação/template inválido passaram |
| TeamOwnershipSmoke | TEAM_OWNERSHIP_SMOKE_PASS, mantendo3 cenários de ownership reais |
| TabSmoke | TAB_SMOKE_PASS, com pacotes reais capturados em jogadores sintéticos |
| Header/nome | Layout VIP, título escolhido e conteúdo dos componentes conferidos |
| Delta | Frame igual não envia pacotes; mudança de grupo altera só uma entrada |
| Protocol | Só UPDATE_DISPLAY_NAME; latência original preservada e UUID real de origem |
| Teams | Rank atualizado; foreign team preservado e display restaurado uma única vez |
| Animação | Viewer habilitado recebe frame; viewer desabilitado não recebe; preferência persistida |
| Stop | Teams próprios liberados, foreign preservado; mundos salvos e serviços STOPPED |
| Migração4→5 | Backup legível schema4 e preferências independentes após restart |
| Reload | Styles/TAB inválidos preservam snapshot; ownership exige restart |
| JAR/refmap | Accessor/config/refmap presentes; entries→field_12369 mapeado; mod de teste ausente |
| Regressões | Chat, aliases/datapack reload, mute/restart, clãs/menus/títulos e schema futuro passaram |
| Cliente real/LuckPerms Fabric/assinaturas criptográficas | NÃO EXECUTADOS |

62 testes anteriores preservados; adicionados StyleEngineTest(5), TabPlanTest(3),
TabPreferenceTest(2) e metadataLP(1), total11. Três cenários de TeamOwnership foram
transferidos para o servidor Fabric após diagnóstico de bootstrap/IllegalAccessError
do jar nomeado no JUnit cru; não foram desativados ou substituídos por checks triviais.
No servidor real, os mesmos cenários passaram com registries e transformações reais.
O primeiro runner desta versão tinha assert de schema futuro incorreto (52 em vez do
42 injetado); foi corrigido e a rodada final completa passou preservando42.

Smokes usam ServerPlayerEntity sintéticos/conexão sem transporte e providers LP de teste.
Não equivalem a cliente autenticado, renderização real ou teste de carga/multiclientes.

## Artefato e evidências

JAR: `build/libs/shirokai-core-0.7.0-dev.jar`

SHA-256: `1f37125b61bc7ab022323c61a384750d013358d296c8189d1f3e83ad7fc72c68`

JUnit: `build/reports/tests/test/index.html`.
Resultado: `build/server-smoke/result.json`.
Logs: `build/server-smoke/first-boot.log`, `restart.log`, `storage-failure.log`.
Refmap: shirokai-core-refmap.json dentro do JAR, alvo de campo confirmado.
Todos os servidores de teste foram parados com stop; diretórios temporários removidos.
Artefato de desenvolvimento, NÃO release estável.

## Limitações restantes

- Teams podem afetar mecânicas de equipe e são opt-in. Espectadores seguem precedência
  do cliente; visual/ordenação real ainda exigem cliente conectado.
- Mods que enviam nomes/header diretamente por pacotes não são detectáveis universalmente;
  escolher dono do TAB, desabilitar módulo/nomes conforme TAB.md.
- Styles é usado pelo TAB; chat/menus e configurações individuais de títulos/clãs ainda
  aguardam integração. Nada de interpretar texto do jogador como estilo.
- Listed/vanish por viewer, nametags customizados, abaixo do nome e bossbar não implementados.
- Scoreboard visual, saldos/pokédex/ginásios reais e adapters Cobblemon/Impactor/Dex/PAPI pendentes.
- Runtime LuckPerms/contextos, Yggdrasil/login, aparência/carga e assinaturas reais não validados.
  Autenticação não foi desativada para mascarar rede.

## Arquivos desta etapa

styles/*, tab/*, mixin/PlayerListPacketAccessor, mixin JSON/refmapconfig, fabric.mod.json,
CoreRuntime/ShirokaiCore/CoreConfig, PermissionService/LuckPermsPermissions,
PlayerPreferences/SqliteStorage, AdminCommands, build.gradle, testes styles/tab/metadata,
smoke/TabSmoke/TeamOwnershipSmoke/ProfileSmoke, tools/server_smoke.py e docs TAB/STYLES.
ChatRepositoryTest teve fixture histórica ajustada para representar schema3 sem a nova coluna.
Nenhuma dependência externa nova ou configuração de terceiro foi adicionada/modificada.

## GitHub e continuidade

Base anterior salva em main (53e5832). Esta etapa segue em commit separado conforme
autorização persistente do usuário, sem reescrever histórico. Não houve publicação de
ambiente ou release. Próximos passos em NEXT_STEPS; uso em TAB/STYLES/CHAT/CLANS/TITLES.
