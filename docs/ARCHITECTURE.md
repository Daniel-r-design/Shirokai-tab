# Arquitetura

Pacote raiz `com.shirokai`. Módulos previstos: core, tab, scoreboard, chat, titles,
clans, profiles, animations, placeholders, permissions, integrations, storage, administration.
Implementados nesta etapa: core/config, storage, permissions, placeholders, administration, titles, profiles.

## Lifecycle

CoreModule/ModuleManager usam IDs únicos, dependências explícitas e estados
REGISTERED/RUNNING/DISABLED/BLOCKED/FAILED/STOPPED. A ordem de registro deve ser
topológica; dependência não RUNNING bloqueia somente seu consumidor. Falha no start
tenta limpar o módulo. Shutdown usa ordem inversa e continua após erros individuais.
BiConsumer de erros deve apenas registrar o erro, sem lançar exceções.

CoreRuntime é criado em SERVER_STARTING e encerrado em SERVER_STOPPED.
O gerenciador é confinado à thread do servidor. Não há módulos de gameplay registrados
como ficticiamente ativos. Eventos e comandos foram conferidos no código-fonte Fabric.

## IO e armazenamento

SqliteStorage possui uma conexão confinada ao worker `shirokai-sqlite`, WAL,
foreign_keys=ON, busy_timeout=5000 e synchronous=FULL. Até 256 operações aceitas
pendentes; excedente falha explicitamente. Cancelar a resposta de uma operação aceita
não descarta a gravação. Shutdown drena a fila antes de fechar a conexão.
Prontidão assíncrona é distinta do lifecycle: status SQLite STARTING/READY/FAILED.

Schema 1 criou player_preferences e admin_audit. Schema 2 adiciona perfis, concessões e
identificações de chat com revisão otimista. Migração1→2 gera backup antes da transação.
Versões futuras são rejeitadas. Preferência de scoreboard é uma API de persistência;
nenhuma scoreboard visual foi implementada. UUID é identidade do jogador.
VACUUM INTO gera backup consistente incluindo WAL; restauração automática não existe.

ConfigService usa snapshots imutáveis e arquivos preservados. Reload lê/valida no
worker `shirokai-config` e aplica na thread do servidor; parâmetros de infraestrutura
exigem reinício. O bootstrap lê a configuração uma vez na inicialização.
Callbacks JDBC não acessam jogador/mundo: resultados administrativos voltam por server.execute.

## Integrações e próximos serviços

LuckPermsPermissions é carregado somente quando o mod está presente. Consulta dados
contextuais da API5.4 em cada autorização. Sem ele, existe apenas fallback admin nível4.
PlaceholderService recebe snapshot de UUID/nome/online e retorna texto literal,
sem executar markup ou resolver recursivamente dados substituídos.

TitleRules valida elegibilidade, expiração, superfícies e ordem de identificações.
TitleService coordena IO assíncrono, CAS de revisão e cache na thread do servidor;
ProfileMenu é container somente leitura com ações revalidadas.
Próximos serviços: renderizadores TAB/scoreboard; clãs transacionais;
GUI com validação no servidor; canais e moderação; formatação administrativa separada
de conteúdo de jogador; adaptadores opcionais; renderização incremental com ownership
explícito de objectives e teams. Requisitos completos preservados em REQUIREMENTS.


## Clãs0.4

ClanConfig/ClanService capturam regras e permissões na thread do servidor.
ClanRepository roda SQL exclusivamente via SqliteStorage.transact; nunca acessa jogo
na thread JDBC. ClanSchema (storage) mantém DDL de schema3 separado do serviço.
Operações usam UUID, FK composta diferida para liderança, associação global única,
limite na aceitação e histórico/auditoria na mesma transação. Permissões de líder
offline podem ser preparadas por LuckPerms.loadUser de forma assíncrona antes do snapshot.


## Menus e títulos de clã0.5

ClanMenu usa páginas e container read-only. ClanService.scoped compartilha config/cache/
listeners, mas seu repositório exige expectedClan na mesma transação de mutação.
roleActions é snapshot validado; ClanPolicy e hierarquia SQL reconferem autorização.
ClanService publica mudanças online após commit, incluindo alvo removido e membros
anteriores em disband. TitleService recebe provedor dinâmico/refresh por interfaces
funcionais, sem depender do serviço de clãs; catálogo efetivo é específico do jogador.
ClanTitle deriva nome/display de ClanSnapshot. Persistência verifica associação real
em save e limpa seleção na própria transação de leave/kick/disband, com revisão CAS.


## Chat0.6

ChatService é núcleo sem Minecraft, com Audience/Participant e Sink/Envelope;
política/cache/gates ficam na thread do servidor. ChatRepository roda SQL pelo
worker transacional; ChatSchema mantém DDL4 e storage cria backup antes de upgrade.
MinecraftChat adapta jogadores/posições/clã e transporte: SentMessage preservado
para mensagem normal, GameMessage literal para comandos. Sender label é separado
do corpo, com hover/sugestão somente no remetente. Sem SQL no roteamento por mensagem.
ServerMessageEvents bloqueia broadcast original e impede duplicação; aliases vanilla
são reconhecidos por referência de classe remapeável e configurados novamente ao
recriar árvore de comandos. ChatConfig participa do snapshot de reload atômico;
ownership exige reinício. Mutes/ignore/canal persistem; reply e gates são de sessão.


## Estilos/TAB0.7

StyleEngine compila AST allowlisted e injeta dados literalmente; caches LRU são
confinados à thread do servidor após publicar o candidato de reload. Compilação/
validação usa dados imutáveis e não acessa mundo/network no workerIO.
TabPlan valida e ordena condições/templates antes de aplicar. TabRuntime lê dados
online/cache, compartilha resoluções, compara componentes por viewer e só envia diffs.
TabPackets usa accessor version-pinned para entries do PlayerListS2CPacket vanilla.
Não injeta getPlayerListName nem inventa list-order. TeamOwnership só administra
referências criadas por ele; estrangeiros/collisions são preservados. SERVER_STOPPING
libera teams antes do save, e SERVER_STOPPED fecha demais serviços.
Schema5 adiciona preferência sem alterar estado de scoreboard; nenhuma consulta SQL
no render. Group metadata é lida em cada atualização, sem persistir autoridade do LP.
