# Próximos passos

## Estado atual

0.5.0-dev adiciona menus de clãs, matriz configurável de ações e identificação CLAN
real no perfil. Schema3 permanece. Proteções de escopo de menu e limpeza de seleção
na saída/expulsão/exclusão são transacionais. Ver CLANS, TITLES e PROJECT_STATUS.

## Próxima implementação

1. Chat/canais/moderação (fase5), incluindo chat de clã, duas identificações totais,
   permissões, cooldown, mute, ignore e texto do jogador separado de templates administrativos.
2. Ferramentas administrativas separadas para inspeção/correção de clãs e diagnóstico.
3. Motor de estilos compartilhado com cores/HEX/gradientes/animações e permissões:
   preservar estilo salvo quando VIP expirar e aplicar fallback permitido.
4. Editor gráfico textual de nome/sigla e lista gráfica de convites enviados.
   Atualmente nome/sigla usam comandos e convites recebidos possuem GUI funcional.
5. Testar menus com cliente Minecraft real e LuckPerms Fabric, incluindo líder offline/contextos.

## Pendências preservadas

Cargo real/dados do treinador no perfil, condições avançadas, eventos LuckPerms
específicos, Cobblemon1.8.3, Impactor, Shirokai Dex, Fabric Placeholder API.
Títulos atuais validam em ações/leitura e a cada100ticks; clã usa cache atualizado
por eventos do serviço sem SQL por tick. Não criar seleção visual separada para TAB/scoreboard.

Dev server registrou falha na busca de chaves Yggdrasil; login online não foi validado.
Não desativar autenticação para mascarar rede. Testes com jogadores sintéticos validam
handlers, não rede/renderização cliente. Depois: TAB, scoreboard visual, integrações e testes completos.

O usuário autorizou salvar progresso no GitHub; manter commits e main sincronizados
sem reescrever histórico. Não publicar ambiente ou release estável automaticamente.
