# Clãs nativos — 0.4.0-dev

Núcleo funcional por comandos. Menus, estilos avançados, chat de clã, títulos CLAN
e ferramentas administrativas de correção ainda não estão implementados.

## Regras implementadas

- Criação gratuita: nenhuma API de economia é chamada.
- /clan create <nome> cria nome e ID UUID estável; não gera sigla.
  O líder escolhe a sigla manualmente com /clan tag <sigla>.
- Nome e sigla são únicos sem diferenciar caixa, com limites, regex e reservas configuráveis.
- Cada UUID pertence a no máximo um clã, imposto também pelo banco.
- Membros são identificados por UUID. Nomes são usados apenas para resolver jogadores online.
- ROLE LEADER deriva de um único leader_uuid; OFFICER/MEMBER são cargos internos.
- Líder pode convidar, expulsar, promover, rebaixar, alterar identidade e solicitar transferências.
- Oficial pode convidar/cancelar e expulsar membros; não expulsa outro oficial ou líder.
- Membro pode sair; líder precisa transferir ou confirmar exclusão.
- Matriz de ações internas ainda é fixa nesta versão; configuração por cargo está pendente.
- Nome/sigla têm cooldowns independentes, iniciados somente em alteração bem-sucedida.
- Convites persistem, expiram, permitem recusa/cancelamento, têm cooldown por remetente/alvo
  e limite de pendências por clã e destinatário. Convites ativos são informados no join.
- Capacidade é checada na transação de aceitação, a partir de permissões do líder
  resolvidas na thread do servidor imediatamente antes de enfileirar a operação.
  A liderança é conferida novamente no banco; se mudou, a entrada é rejeitada para tentar de novo.
- Perda de VIP/limite menor não expulsa ninguém: somente novas entradas são bloqueadas.
- Aceitar convites simultâneos não ultrapassa a capacidade e não duplica associação.
- Transferência exige aceitação pelo sucessor membro, dentro do prazo; nunca ocorre por offline.
- Transferência revalida líder/membro e mantém todos os membros, mesmo acima do novo limite.
- Exclusão exige /clan disband e depois /clan disband confirm no prazo e pelo mesmo líder.
- Troca de líder invalida confirmação de exclusão antiga. Alvo que saiu/foi expulso
  perde a solicitação pendente de transferência.
- Histórico e auditoria são gravados na mesma transação; falha reverte a operação.

## Configuração

`config/shirokai-core/clans.json`, schemaVersion=1, gerado apenas se ausente.
Campos desconhecidos/tipos incorretos são rejeitados. Campos ausentes usam defaults
em memória, exceto schemaVersion obrigatório. Participa do reload atômico com core/titles.

| Campo | Default / significado |
|---|---|
| creationEnabled | true |
| minimumPlaySeconds | 0; usa PLAY_TIME do Minecraft dividido por20 |
| creationCooldownSeconds | 86400 |
| nameCooldownSeconds | 604800 |
| tagCooldownSeconds | 604800 |
| minimumNameLength / maximumNameLength | 3 /32 |
| minimumTagLength / maximumTagLength | 2 /5 |
| namePattern | [A-Za-z0-9 _-]+ |
| tagPattern | [A-Za-z0-9]+ |
| reservedNames | admin, staff, shirokai; comparação sem caixa |
| defaultLimit | 20 |
| permissionLimits | shirokaicore.clan.limit.30→30, .40→40, .50→50 |
| invitationSeconds | 300 |
| invitationCooldownSeconds | 30, por remetente/alvo |
| maximumInvitations | 20, por clã e destinatário |
| confirmationSeconds | 60, transferência/exclusão |

Sempre usa o maior limite permitido, pelo menos defaultLimit. Não há custo configurável:
criação/alterações são gratuitas. Alterar clans em core.json exige reinício.
Aumentar/reduzir limites em clans.json aceita reload; não remove membros existentes.
Renomear um clã preserva ID, membros, convites, progresso e histórico.

## Comandos

```
/clan
/clan info
/clan members
/clan create <nome>
/clan tag <sigla>
/clan rename <nome>
/clan invite <jogador online ou UUID>
/clan invites
/clan accept <nome do clã>
/clan decline <nome do clã>
/clan cancel <jogador online ou UUID>
/clan leave
/clan kick <jogador online ou UUID>
/clan promote <jogador online ou UUID>
/clan demote <jogador online ou UUID>
/clan transfer <jogador online ou UUID>
/clan transferaccept
/clan transferdecline
/clan disband
/clan disband confirm
```

Criação exige shirokaicore.clan.create no LuckPerms. Sem LuckPerms o fallback administrativo
não concede essa permissão de gameplay. Limites usam shirokaicore.clan.limit.<n> configurados.
Usuário offline do líder é preparado pela API LuckPerms antes de avaliar limite;
integração com a distribuição Fabric/contextos reais continua pendente de teste.

Comando conflitante /clan de outro mod é preservado e o Core registra aviso sem sobrescrever.
Feedback usa texto literal, sem interpretar formatação/comandos enviados pelo jogador.
UUIDs podem ser usados para alvos offline; convites/transferências ainda obedecem prazo.

## Banco e testes

Schema3 adiciona clans, clan_members, clan_invites, cooldowns, transferências,
confirmações e histórico. FK diferida composta garante líder membro do próprio clã;
leader_uuid único por linha e PK global de membro impedem múltipla associação.
Migração de schema1/2 gera backup consistente antes da transação; dados de títulos/prefs são preservados.
Schema futuro é rejeitado sem reset. Não há SQL por tick para clãs.

Testes usam SQLite real e provedor de permissões de teste para simular VIP/expiração.
Smoke do componente roda no Fabric real, sem autenticação falsa nem desativação de online-mode.
Isso não equivale a testar LuckPerms Fabric instalado, interfaces de clãs ou múltiplos clientes.
