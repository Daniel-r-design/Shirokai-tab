package com.shirokai.core.titles;

public record TitleDefinition(String id, String name, String display, String description,
        Category category, int priority, String permission, boolean unlockedByDefault,
        boolean tab, boolean scoreboard, boolean chat) {
    public enum Category { ROLE, SPECIAL, CLAN }
    public TitleDefinition {
        if (id == null || !id.matches("[a-z0-9][a-z0-9_-]{0,63}")) throw new IllegalArgumentException("ID de título inválido");
        if (name == null || name.isBlank() || name.length() > 128 || display == null || display.length() > 128 || description == null || description.length() > 512)
            throw new IllegalArgumentException("Nome/display/descrição de título inválido: " + id);
        if (category == null) throw new IllegalArgumentException("Categoria ausente: " + id);
        if (permission != null && !permission.matches("[a-zA-Z0-9_.-]{1,128}")) throw new IllegalArgumentException("Permissão inválida: " + id);
        if (category == Category.ROLE && permission == null) throw new IllegalArgumentException("Cargo exige permissão LuckPerms: " + id);
        if (category == Category.CLAN && (!id.matches("clan_[0-9a-f-]{36}") || permission!=null || unlockedByDefault))
            throw new IllegalArgumentException("Título de clã deve ser derivado de associação nativa");
        if (category != Category.CLAN && id.startsWith("clan_")) throw new IllegalArgumentException("Prefixo clan_ reservado");
    }
}
