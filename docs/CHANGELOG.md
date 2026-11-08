# Histórico

## 0.5.0-dev — menus e identificação de clãs

- Menus nativos para clã, membros/cargos, convite/aceitação e confirmações.
- Cancelamento revoga confirmação de exclusão no banco.
- Matriz de ações por cargo configurável, com invariantes de liderança/hierarquia.
- Ações de menus vinculadas ao ID do clã exibido, validadas na transação.
- Título CLAN dinâmico e clã atual no /perfil, respeitando duas identificações totais.
- Leave/kick/disband removem seleção CLAN atomicamente; save bloqueia falsificação.
- Cache online atualizado por eventos do serviço sem SQL por tick.
- Testes de integração de regras e menus em servidor real com jogadores sintéticos.

## 0.4.0-dev — núcleo de clãs e GitHub

- Código0.3 salvo na branchmain do GitHub (89117d0), conforme pedido do usuário.
- Schema3 com backups e integridade de liderança/associação.
- Criação e alterações gratuitas, nome/sigla independentes, convites persistentes.
- Cargos, limites por permissão, perda de VIP sem expulsão e aceitação transacional.
- Transferência aceita e exclusão confirmada, prazos e auditoria atômica.
- clans.json validado/reload e comandos sem sobrescrever comandos de terceiros.
- Testes de concorrência, expiração, permissões, rollback, migração e componente no servidor real.

## 0.3.0-dev — títulos e perfil

- titles.json validado sem sobrescrever customizações; reload junto com core.json.
- Concessões permanentes/temporárias, expiração, permissões e seleção visual única.
- Duas identificações totais com ordem por categoria/prioridade e bloqueio da terceira.
- Schema2 com backup pré-migração, CAS por revisão e auditoria transacional.
- /perfil nativo com paginação, escolha, remoção, refresh e retorno a /menu.
- Proteções de inventário, ações revalidadas e limpeza de cache no disconnect.
- Administração de títulos por UUID e placeholders de título/identificações.
- Testes de regras, persistência, concorrência, migração e GUI no servidor com jogadores sintéticos.

## 0.2.0-dev — infraestrutura e servidor

- EULA aceita por solicitação explícita e boot real Fabric concluído.
- Configuração core.json validada, defaults preservados e reload transacional em memória.
- SQLite assíncrono com schema versionado, rollback, limites de fila, preferências,
  auditoria, backup consistente e drenagem no shutdown.
- Dependências de módulos e isolamento de falhas.
- Adaptador LuckPerms API5.4 e fallback administrativo nível4 quando ausente.
- Placeholders nativos literais com fallback; comandos administrativos com permissões.
- Testes unitários e smoke em servidor real, com reload, restart e falhas de storage.

## 0.1.0-dev — início

- Auditoria de checkout vazio e preservação do prompt integral.
- Bootstrap Fabric/Gradle/Java21, lifecycle inicial, CI e documentação de continuidade.
