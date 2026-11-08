package com.shirokai.core.config;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Set;

/** Disk reads happen on the caller's IO executor; never replaces custom configuration. */
public final class ConfigService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    public ConfigService(Path file) { this.file = file; }
    public Path file() { return file; }
    public CoreConfig load() throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        if (!Files.exists(file)) {
            try { Files.writeString(file, GSON.toJson(CoreConfig.defaults()) + "\n", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW); }
            catch (FileAlreadyExistsException ignored) { /* Another writer won; read their file. */ }
        }
        if (Files.size(file) > 65536) throw new IOException(file + ": arquivo excede 64 KiB");
        try { return parse(Files.readString(file, StandardCharsets.UTF_8)); }
        catch (RuntimeException | IOException failure) {
            throw new IOException(file + ": " + failure.getMessage(), failure);
        }
    }
    public static CoreConfig parse(String json) throws IOException {
        JsonElement value;
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            reader.setLenient(false);
            value = GSON.getAdapter(JsonElement.class).read(reader);
            if (reader.peek() != JsonToken.END_DOCUMENT) throw new IllegalArgumentException("Conteúdo após JSON");
        }
        if (value == null || !value.isJsonObject()) throw new IllegalArgumentException("Esperado objeto JSON");
        JsonObject root = value.getAsJsonObject();
        checkFields(root, Set.of("schemaVersion", "modules", "databaseFile", "unavailablePlaceholder"));
        CoreConfig defaults = CoreConfig.defaults();
        if (!root.has("schemaVersion") || !root.get("schemaVersion").toString().equals("1"))
            throw new IllegalArgumentException("schemaVersion deve ser 1; migração de outra versão não suportada");
        var modules = new HashMap<>(defaults.modules());
        if (root.has("modules")) {
            if (!root.get("modules").isJsonObject()) throw new IllegalArgumentException("modules deve ser objeto");
            JsonObject entries = root.getAsJsonObject("modules");
            checkFields(entries, CoreConfig.MODULES);
            entries.entrySet().forEach(entry -> {
                if (!entry.getValue().isJsonPrimitive() || !entry.getValue().getAsJsonPrimitive().isBoolean())
                    throw new IllegalArgumentException("modules." + entry.getKey() + " deve ser booleano");
                modules.put(entry.getKey(), entry.getValue().getAsBoolean());
            });
        }
        String db = string(root, "databaseFile", defaults.databaseFile());
        if (!db.matches("[a-zA-Z0-9][a-zA-Z0-9_.-]{0,63}\\.db") || db.contains(".."))
            throw new IllegalArgumentException("databaseFile deve ser um nome .db, sem diretórios");
        String fallback = string(root, "unavailablePlaceholder", defaults.unavailablePlaceholder());
        if (fallback.length() > 256 || fallback.codePoints().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("unavailablePlaceholder inválido");
        return new CoreConfig(1, modules, db, fallback);
    }
    private static String string(JsonObject root, String key, String fallback) {
        if (!root.has(key)) return fallback;
        JsonElement element = root.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString())
            throw new IllegalArgumentException(key + " deve ser texto");
        return element.getAsString();
    }
    private static void checkFields(JsonObject object, Set<String> allowed) {
        for (String key : object.keySet()) if (!allowed.contains(key))
            throw new IllegalArgumentException("Campo desconhecido: " + key);
    }
}
