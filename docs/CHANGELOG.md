# Histórico

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
