package com.shirokai.core.storage;

import java.util.UUID;

public record PlayerPreferences(UUID playerId, boolean scoreboardEnabled) {
    public PlayerPreferences { java.util.Objects.requireNonNull(playerId); }
}
