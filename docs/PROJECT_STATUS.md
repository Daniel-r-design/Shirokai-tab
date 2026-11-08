# Estado real do projeto — 0.6.0-dev

## Situação

Chat e moderação implementados no núcleo da fase5, com títulos/perfil/clãs preservados.
Projeto completo NÃO entregue. Motor de estilos, menções avançadas, TAB, scoreboard
visual, integrações completas e validação cliente/assinaturas reais permanecem pendentes.
SQLite schema4 migra versões1/2/3 com backup consistente e preservação dos sistemas anteriores.

## Implementado nesta etapa

- ChatConfig validado e preservado em chat.json, com reload atômico core/titles/clans/chat.
- Canais global/local/clã/staff e seleção persistente, por comandos e entrada normal.
- LOCAL usa mesma dimensão e distância3D; CLAN exige UUID igual; STAFF checa remetente
  e destinatário em cada envio. Perda de acesso nunca converte conteúdo para global.
- Privados, reply, ignore persistente e aliases, sem aceitar remetente arbitrário.
- Exibição das duas identificações escolhidas; não adiciona cargo extra automaticamente.
- Mute permanente/temporário, cooldown compartilhado, duplicação, tamanho/caracteres,
  filtro literal normalizado, bypasses explícitos e logs de rejeição com limite de frequência.
- Corpo do jogador literal: nenhum parser MiniMessage/JSON/eventos. Hover/sugestão
  de conversa somente no cabeçalho; corpo não herda clique.
- Mensagem normal mantém SignedMessage, assinatura/corpo/filtro, alterando somente
  nome decorado de apresentação via MessageType.CHAT. Comandos Core usam mensagens system.
- /say e /me de jogadores passam pelo mesmo roteamento/gate; console mantém anúncios vanilla.
- /msg vanilla reconhecido por classe remapeável pode ser assumido via configuração;
  redirects tell/w compartilham callback moderado. Reaplica após registro de datapack reload.
- Comandos de terceiros preservados; caminhos externos não são presumidos como moderados.
- Administração: chat inspect/mute/unmute por UUID, com permissões próprias e auditoria
  atômica; falha de auditoria reverte a mutação.
- Perfil ainda carregando/falhou não pode enviar e não recebe conteúdo seletivo.
- Cache de sessão sem SQLite por mensagem/tick; canal/ignore/mute persistem e reply/spam
  limpam no disconnect. Mute/ignore não mudam canal de sessão ao alterar default no JSON.

## Validação final executada

| Verificação | Resultado |
|---|---|
| python3 tools/gradle_cloud.py build smokeClasses | PASSOU, Java21, testes e JAR remapeado |
| JUnit | 62 executados; 0 falhas; 0 erros; 0 ignorados |
| python3 tools/server_smoke.py --accept-eula | 33 verificações funcionais passaram em três boots Fabric reais |
| ChatSmoke | CHAT_SMOKE_PASS: transporte real com pacotes capturados e jogadores sintéticos |
| LOCAL/GLOBAL/CLAN/STAFF | Distância/dimensão/associação/permissões e destinatários testados |
| Staff revogado | Mensagem negada, sem fallback público nem recepção por usuário revogado |
| Privado/ignore/reply | Só remetente/destinatário, ignore respeitado, peer removido no disconnect |
| Mute e bypass | Rotas compartilhadas bloqueadas; bypass explícito não ignora limites obrigatórios |
| Payload | Corpo/UUID preservados; duas tags no cabeçalho; markup literal e sem clique no corpo |
| Aliases vanilla | Wrapper e redirects conferidos antes/depois de END_DATA_PACK_RELOAD |
| Admin/restart | Mute permanente persistido no servidor, consultado após restart e removido |
| Migração3→4 | Preserva prefs/títulos/clãs e backup legível schema3 |
| Auditoria | Falha reverte mute; regressões de títulos/clãs preservadas |
| Reload | Chat inválido preserva snapshot; trocar ownership exige reinício; canal não muda por mute/ignore |
| Regressões | Menus, clãs, títulos, backup, restart, schema futuro e shutdown passaram |
| JAR | Versão0.6.0-dev e classes do mod de teste ausentes |
| Cliente real/assinatura criptográfica/LuckPerms Fabric | NÃO EXECUTADOS |
| Cobblemon/Impactor/Dex/PAPI | Ainda não implementados/validados |

45 testes anteriores preservados. Adicionados ChatServiceTest(10),
ChatConfigAndGateTest(3) e ChatRepositoryTest(4), total17 novos testes.
ChatSmoke usa SignedMessage.ofUnsigned e conexão sem transporte; confirma métodos,
campos de pacote, corpo e destinatários, NÃO autenticação/criptografia cliente.
Preservação de assinatura no caminho real usa API vanilla auditada, mas exige teste
com cliente autenticado antes de afirmar compatibilidade completa de chat assinado.

## Artefato e evidências

JAR: `build/libs/shirokai-core-0.6.0-dev.jar`

SHA-256: `0ac1438f06723c2a39d3106d0c1b62dc22e8ed911a71446e35680aac39d01bde`

JUnit: `build/reports/tests/test/index.html`.
Resultado: `build/server-smoke/result.json`.
Logs: `build/server-smoke/first-boot.log`, `restart.log`, `storage-failure.log`.
Smokes separados em src/smoke e não incluídos no artefato final.
Todos os servidores de teste foram parados com stop; dados temporários removidos.
Artefato de desenvolvimento, NÃO release estável.

## Limitações restantes

- Formatos atuais decoram remetente com texto literal. Estilos HEX/gradientes,
  menções, grupos/prefixos/sufixos avançados e templates de corpo dependem do motor futuro.
- Mensagens por comandos Core são system e não reportáveis como chat assinado.
  /say,/me podem ter aparência de chat normal por redirecionamento ao canal.
- Outro mod pode possuir comandos/eventos próprios. takeVanillaPrivateCommands=false
  deixa vanilla privado independente; não afirmar cobertura de moderação nesses caminhos.
- Cooldown/reply são de sessão; ignore/canal/mute persistem. Registros vencidos podem
  permanecer para consulta; ausência de perfil carregado é bloqueada explicitamente.
- Cliente conectado, login/Yggdrasil, LuckPerms Fabric/contextos reais e carga com
  múltiplos clientes não validados. online-mode não foi desativado para contornar rede.
- Estilos/administração avançada/editor gráfico de clãs, dados de treinador/cargo real
  no perfil, TAB/scoreboard e integrações externas continuam no plano.

## Arquivos desta etapa

chat/*, administration/ChatAdminCommands/AdminCommands, storage/ChatSchema e SqliteStorage,
CoreRuntime/ShirokaiCore/CoreConfig, ClanCommands, build.gradle, testes chat/*,
smoke/ChatSmoke/ProfileSmoke, tools/server_smoke.py e documentação, incluindo CHAT.md.
Nenhuma dependência externa nova ou configuração de terceiro foi adicionada/modificada.

## GitHub e continuidade

Base anterior salva em main (3f2c959). Esta etapa usa um commit separado, conforme
autorização persistente do usuário. Nenhuma publicação de ambiente/release foi solicitada
ou realizada. Próximos passos em NEXT_STEPS, uso em CHAT, CLANS e TITLES.
