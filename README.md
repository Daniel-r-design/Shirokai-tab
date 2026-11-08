# Shirokai Core

Mod server-side Fabric para Minecraft 1.21.1 / Java 21, em desenvolvimento.
A versão 0.6.0-dev implementa infraestrutura de configuração, SQLite, permissões,
placeholders nativos, administração, títulos, perfil, clãs e chat/moderação.
TAB, scoreboard visual, estilos avançados e integrações completas permanecem pendentes.
Esta versão não é uma release estável.

Chat: [CHAT](docs/CHAT.md).

Clãs: [CLANS](docs/CLANS.md).

Títulos, seleções e menu: [TITLES](docs/TITLES.md).

Requisitos completos: [REQUIREMENTS](docs/REQUIREMENTS.md).
Estado e evidências: [PROJECT_STATUS](docs/PROJECT_STATUS.md).

## Desenvolvimento

```sh
bash ./gradlew --no-daemon build
# Na nuvem, com JDK/proxy/CA preparados:
python3 tools/gradle_cloud.py build
# Servidor de desenvolvimento (a EULA deve ser aceita pelo responsável):
python3 tools/gradle_cloud.py runServer --args=nogui
# Teste automatizado em servidor isolado, com aceitação explícita da EULA:
python3 tools/server_smoke.py --accept-eula
```

Artefato: `build/libs/shirokai-core-0.6.0-dev.jar`. Requer Fabric API;
o driver SQLite é incluído. LuckPerms é opcional nesta etapa: sem ele, somente
administradores vanilla nível 4 recebem acesso aos comandos administrativos.
Não há concessão automática de permissões de gameplay.

Configuração e comandos: [CONFIGURATION](docs/CONFIGURATION.md),
[PERMISSIONS](docs/PERMISSIONS.md). Continuidade: [NEXT_STEPS](docs/NEXT_STEPS.md).
