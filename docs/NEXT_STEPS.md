# Próximos passos

## Estado atual

0.6.0-dev adiciona chat global/local/clã/staff, privados/reply/ignore e moderação,
com schema4. Mantém perfil/títulos/clãs e seus testes. Aliases privados vanilla são
controlados por configuração, com reaplicação após reload de datapacks. Ver CHAT e PROJECT_STATUS.

## Próxima implementação

1. Motor de estilos central: cores/HEX/gradientes, templates seguros e animações
   de superfícies atualizáveis; chat permanece estático por mensagem.
2. TAB próprio (fase6): header/footer, título escolhido, layouts, ordenação real,
   atualização incremental e ownership de teams. Depois scoreboard visual (fase7).
3. Auditar e integrar APIs reais de Cobblemon1.8.3, LuckPerms Fabric, Impactor, Dex
   e Fabric Placeholder API. Não produzir saldos/dados fictícios.
4. Concluir estilos/personalização de clãs, editor textual gráfico, enviados e
   administração de correção; cargo real/treinador no perfil.
5. Chat: menções estilizadas, prefixo/sufixo/estilos por grupo no motor compartilhado,
   validação de cliente real/assinaturas e coexistência com mods que controlam chat.

## Pendências preservadas

Cliente Minecraft conectado/login online/LuckPerms Fabric real não validados.
Dev server registrou falha na busca de chaves Yggdrasil; não desativar autenticação
para mascarar rede. Smokes de jogadores sintéticos validam métodos/packets capturados,
não uma sessão criptograficamente autenticada. Nenhuma funcionalidade restante foi descartada.

Quando takeVanillaPrivateCommands=false ou comando de terceiro é preservado, não
assumir que o Core modera esse caminho externo. Um único proprietário de chat deve
ser escolhido pelo administrador. Mudança desse ownership exige reinício.

Push continua autorizado: salvar commits em main sem reescrever histórico.
Não publicar ambiente ou release estável automaticamente.
