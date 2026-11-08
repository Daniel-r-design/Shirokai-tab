package com.shirokai.core;

import com.shirokai.core.config.*;
import com.shirokai.core.titles.*;
import com.shirokai.core.module.*;
import com.shirokai.core.permissions.*;
import com.shirokai.core.placeholders.PlaceholderService;
import com.shirokai.core.storage.SqliteStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CoreRuntime implements AutoCloseable {
    @FunctionalInterface private interface CheckedAction { void run() throws Exception; }
    private final MinecraftServer server;
    private final ConfigService configs;
    private final ModuleManager modules;
    private final ExecutorService io = Executors.newSingleThreadExecutor(task -> new Thread(task, "shirokai-config"));
    private final AtomicBoolean reloading = new AtomicBoolean();
    private final Logger logger;
    private volatile CoreConfig config;
    private volatile boolean closed;
    private SqliteStorage storage;
    private PermissionService permissions;
    private PlaceholderService placeholders;
    private TitleService titles;
    private int titleTicks;
    private final Path dataDirectory;

    public CoreRuntime(MinecraftServer server, Logger logger) throws Exception {
        this.server = server; this.logger = logger;
        dataDirectory = FabricLoader.getInstance().getGameDir().resolve("shirokai-core");
        configs = new ConfigService(FabricLoader.getInstance().getConfigDir().resolve("shirokai-core/core.json"));
        config = configs.load();
        modules = new ModuleManager((id, error) -> logger.error("[{}] Lifecycle falhou", id, error));
        modules.register(module("storage", Set.of(), () -> {
            storage = new SqliteStorage(dataDirectory.resolve(config.databaseFile()));
            storage.ready().whenComplete((ignored, error) -> {
                if (error == null) logger.info("[storage] SQLite pronto: {}", config.databaseFile());
                else logger.error("[storage] SQLite indisponível; demais módulos independentes continuam", error);
            });
        }, () -> { if (storage != null) storage.close(); }));
        modules.register(module("permissions", Set.of(), () -> {
            if (FabricLoader.getInstance().isModLoaded("luckperms")) {
                try { permissions = new LuckPermsPermissions(); }
                catch (LinkageError | IllegalStateException failure) {
                    throw new Exception("LuckPerms presente mas API indisponível/incompatível", failure);
                }
            } else {
                permissions = new PermissionService() {
                    public boolean allowed(java.util.UUID player, String permission) { return false; }
                    public String provider() { return "vanilla-op4 (admin fallback only)"; }
                };
                logger.warn("[permissions] LuckPerms ausente: apenas administração vanilla nível 4 disponível");
            }
        }, () -> {}));
        modules.register(module("placeholders", Set.of(), () -> placeholders = new PlaceholderService(
                (key, error) -> logger.warn("[placeholders] Falha em {}", key, error)), () -> {}));
        modules.register(module("titles", Set.of("storage", "permissions"), () -> {
            var path = FabricLoader.getInstance().getConfigDir().resolve("shirokai-core/titles.json");
            var catalog = TitleCatalog.load(path);
            titles = new TitleService(storage, catalog, permissions::allowed, server,
                    id -> server.getPlayerManager().getPlayer(id) != null, java.time.Clock.systemUTC(),
                    (message, error) -> logger.warn("[titles] " + message, error));
            if (placeholders != null) {
                for (String key : java.util.List.of("shirokai:selected_title", "shirokai:chat_tag_1", "shirokai:chat_tag_2"))
                    placeholders.register(key, context -> titles.placeholder(context.playerId(), key));
            }
        }, () -> {}));
        modules.register(module("profiles", Set.of("titles"), () -> {}, () -> {}));
        modules.register(module("administration", Set.of("permissions"), () -> {}, () -> {}));
        for (String id : new String[]{"storage", "permissions", "placeholders", "titles", "profiles", "administration"}) {
            modules.start(id, config.enabled(id));
            logger.info("[{}] {}", id, modules.states().get(id));
        }
    }
    private static CoreModule module(String id, Set<String> dependencies, CheckedAction start, CheckedAction stop) {
        return new CoreModule() {
            public String id() { return id; }
            public Set<String> dependencies() { return dependencies; }
            public void start() throws Exception { start.run(); }
            public void stop() throws Exception { stop.run(); }
        };
    }
    public TitleService titles() {
        if (!running("titles") || titles == null) throw new IllegalStateException("Módulo titles indisponível");
        return titles;
    }
    public void playerJoined(java.util.UUID player) {
        if (running("titles")) titles.view(player).whenComplete((value, error) -> {
            if (error != null) logger.warn("[titles] Falha ao carregar perfil " + player, error);
        });
    }
    public void playerLeft(java.util.UUID player) { if (titles != null) titles.forget(player); }
    public void tick() {
        if (running("titles") && ++titleTicks >= 100) { titleTicks = 0; titles.revalidateCached(); }
    }
    public CoreConfig config() { return config; }
    public ModuleManager modules() { return modules; }
    public PermissionService permissions() { return permissions; }
    public PlaceholderService placeholders() { return placeholders; }
    public Path dataDirectory() { return dataDirectory; }
    public SqliteStorage storage() {
        if (storage == null || !running("storage")) throw new IllegalStateException("Módulo storage indisponível");
        return storage;
    }
    public boolean running(String id) { return modules.states().get(id) == ModuleManager.State.RUNNING; }
    public String storageStatus() {
        if (storage == null) return "DISABLED";
        var ready = storage.ready();
        return ready.isCompletedExceptionally() ? "FAILED" : ready.isDone() ? "READY (fila=" + storage.pendingOperations() + ")" : "STARTING";
    }
    /** Parse on IO worker; publish only validated immutable config on server thread. */
    public CompletableFuture<Void> reload() {
        if (closed || !reloading.compareAndSet(false, true))
            return CompletableFuture.failedFuture(new IllegalStateException("Reload indisponível ou já em andamento"));
        var result = new CompletableFuture<Void>();
        CompletableFuture.supplyAsync(() -> {
            try {
                CoreConfig core = configs.load();
                TitleCatalog catalog = titles == null ? null : TitleCatalog.load(
                        FabricLoader.getInstance().getConfigDir().resolve("shirokai-core/titles.json"));
                return new ReloadCandidate(core, catalog);
            } catch (Exception failure) { throw new CompletionException(failure); }
        }, io).whenComplete((candidate, error) -> {
            if (closed) { reloading.set(false); result.completeExceptionally(new IllegalStateException("Servidor encerrando")); return; }
            server.execute(() -> {
                try {
                    if (closed) throw new IllegalStateException("Servidor encerrando");
                    if (error != null) throw new CompletionException(error);
                    if (candidate.core().requiresRestartComparedTo(config))
                        throw new IllegalStateException("Alteração de módulos ou databaseFile exige reinício; configuração ativa preservada");
                    config = candidate.core();
                    if (titles != null) titles.replaceCatalog(candidate.titles());
                    logger.info("[core] Configuração recarregada com segurança");
                    result.complete(null);
                } catch (Exception failure) { result.completeExceptionally(failure); }
                finally { reloading.set(false); }
            });
        });
        return result;
    }
    private record ReloadCandidate(CoreConfig core, TitleCatalog titles) {}
    @Override public void close() throws Exception {
        closed = true;
        io.shutdown();
        modules.close();
        if (!io.awaitTermination(10, TimeUnit.SECONDS)) logger.warn("[core] Worker de configuração ainda encerrando");
        logger.info("[core] Encerramento concluído: {}", modules.states());
    }
}
