# Próximos passos

## Estado atual

0.3.0-dev adiciona títulos, concessões permanentes/temporárias, seleção visual única,
duas identificações compartilhadas, /perfil nativo e administração. A migração2
preserva schema1 e cria backup antes da transação. Ver TITLES e PROJECT_STATUS.

## Próxima implementação: clãs (fase 4)

1. Schema de clãs/membros/convites/cooldowns/histórico com índices e transações.
2. Criação gratuita, nome e sigla manual independentes e unicidade case-insensitive.
3. Convites com expiração e capacidade revalidada atomicamente na aceitação.
4. Limite pelas permissões do líder: redução nunca expulsa membros existentes.
5. Cargos internos, transferência com aceitação e exclusão confirmada.
6. Menus e títulos de categoria CLAN derivados de pertencimento real, sem concessões fictícias.
7. Testar concorrência, restart, cooldowns independentes e permissões antes de liberar.

## Pendências de títulos/perfil

- Validar aparência e tráfego de cliques com um cliente Minecraft real conectado.
  Teste server-side com jogadores sintéticos passou; não equivale a autenticação/rede cliente.
- Cargo real, clã e dados do treinador no painel dependem das integrações correspondentes.
- Gradientes, animações, condições avançadas e demais metadados do catálogo precisam
  do motor de estilos/integrações. Texto desta versão é literal; não interpretar markup de usuário.
- Subscrição de eventos LuckPerms para invalidar imediatamente; atualmente há validação
  em cada ação/leitura e revalidação do cache a cada100ticks, sem consultar SQL por tick.
- Fallback visual é ausência de seleção + fallback do consumidor. Não implementar
  uma segunda seleção independente em TAB e scoreboard.
- Catálogo inválido no bootstrap isola titles e bloqueia profiles; após corrigir esse
  caso é necessário reiniciar. Reload de catálogo ativo é validado atomicamente.

## Integrações e infraestrutura

LuckPerms Fabric runtime, Cobblemon1.8.3, Impactor, Shirokai Dex e Fabric Placeholder API
ainda exigem auditoria/validação nas versões exatas. Não inventar assinaturas.
Modrinth recebeu CONNECT403 na auditoria inicial. Dev server registrou falha de busca
de chaves Yggdrasil; login online não foi testado. Não desativar autenticação para mascarar rede.
GUI smoke usa source set smoke separado, carregado só com -PprofileSmoke, nunca no JAR final.
Migrações futuras devem continuar preservando dados e gerando backups antes de alterações.

## Depois

Chat/moderação, TAB, scoreboard visual, estilos/integrações e testes completos.
Todos os requisitos originais continuam em REQUIREMENTS. Não publicar ambiente,
push ou release automaticamente.
