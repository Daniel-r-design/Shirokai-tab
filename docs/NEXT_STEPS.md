# Próximos passos

## Estado atual

0.7.0-dev adiciona motor de estilos e TAB nativo: header/footer, título escolhido,
layouts, nomes incrementais, metadata LuckPerms e teams opt-in. Schema5 adiciona
preferência de animações. Demais módulos/testes preservados. Ver TAB, STYLES e PROJECT_STATUS.

## Próxima implementação

1. Scoreboard visual (fase7): objetivos/linhas reais, atualização por diferenças,
   mesmo título do TAB, persistência da visibilidade e ownership seguro do sidebar.
2. Auditar APIs reais de Cobblemon1.8.3, Impactor, Dex, Fabric Placeholder API e
   LuckPerms Fabric; exibir fallback em vez de dados fictícios.
3. Integrar estilos de forma segura a cabeçalhos de chat/menus/títulos/clãs, com
   permissões, menções e preservação do estilo salvo após perda de VIP.
4. Completar ferramentas administrativas de clãs, editor textual gráfico e enviados.
5. Validar TAB/teams/gradientes com cliente real e runtime LuckPerms, incluindo
   espectador, mundo, reconexão, vários jogadores e conflitos de pacotes de outros mods.

## Limitações preservadas

Ordenação por teams é opt-in e pode afetar mecânicas de equipe. Nunca adotar/remover
team externo. Cliente vanilla dá precedência a espectador e desempata por nome de perfil.
Mixin mínimo acessa entries de packetMC1.21.1; manter refmap e testes do JAR.
Nametags, abaixo do nome, bossbar e listed/vanish por viewer permanecem pendentes.
Não afirmar compatibilidade com mods que sobrescrevem nomes/header por pacotes sem verificação.

Cliente autenticado, Yggdrasil, contexto real LuckPerms e carga/multiclientes não
validados. Não desativar autenticação para mascarar rede. Smokes capturam métodos/
packets de jogadores sintéticos, não validam renderização ou assinatura criptográfica real.

Push continua autorizado: salvar commits em main sem reescrever histórico.
Não publicar ambiente ou release estável automaticamente.
