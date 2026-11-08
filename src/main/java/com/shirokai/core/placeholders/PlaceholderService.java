package com.shirokai.core.placeholders;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.*;

/** Literal, nonrecursive substitution. This is not a MiniMessage parser or a PAPI adapter. */
public final class PlaceholderService {
    public record Context(UUID playerId, String playerName, int online) { }
    private static final Pattern TOKEN = Pattern.compile("%([a-z0-9_]+:[a-z0-9_]+)%");
    private final Map<String, Function<Context, Optional<String>>> resolvers;
    private final BiConsumer<String, Exception> errors;
    public PlaceholderService(BiConsumer<String, Exception> errors) {
        this.errors = Objects.requireNonNull(errors);
        resolvers = new HashMap<>(Map.of(
            "shirokai:online", context -> Optional.of(Integer.toString(context.online())),
            "shirokai:player", context -> Optional.ofNullable(context.playerName()),
            "shirokai:uuid", context -> Optional.ofNullable(context.playerId()).map(UUID::toString)));
    }
    public void register(String key, Function<Context, Optional<String>> resolver) {
        if (!key.matches("[a-z0-9_]+:[a-z0-9_]+") || resolvers.putIfAbsent(key, Objects.requireNonNull(resolver)) != null)
            throw new IllegalArgumentException("Placeholder inválido ou duplicado: " + key);
    }
    public Set<String> keys() { return Set.copyOf(resolvers.keySet()); }
    public String render(String template, Context context, String fallback) {
        Objects.requireNonNull(context); Objects.requireNonNull(fallback);
        if (template.length() > 8192) throw new IllegalArgumentException("Template excede 8192 caracteres");
        Matcher matcher = TOKEN.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1); String value = fallback;
            var resolver = resolvers.get(key);
            if (resolver != null) {
                try { value = resolver.apply(context).orElse(fallback); }
                catch (Exception failure) { errors.accept(key, failure); }
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        return matcher.appendTail(result).toString();
    }
}
