# Prompt para continuar o Shirokai Core em outro Codex

Copie o texto abaixo para o outro Codex, com acesso ao repositório
`Daniel-r-design/Shirokai-tab` na branch `main`.

---

Continue o desenvolvimento do Shirokai Core no repositório
https://github.com/Daniel-r-design/Shirokai-tab, a partir da versão mais recente
da branch main. O projeto ainda não está concluído. Trabalhe no checkout existente
e preserve alterações locais e o histórico remoto. Confira git status, branch e
remoto antes de sincronizar; não use reset destrutivo, force push ou reescrita de
histórico. Está autorizado salvar o progresso com commits e push para main.
Não publique releases nem alterações de configuração do ambiente na nuvem.

## Primeiro, recupere o contexto

Leia integralmente AGENTS.md, docs/REQUIREMENTS.md, docs/PROJECT_STATUS.md e
docs/NEXT_STEPS.md. Leia também docs/API_COMPATIBILITY.md, FEATURE_MATRIX.md,
CONFIGURATION.md, PERMISSIONS.md, TEST_PLAN.md, TAB.md e STYLES.md antes das
respectivas mudanças. Os caminhos abreviados desta lista ficam em docs/.
REQUIREMENTS.md contém o pedido original completo; este resumo não o substitui.
Se o estado atual tiver avançado, use o código e as evidências mais recentes e
atualize este documento. Não reaplique etapas já concluídas.

## Ponto de partida desta passagem

Versão 0.7.0-dev, implementação registrada no commit
d9099c02b26a16a9f94f9212d11a157c665b1741. Commits posteriores podem conter
esta documentação ou novas entregas; não volte o checkout para esse commit.

Implementados: infraestrutura modular, configuração com reload atômico, SQLite
assíncrono, permissões LuckPerms, placeholders nativos, administração/auditoria,
títulos persistidos, menu /perfil, clãs e seus menus, chat moderado e canais,
motor seguro de estilos/animações e TAB nativo incremental. SQLite está no schema5.
TAB possui header/footer, layouts condicionais, título selecionado, nomes por
viewer, preferência persistente de animação e ordenação opcional por teams.
Styles ainda é consumido pelo TAB; sua adoção nas demais superfícies está pendente.

Última validação documentada: 73 testes JUnit sem falhas e 37 verificações
funcionais em três boots Fabric. São resultados anteriores, não testes executados
por você. Os jogadores do smoke são sintéticos: não houve validação de renderização
com cliente real, autenticação/assinaturas reais, carga ou runtime LuckPerms Fabric.
Não apresente esses cenários como validados.

## Próxima entrega: scoreboard visual

Implemente a fase7 descrita nos requisitos: objetivos e linhas reais, layouts e
condições, atualizações por diferenças, o mesmo título visual escolhido no perfil
e preferência persistente de visibilidade. A preferência scoreboard_enabled já
existe; preserve sua independência de animations_enabled. Use o motor de estilos
existente e dados reais disponíveis, com fallback explícito para indisponíveis.

Antes de programar, audite as classes, assinaturas e limites reais de scoreboard
e packets em Minecraft 1.21.1/Yarn da versão fixada. Não invente métodos ou campos.
Defina ownership seguro do sidebar por viewer, respeitando outros mods e liberando
somente recursos próprios. Planeje reconexão, reload, mudanças de layout, desligamento
e limpeza. Trate colisões de comandos sem assumir comandos de terceiros.
Integre módulo/configuração ao reload atômico, mantendo o snapshot anterior quando
a configuração candidata falhar. CoreConfig já tem dez módulos em Map.of; ao
adicionar o décimo primeiro, use uma construção compatível, como Map.ofEntries.

Depois avance conforme NEXT_STEPS e requisitos: integrações reais de Cobblemon
1.8.3, Impactor, Dex e Fabric Placeholder API; estilos nas demais superfícies;
ferramentas administrativas/editor gráfico de clãs; recursos restantes de TAB;
testes com cliente real e compatibilidade entre mods. Não simule essas integrações
nem considere a API nativa de placeholders como integração Fabric PAPI entregue.

## Regras que devem permanecer verdadeiras

- Java21, Minecraft1.21.1 e Fabric; nenhuma API Bukkit/Paper. Confirme versões e APIs
  externas antes de implementar. LuckPerms é a fonte de grupos/permissões; escolhas
  visuais nunca alteram grupos reais. Moedas: impactor:dollars e impactor:tokens.
- Um único título visual compartilhado entre TAB e scoreboard. No chat, no máximo
  duas identificações totais entre ROLE, SPECIAL e CLAN, com ordenação configurada.
- Clãs gratuitos, nome e sigla manuais independentes, cooldowns separados. Redução
  de capacidade nunca expulsa membros; apenas bloqueia novas entradas. Transferir
  liderança exige aceitação do destinatário; excluir exige confirmação explícita.
- Toda ação de menu revalida autorização no servidor. Preserve escopo do clã
  exibido contra callbacks antigos e revisão otimista de perfis. O namespace
  clan_ é reservado à associação real; não conceda clãs fictícios como títulos.
- APIs de jogo/rede na thread do servidor; SQLite em executor próprio, sem consulta
  por tick. Migrações transacionais com backup e testes de upgrade/restart, sem
  apagar dados nem aceitar schema futuro silenciosamente.
- Preserve corpo, assinatura e filtro do chat normal; mensagens system de comandos
  não são assinadas. Não interprete markup escrito pelo jogador nem converta canal
  staff/clã indisponível para global. Moderação cobre aliases e datapack reload.
- Dados dinâmicos entram literalmente no StyleEngine. Text em cache é compartilhado
  e somente leitura. Não adote teams externos pelo nome; limpe os próprios antes
  do save em SERVER_STOPPING. Preserve mixin/refmap do packet PlayerList1.21.1.
- Não invente saldos, progresso de Pokédex ou ginásios. Não anuncie compatibilidade,
  conclusão ou percentuais exatos sem evidências. Consulte os requisitos completos.

## Compilação e verificação

Ferramentas fixadas: Gradle8.12.1, Loom1.10.5, Yarn1.21.1+build.3,
Fabric Loader0.16.14 e Fabric API0.116.17+1.21.1. Confira build.gradle,
gradle.properties e wrapper antes de alterar versões.

Na nuvem atual:

```sh
python3 tools/gradle_cloud.py build smokeClasses
python3 tools/server_smoke.py --accept-eula
```

O helper configura JDK/proxy/CA sem expor credenciais. O JDK atual está em
/workspace/toolchains/jdk-21.0.13+9; em outro ambiente, prepare Java21 completo
conforme o helper e a documentação. Fora desta nuvem, com JDK21 configurado:

```sh
bash ./gradlew --no-daemon build smokeClasses
```

O usuário já aceitou explicitamente a EULA para os testes de desenvolvimento.
Não desative online-mode para contornar falhas de rede/autenticação.
O servidor smoke é isolado; -PshirokaiRunDir deve receber caminho relativo.
O source set smoke é habilitado por -PprofileSmoke e não pode entrar no JAR final.
Testes que dependem das transformações Fabric devem rodar no smoke apropriado,
sem desabilitar cenários para fazer a suíte passar.

Artefato atual: build/libs/shirokai-core-0.7.0-dev.jar. Relatórios ficam em
build/reports/tests/test/index.html e build/server-smoke/result.json.
CI de build/upload de JAR está configurado em .github/workflows/build.yml;
não afirme que a execução remota passou sem verificá-la.

## Como entregar o próximo progresso

Implemente e teste comportamentos reais, preservando regressões. Atualize
PROJECT_STATUS, NEXT_STEPS, FEATURE_MATRIX, API_COMPATIBILITY, CONFIGURATION,
PERMISSIONS, TEST_PLAN e CHANGELOG conforme o escopo, com resultados executados,
limitações e versão/SHA do novo artefato quando aplicável. Atualize este prompt
se o ponto de continuidade mudar.

Revise o diff, execute os checks pertinentes e faça commit/push para main sem
reescrever histórico. Confirme que HEAD local corresponde ao remoto. Código,
testes, configuração versionável e documentação vão ao Git; mundos, banco de
runtime, caches, build e segredos ficam fora dele. Não publique release estável
ou ambiente automaticamente. Informe em português o que entregou, quais testes
executou e a próxima pendência concreta. Continue o trabalho autorizado até
concluir a etapa, comunicando bloqueios reais com evidências.
