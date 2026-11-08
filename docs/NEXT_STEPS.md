# Próximos passos

## Estado atual

0.4.0-dev mantém títulos/perfil e adiciona núcleo de clãs por comandos com schema3,
criação gratuita, sigla manual, convites, cargos, limites, transferência aceita e exclusão confirmada.
O código até0.3 foi salvo no GitHub em main (89117d0); esta etapa usa um commit separado
no mesmo ramo após validação. Ver CLANS, TITLES e PROJECT_STATUS para estado e evidências.

## Próxima etapa: concluir integrações e interfaces de clãs

1. Configurar matriz de ações internas por cargo, mantendo invariantes de liderança.
2. Menus de clã/membros/convites/cargos, confirmação e personalização, com validação server-side.
3. Títulos categoria CLAN derivados de associação real; invalidar seleção no leave/kick/disband.
4. Chat de clã integrado ao módulo de canais/moderação (fase5), sem bypass de mute/ignore.
5. Estilos básicos/HEX/gradientes/animações por permissão, preservando estilos ao expirar VIP.
6. Administração separada para inspeção/correção e sugestões de nomes de clãs/convites.
7. Validar LuckPerms Fabric real, liderança offline/contextos e mensagens com clientes conectados.

## Demais pendências

Gradientes/animações/condições avançadas e painel de cargo/clã/treinador no /perfil.
Títulos usam cache validado a cada100ticks e em cada ação/leitura; eventos LuckPerms
específicos e aparência com cliente real permanecem pendentes. Não criar segunda
seleção de título para TAB/scoreboard.

Cobblemon1.8.3, Impactor, Shirokai Dex e Fabric Placeholder API ainda exigem auditoria
nas versões exatas. Minecraft dev registrou falha na busca de chaves Yggdrasil;
login online não validado. Não desativar autenticação para mascarar rede.

Depois: chat/moderação, TAB, scoreboard visual, estilos/integrações e testes completos.
Requisitos completos preservados em REQUIREMENTS. Push foi explicitamente autorizado
pelo usuário nesta tarefa. Publicação de ambiente/releases continua sem autorização.
