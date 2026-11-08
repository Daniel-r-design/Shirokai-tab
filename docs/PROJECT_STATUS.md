# Estado real do projeto — 0.5.0-dev

## Situação

Menus de clãs, matriz configurável de ações e identificação nativa de clã no /perfil
implementados. Projeto completo NÃO entregue: chat/moderação, estilos avançados,
TAB, scoreboard visual, integrações completas e validação com cliente real pendentes.
SchemaSQLite3 preservado sem migração de versão nesta etapa.

## Implementado

- /clan e /clan menu abrem container vanilla9x6, sem mod obrigatório no cliente.
- Páginas de clã, membros/cargos, convites recebidos, convite para jogadores online,
  gestão do membro, solicitação de transferência e confirmação de exclusão.
- Convite pode ser aceito/recusado no menu com validação transacional existente.
- Transferir exige confirmação do líder e aceitação separada pelo destinatário.
- Excluir exige duas telas/ações e confirmação no banco; cancelar remove confirmação persistente.
- Itens são ícones, sem transferências de inventário; ações de outro usuário, tela
  fechada, slots inválidos, swaps/shift/clone/throw/craft são bloqueadas.
- Ações de tela são vinculadas ao ID do clã exibido. ClanRepository.scoped valida
  associação e ID na mesma transação; tela antiga não atua sobre novo clã.
- roleActions em clans.json configura invite/kick/promote/demote/rename/tag por cargo.
  Liderança e hierarquia permanecem invariantes; autorização usa config/cargo atuais.
- ClanTitle deriva título CLAN do ID/associação/sigla nativos, sem concessão artificial.
- /perfil mostra clã atual e oferece sigla como título visual único ou uma das duas
  identificações totais de chat. Ordem ROLE→SPECIAL→CLAN e prioridades preservadas.
- Leave/kick/disband limpam a seleção de clã na mesma transação da associação.
  SaveTitles também valida associação real e sigla no SQL, bloqueando reequipes antigos/forjados.
- Nome/display da identificação acompanha rename/tag, mantendo ID estável.
- Cache de associação online atualiza por eventos do serviço e limpa no disconnect;
  não faz SQLite por tick. Títulos carregam associação antes de view/equip.

## Verificações executadas

| Verificação | Resultado |
|---|---|
| python3 tools/gradle_cloud.py build smokeClasses | PASSOU, Java21 e remap do JAR |
| JUnit | 45 executados, 0 falhas, 0 erros, 0 ignorados |
| python3 tools/server_smoke.py --accept-eula | 27 verificações funcionais passaram em três boots reais |
| ClanMenuSmoke | CLAN_MENU_SMOKE_PASS |
| Menu: convite/membro/cargo | Aceitação, listagem e promoção passaram |
| Menu: transferência | Não solicita antes de confirmar; sucessor aceita separadamente |
| Menu: exclusão | Primeiro clique não exclui; cancelar invalida pedido; segundo fluxo confirmado exclui |
| Itens/ownership | Outro usuário e transferências bloqueados; inventário preservado; tela fechada inutilizável |
| Escopo antigo | Troca de clã faz mutação antiga falhar sem alterar associação nova |
| Matriz de ações | Regras configuráveis aplicadas sem permitir operar contra líder/pares |
| Identificação CLAN | Duas vagas compartilhadas, título único e outsider rejeitado |
| Persistência | Leave/kick/disband limpam tag atomicamente e save posterior forjado é rejeitado |
| Regressões | Perfil, núcleo de clãs, reload, backup, restart e schema futuro passaram |
| Artefato | ClanMenu presente e mod de teste ausente no JAR0.5 |
| Cliente real/login/LuckPerms Fabric | NÃO EXECUTADOS |
| Cobblemon/Impactor/Dex/PAPI | Ainda não implementados/validados |

41 testes anteriores preservados; ClanIntegrationTest(4) adicionada.
Smokes usam servidor Fabric real com ServerPlayerEntity sintéticos/conexão sem
transporte e provedores de permissões de teste. Não comprovam aparência, rede,
autenticação ou contextos reais de LuckPerms. Não foi desativado online-mode.

## Artefato e evidências

JAR: `build/libs/shirokai-core-0.5.0-dev.jar`

SHA-256: `f5cbdfdc9fc0e4008aa1d8374bcae03754a5cc8041950d66244b48ceaa7fa76f`

JUnit: `build/reports/tests/test/index.html`.
Resultado: `build/server-smoke/result.json`.
Logs: `build/server-smoke/first-boot.log`, `restart.log`, `storage-failure.log`.
Source set smoke nunca é incluído no artefato de produção. Todos os servidores
foram parados com stop e os dados temporários dos smokes são removidos.
Artefato de desenvolvimento, NÃO release estável.

## Pendências

Editor textual gráfico de nome/sigla (atuais comandos mostrados no painel), listagem
gráfica de convites enviados, estilos por permissão, ferramentas administrativas de
clãs, chat integrado a moderação/ignore/mute. Cargo real/dados do treinador no perfil,
eventos LuckPerms específicos, integrações e renderizadores futuros continuam no plano.
Títulos CLAN não podem ser declarados no JSON nem concedidos pelo admin; só associação real.
Matriz roleActions substitui todas as regras quando fornecida; campo ausente usa defaults.
Ainda há falha observada na rede de chaves Yggdrasil; login online não validado.

## Arquivos desta etapa

ClanMenu, ClanPolicy, ClanConfig/Repository/Service/Commands, ClanTitle, TitleDefinition/
Catalog/Rules/Service, ProfileMenu/Commands, CoreRuntime, SqliteStorage.saveTitles,
ClanIntegrationTest, ClanMenuSmoke/ProfileSmoke, tools/server_smoke.py, build.gradle e docs.
Não foram adicionadas dependências externas ou alterados arquivos de terceiros.

## GitHub e continuidade

Progresso anterior está na branch main (7a7eab1 e 89117d0). Esta etapa será salva em
commit separado conforme autorização persistente do usuário, sem reescrever histórico.
Nenhuma publicação de ambiente/release foi solicitada ou realizada. Ver NEXT_STEPS.
