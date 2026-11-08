package com.shirokai.core.administration;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.shirokai.core.CoreRuntime;
import com.shirokai.core.titles.TitleState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import static net.minecraft.server.command.CommandManager.*;

public final class TitleAdminCommands {
    private final Supplier<CoreRuntime> runtime;
    private final BiPredicate<ServerCommandSource, String> allowed;
    public TitleAdminCommands(Supplier<CoreRuntime> runtime, BiPredicate<ServerCommandSource, String> allowed) { this.runtime = runtime; this.allowed = allowed; }
    public LiteralArgumentBuilder<ServerCommandSource> node() {
        return literal("titles").requires(s -> allowed.test(s, "titles.inspect") || allowed.test(s, "titles.grant") || allowed.test(s, "titles.revoke"))
            .then(literal("list").requires(s -> allowed.test(s, "titles.inspect")).executes(c -> {
                c.getSource().sendFeedback(() -> Text.literal("Títulos: " + runtime.get().titles().catalog().definitions().keySet()), false); return 1;
            }))
            .then(literal("inspect").requires(s -> allowed.test(s, "titles.inspect")).then(argument("uuid", StringArgumentType.word()).executes(c ->
                execute(c.getSource(), () -> runtime.get().storage().titleState(UUID.fromString(StringArgumentType.getString(c, "uuid")))))))
            .then(literal("grant").requires(s -> allowed.test(s, "titles.grant")).then(argument("uuid", StringArgumentType.word())
                .then(argument("id", StringArgumentType.word()).suggests((c,b) -> suggest(b))
                    .executes(c -> execute(c.getSource(), () -> grant(c.getSource(), StringArgumentType.getString(c,"uuid"), StringArgumentType.getString(c,"id"), 0)))
                    .then(argument("seconds", IntegerArgumentType.integer(1, 31536000)).executes(c -> execute(c.getSource(), () -> grant(c.getSource(),
                        StringArgumentType.getString(c,"uuid"), StringArgumentType.getString(c,"id"), IntegerArgumentType.getInteger(c,"seconds"))))))))
            .then(literal("revoke").requires(s -> allowed.test(s, "titles.revoke")).then(argument("uuid", StringArgumentType.word())
                .then(argument("id", StringArgumentType.word()).suggests((c,b) -> suggest(b)).executes(c -> execute(c.getSource(), () -> runtime.get().titles().revoke(
                    actor(c.getSource()), UUID.fromString(StringArgumentType.getString(c,"uuid")), StringArgumentType.getString(c,"id")))))));
    }
    private CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggest(com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        for (String id : runtime.get().titles().catalog().definitions().keySet()) if (id.startsWith(builder.getRemainingLowerCase())) builder.suggest(id);
        return builder.buildFuture();
    }
    private CompletableFuture<TitleState> grant(ServerCommandSource source, String uuid, String id, int seconds) {
        long expiry = seconds == 0 ? 0 : runtime.get().titles().now() + seconds * 1000L;
        return runtime.get().titles().grant(actor(source), UUID.fromString(uuid), id, expiry);
    }
    private static UUID actor(ServerCommandSource source) { return source.getPlayer() == null ? null : source.getPlayer().getUuid(); }
    private int execute(ServerCommandSource source, Supplier<CompletableFuture<TitleState>> operation) {
        try {
            operation.get().whenCompleteAsync((state, error) -> {
                if (error != null) failure(source, error);
                else source.sendFeedback(() -> Text.literal("Perfil " + state.player() + " revision=" + state.revision() + " selected=" + state.selectedVisual() + " chat=" + state.chatIds() + " grants=" + state.grants()), false);
            }, source.getServer());
            source.sendFeedback(() -> Text.literal("Operação de título iniciada"), false); return 1;
        } catch (Exception error) { failure(source, error); return 0; }
    }
    private static void failure(ServerCommandSource source, Throwable error) {
        while (error.getCause() != null) error = error.getCause();
        source.sendError(Text.literal("Títulos: " + error.getMessage()));
    }
}
