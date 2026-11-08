# Shirokai Core

Leia `docs/REQUIREMENTS.md`, `docs/PROJECT_STATUS.md` e `docs/NEXT_STEPS.md` antes de alterar o projeto.
Trabalhe no checkout existente; não crie worktree sem solicitação explícita.
Java 21, Minecraft 1.21.1, Fabric. Nunca use APIs Bukkit/Paper.
Não invente integrações: confirme artefatos e assinaturas da versão exata antes de implementar.
Não marque esqueletos como funcionalidades entregues. Atualize documentação e resultados dos testes.
Não publique releases ou configuração de ambiente sem solicitação.
Não acesse APIs Minecraft fora da thread do servidor. Persistência deve usar executor próprio.
Título visual único compartilhado entre TAB e scoreboard; no máximo duas identificações totais no chat.
Clãs gratuitos; sigla manual; cooldowns independentes; redução de limite nunca expulsa membros.
Transferência exige aceitação; exclusão exige confirmação; toda ação revalidada no servidor.
Build: `bash ./gradlew --no-daemon build`; na nuvem: `python3 tools/gradle_cloud.py build`.

Smoke real isolado: `python3 tools/server_smoke.py --accept-eula`; a aceitação atual
foi autorizada explicitamente pelo usuário. Não alterar online-mode para contornar rede.
Configuração de runtime: docs/CONFIGURATION.md; novos schemas precisam de testes
transacionais e de restart. Ao atualizar artefatos, atualizar SHA/versão em PROJECT_STATUS.

Título/perfil: docs/TITLES.md. Usar uma única selected_visual persistente para TAB/scoreboard.
Concessão/revogação e auditoria são transacionais. Revisão otimista deve impedir perda
em ações concorrentes. Nunca limpar permissões de perfil offline em consulta diagnóstica.
Source set smoke só com -PprofileSmoke; não incluir mod de teste no artefato de produção.

Clãs: docs/CLANS.md. Schema3 usa líder como membro e associação global única.
Nunca expulsar membros ao reduzir limite. Transferência/exclusão exigem confirmações.
O usuário autorizou salvar progresso no GitHub nesta tarefa; não confundir com
publicação de ambiente ou release estável. Preservar código e histórico remotos.

Menus de clã devem usar ClanService.scoped(idExibido), nunca agir silenciosamente
sobre clã novo a partir de tela antiga. Identificação CLAN exige associação real e
limpeza transacional em leave/kick/disband. Namespace clan_ é reservado e não concedível.

Chat: docs/CHAT.md. Nunca converter staff/clã indisponível para global silenciosamente.
Toda rota Core deve usar o mesmo gate. Preservar corpo/assinatura/filtro do chat normal;
comandos system não devem ser apresentados como mensagens assinadas. Não interpretar
markup do jogador. A árvore de aliases privados deve continuar moderada após datapack reload.
