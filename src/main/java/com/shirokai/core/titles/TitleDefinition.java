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
        // Native clan membership integration will provide these titles in the clans phase.
        if (category == Category.CLAN) throw new IllegalArgumentException("Títulos de clã exigem módulo de clãs ainda não implementado");
    }
}
