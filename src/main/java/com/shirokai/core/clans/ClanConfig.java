package com.shirokai.core.clans;

import com.google.gson.*;
import com.google.gson.stream.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

public record ClanConfig(int schemaVersion, boolean creationEnabled, long minimumPlaySeconds,
        long creationCooldownSeconds, long nameCooldownSeconds, long tagCooldownSeconds,
        int minimumNameLength, int maximumNameLength, int minimumTagLength, int maximumTagLength,
        String namePattern, String tagPattern, Set<String> reservedNames, int defaultLimit,
        Map<String,Integer> permissionLimits, long invitationSeconds, long invitationCooldownSeconds,
        int maximumInvitations, long confirmationSeconds) {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public ClanConfig {
        reservedNames = Set.copyOf(Objects.requireNonNull(reservedNames));
        permissionLimits = Map.copyOf(Objects.requireNonNull(permissionLimits));
        if (schemaVersion != 1 || minimumPlaySeconds < 0 || creationCooldownSeconds < 0 || nameCooldownSeconds < 0 || tagCooldownSeconds < 0
                || minimumNameLength < 1 || maximumNameLength > 64 || maximumNameLength < minimumNameLength
                || minimumTagLength < 1 || maximumTagLength > 16 || maximumTagLength < minimumTagLength
                || defaultLimit < 1 || defaultLimit > 1000 || invitationSeconds < 1 || invitationCooldownSeconds < 0
                || maximumInvitations < 1 || maximumInvitations > 1000 || confirmationSeconds < 1
                || Math.max(Math.max(creationCooldownSeconds,nameCooldownSeconds),Math.max(tagCooldownSeconds,invitationSeconds)) > 31536000
                || invitationCooldownSeconds > 31536000 || confirmationSeconds > 3600 || minimumPlaySeconds > 31536000)
            throw new IllegalArgumentException("Configuração de clãs fora dos limites");
        Pattern.compile(Objects.requireNonNull(namePattern)); Pattern.compile(Objects.requireNonNull(tagPattern));
        permissionLimits.forEach((node,limit) -> {
            if (!node.matches("[a-z0-9_.-]{1,128}") || limit < 1 || limit > 1000) throw new IllegalArgumentException("Limite por permissão inválido");
        });
    }
    public static ClanConfig defaults() {
        return new ClanConfig(1,true,0,86400,604800,604800,3,32,2,5,
                "[A-Za-z0-9 _-]+","[A-Za-z0-9]+",Set.of("admin","staff","shirokai"),20,
                Map.of("shirokaicore.clan.limit.30",30,"shirokaicore.clan.limit.40",40,"shirokaicore.clan.limit.50",50),300,30,20,60);
    }
    public String name(String value) { return validate(value,minimumNameLength,maximumNameLength,namePattern); }
    public String tag(String value) { return validate(value,minimumTagLength,maximumTagLength,tagPattern); }
    private String validate(String value,int min,int max,String pattern) {
        value = Objects.requireNonNull(value).strip();
        if (value.length() < min || value.length() > max || !value.matches(pattern) || value.codePoints().anyMatch(Character::isISOControl)
                || reservedNames.stream().anyMatch(value::equalsIgnoreCase)) throw new IllegalArgumentException("Nome/sigla inválido ou reservado");
        return value;
    }
    public static String key(String value) { return value.toLowerCase(Locale.ROOT); }
    public static ClanConfig load(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        if (!Files.exists(file)) try { Files.writeString(file,GSON.toJson(defaults())+"\n",StandardOpenOption.CREATE_NEW); }
        catch (FileAlreadyExistsException ignored) { }
        if (Files.size(file)>65536) throw new IOException("clans.json excede64KiB");
        try { return parse(Files.readString(file)); } catch (Exception error) { throw new IOException(file+": "+error.getMessage(),error); }
    }
    public static ClanConfig parse(String text) throws IOException {
        JsonElement value;
        try(var reader=new JsonReader(new StringReader(text))) {
            reader.setLenient(false);value=GSON.getAdapter(JsonElement.class).read(reader);
            if(reader.peek()!=JsonToken.END_DOCUMENT)throw new IllegalArgumentException("Conteúdo após JSON");
        }
        if(value==null || !value.isJsonObject())throw new IllegalArgumentException("Esperado objeto de configuração de clãs");
        var object=value.getAsJsonObject();var defaults=GSON.toJsonTree(defaults()).getAsJsonObject();
        for(String key:object.keySet()) {
            if(!defaults.has(key))throw new IllegalArgumentException("Campo desconhecido: "+key);
            var expected=defaults.get(key);var supplied=object.get(key);
            if(supplied.isJsonNull() || expected.isJsonPrimitive() && (!supplied.isJsonPrimitive()
                    || expected.getAsJsonPrimitive().isBoolean()!=supplied.getAsJsonPrimitive().isBoolean()
                    || expected.getAsJsonPrimitive().isString()!=supplied.getAsJsonPrimitive().isString()
                    || expected.getAsJsonPrimitive().isNumber() && !supplied.toString().matches("[0-9]{1,9}")))
                throw new IllegalArgumentException("Tipo inválido: "+key);
            defaults.add(key,supplied);
        }
        if(!object.has("schemaVersion"))throw new IllegalArgumentException("schemaVersion obrigatório");
        if(!defaults.get("reservedNames").isJsonArray() || !defaults.get("permissionLimits").isJsonObject())throw new IllegalArgumentException("reservedNames/permissionLimits inválidos");
        for(var name:defaults.getAsJsonArray("reservedNames"))if(!name.isJsonPrimitive()||!name.getAsJsonPrimitive().isString())throw new IllegalArgumentException("Nome reservado inválido");
        for(var limit:defaults.getAsJsonObject("permissionLimits").entrySet())if(!limit.getValue().toString().matches("[0-9]{1,4}"))throw new IllegalArgumentException("Limite inválido");
        return GSON.fromJson(defaults,ClanConfig.class);
    }
}
