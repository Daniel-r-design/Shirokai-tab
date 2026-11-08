# Permissões 0.4.0-dev

LuckPerms é a fonte de verdade quando presente e inicializado. O adaptador usa API 5.4,
usuário carregado e dados de permissão com contextos atuais. Não retém resultado de
permissão entre chamadas; FALSE/UNDEFINED/usuário ausente negam acesso.
Não altera grupos, prefixos ou permissões para fins visuais.

| Nó | Ação |
|---|---|
| shirokaicore.command.status | status e resultado da raiz sem subcomando |
| shirokaicore.command.version | versão |
| shirokaicore.command.modules | estados dos módulos |
| shirokaicore.command.permissions | provedor |
| shirokaicore.command.reload | recarregar configuração |
| shirokaicore.command.debug | integridade SQLite |
| shirokaicore.command.backup | criar backup |
| shirokaicore.command.placeholders | listar/resolver placeholders |
| shirokaicore.command.titles.inspect | listar catálogo e consultar perfil por UUID |
| shirokaicore.command.titles.grant | conceder títulos próprios permanentes/temporários |
| shirokaicore.command.titles.revoke | revogar concessões e seleção correspondente |

Com LuckPerms, nenhum desses nós é concedido automaticamente aos jogadores.
Sem LuckPerms, há fallback somente para comandos administrativos para jogadores
vanilla nível 4. Console usa autoridade vanilla nível 4; command blocks padrão
(nível 2) não ganham acesso. Se administration estiver desativado/bloqueado ou o
runtime não iniciar, apenas status/version permanecem disponíveis ao console nível 4.
Se placeholders estiver desativado, o subcomando correspondente fica indisponível.

A integração foi compilada e testada contra contrato da API; a distribuição real
LuckPerms Fabric ainda não foi instalada/testada. Isso não comprova compatibilidade
com todas as versões do mod.

## Clãs implementados

shirokaicore.clan.create autoriza criação; shirokaicore.clan.limit.<n> determina
limite do líder conforme clans.json, usando o maior valor. Demais ações validam
cargos internos no banco. Fallback admin sem LuckPerms não concede criação.

## Nós futuros de estilos, ainda não implementados

`shirokaicore.clan.color.hex`,
`shirokaicore.clan.color.gradient`, `shirokaicore.clan.animation`,
`shirokaicore.clan.tag.edit`, `shirokaicore.clan.style.*`, canais e bypass de moderação.
Cargos internos de clã não substituem LuckPerms.


/perfil permite gestão do próprio UUID sem permissão administrativa. Cada título
valida seu desbloqueio e a permissão configurada. ROLE exige permission; nunca atribui
esse grupo ao jogador. grant não ignora a permissão exigida por um título.
Detalhes e limites de chat em TITLES.md.
