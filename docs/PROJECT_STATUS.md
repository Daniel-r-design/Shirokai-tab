# Estado real do projeto — 0.4.0-dev

## Situação

Núcleo de clãs da fase4 implementado por comandos, com infraestrutura e títulos/perfil
anteriores preservados. Projeto completo NÃO entregue. Interfaces/estilos de clãs,
chat, TAB, scoreboard visual e integrações completas continuam pendentes.

## GitHub

O usuário autorizou salvar o progresso e continuar. Base0.3 enviada à branch main em
89117d0 (feat: bootstrap Shirokai Core with persisted titles and profile GUI).
Esta etapa0.4 é salva em um commit separado na mesma branch, sem reescrever histórico.
Não há publicação de ambiente, release ou envio de dados/worlds/bancos ao GitHub.
CI compila build/testes/smokeClasses; execução remota do workflow não foi verificada.

## Implementado nesta etapa

- ClanConfig/ClanService/ClanRepository e módulo clans com dependências storage/permissions.
- clans.json validado, defaults preservados e reload atômico junto de core/titles.
- Criação gratuita com permissão/tempo mínimo/cooldown; nome separado de sigla manual.
- Nome/sigla únicos sem caixa e cooldowns independentes; identidade não perde membros.
- Associação única por UUID; cargos líder/oficial/membro e ações com hierarquia protegida.
- Convites persistentes, prazos, limites, recusa/cancelamento e notificação no join.
- Aceitação transacional com capacidade atual do líder e proteção contra troca de líder.
- VIP expirado/limite reduzido não remove membros; só bloqueia novas entradas.
- Transferência com solicitação/aceitação/recusa/prazo; nenhuma transferência por offline.
- Exclusão exige pedido e confirmação explícita com prazo e mesmo líder.
- Histórico e auditoria na mesma transação; falha de auditoria reverte mutação.
- Schema3 com liderança como membro por FK composta diferida e backup pré-upgrade1/2.
- API LuckPerms prepara dados do líder offline antes de calcular permissões na thread do servidor.
- Comandos /clan, info, members, create, tag, rename, invite, invites, accept, decline,
  cancel, leave, kick, promote, demote, transfer, transferaccept, transferdecline,
  disband e disband confirm. Comando de terceiro não é sobrescrito.

## Validação final

| Verificação | Resultado |
|---|---|
| python3 tools/gradle_cloud.py build smokeClasses | PASSOU, Java21 e JAR remapeado |
| JUnit | 41 executados, 0 falhas, 0 erros, 0 ignorados |
| python3 tools/server_smoke.py --accept-eula | 26 verificações funcionais passaram em três boots reais |
| Clãs no Fabric real | CLAN_SMOKE_PASS: mesmo serviço/JDBC com provedor de permissões de teste |
| Capacidade/concorrência | Última vaga não duplica associação; redução não expulsa; recuperação libera |
| Liderança | Transferência aceita, limite menor sem expulsão, confirmação antiga invalidada |
| Segurança interna | Membro não expulsa, oficial não promove/expulsa líder ou outro oficial |
| Expiração | Convite, transferência e confirmação vencidos rejeitados |
| Identidade | Sigla manual, nomes/siglas duplicados e cooldowns independentes testados |
| Atomicidade | Falha de auditoria reverte criação/identidade; títulos anteriores continuam testados |
| Migração | Upgrade2→3 preserva título/revisão e gera backup legível schema2 |
| Regressão de perfil | PROFILE_GUI_SMOKE_PASS e regras anteriores preservadas |
| Shutdown | stop, mundos salvos, módulos STOPPED e Gradle exit0 |
| JAR | Versão0.4.0-dev; classes do mod de teste ausentes |
| Cliente real/login/LuckPerms Fabric | NÃO EXECUTADOS |
| Cobblemon/Impactor/Dex/PAPI | Ainda não implementados/validados |

Suites anteriores (31) preservadas; ClanRepositoryTest(7) e ClanServiceTest(3) adicionadas.
O teste de clãs usa UUIDs e provedor de permissões de teste para simular VIP, não uma
instalação de LuckPerms Fabric. O teste de perfil usa jogadores sintéticos/conexão
sem transporte. Não representam validação de login, rede ou aparência com cliente real.

## Artefato e evidências

`build/libs/shirokai-core-0.4.0-dev.jar`

SHA-256: `0c2362e3ddd04e5682b3694189dcb50ac4c392b00f2f1b4bc484a1e6f9c630e2`

JUnit: `build/reports/tests/test/index.html`.
Smoke: `build/server-smoke/result.json`, logs first-boot/restart/storage-failure.
Source set smoke: testes isolados, nunca incluídos no JAR de produção.
Artefato de desenvolvimento, NÃO release estável.

## Limitações e próximos passos

- Menus, estilos visuais e configuração da matriz de ações internas ainda pendentes;
  os cargos têm regras fixas nesta versão, documentadas em CLANS.
- Chat de clã e títulos CLAN no /perfil aguardam integrações reais; não simulados.
- Ferramentas administrativas de inspeção/correção de clãs ainda pendentes.
- Capacidade usa snapshot de permissões na thread do servidor antes de enfileirar
  a transação; liderança é reconferida no banco. Não há transação distribuída com LuckPerms.
- Minecraft dev registrou falha de busca de chaves Yggdrasil; login online não validado.
  Autenticação não foi desativada. Runtime LuckPerms e contextos do servidor real pendentes.
- Clãs não fazem SQL por tick. Registros expirados podem permanecer para manutenção futura.
- Gradientes/animações/condições avançadas, cargo/clã/treinador no painel, eventos LP,
  renderizadores TAB/scoreboard/chat e integrações externas continuam no plano.

## Arquivos desta etapa

clans/*, storage/ClanSchema.java, SqliteStorage.java, PermissionService/LuckPermsPermissions,
CoreRuntime/ShirokaiCore/CoreConfig, build.gradle, testes clans/*, smoke/ClanSmoke,
ProfileSmoke, tools/server_smoke.py, .gitattributes e documentação.
REQUIREMENTS original foi preservado; atributo de whitespace aceita os CRLF originais.
Detalhes de uso: CLANS.md. Próximos passos: NEXT_STEPS.md.
