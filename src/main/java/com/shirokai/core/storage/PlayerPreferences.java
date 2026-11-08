package com.shirokai.core.storage;

import java.util.UUID;

public record PlayerPreferences(UUID playerId, boolean scoreboardEnabled, boolean animationsEnabled) {
    public PlayerPreferences(UUID playerId,boolean scoreboardEnabled){this(playerId,scoreboardEnabled,true);}
    public PlayerPreferences { java.util.Objects.requireNonNull(playerId); }
}
