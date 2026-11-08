# Matriz de funcionalidades

| Área | Estado | Limitação |
|---|---|---|
| Bootstrap Fabric | Servidor chegou a Done | Login de jogadores não testado |
| Lifecycle modular | Implementado e testado | Dependências em ordem de registro |
| core.json | Defaults, validação e reload testados | Outros JSONs ainda pendentes |
| SQLite | Schema3, upgrade com backup, perfis/concessões/clãs, CAS e auditoria | Schema3 clãs presente; restauração automática pendente |
| Permissões | Adaptador LuckPerms API5.4 e fallback admin | Runtime LuckPerms Fabric não testado |
| Placeholders | Online/nome/UUID/título/tags, fallback literal | Adaptador PAPI e demais dados pendentes |
| Administração | Status/version/modules/permissions/reload/debug/backup/placeholders | Gestão de clãs/títulos pendente |
| Títulos e duas identificações | Implementados e testados | ROLE/SPECIAL; integração com renderizadores e clãs pendente |
| Clãs nativos | Núcleo por comandos implementado/testado | Menus, estilos, chat, títulos CLAN e permissões internas configuráveis pendentes |
| Chat e moderação | Pendente | Canais, segurança e chat assinado |
| TAB e nametags | Pendente | Protocolo e controle de teams |
| Scoreboard visual | Pendente | Mesmo título do TAB; diferenças incrementais |
| /perfil nativo | Handler testado com jogadores sintéticos | Aparência e pacotes de cliente real não testados; menus de clã pendentes |
| Estilos e animações | Pendente | Cache e intervalos seguros |
| Cobblemon/Impactor/Dex/PAPI | Pendente | Auditoria de versões e APIs |
| CI | Workflow de build criado | Execução no GitHub não realizada |
| Release estável | Não disponível | Critérios completos não cumpridos |

Não confundir a API de preferência de scoreboard com uma scoreboard funcional.
