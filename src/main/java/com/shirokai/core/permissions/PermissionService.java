package com.shirokai.core.permissions;

import java.util.UUID;

public interface PermissionService {
    boolean allowed(UUID player, String permission);
    String provider();
    default java.util.concurrent.CompletableFuture<Void> prepare(UUID player) {
        return java.util.concurrent.CompletableFuture.completedFuture(null);
    }
}
