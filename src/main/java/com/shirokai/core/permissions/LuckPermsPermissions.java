package com.shirokai.core.permissions;

import java.util.UUID;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

/** Loaded only when LuckPerms is present. Queries current context-aware cached data each time. */
public final class LuckPermsPermissions implements PermissionService {
    private final LuckPerms api;
    public LuckPermsPermissions() { this(LuckPermsProvider.get()); }
    public LuckPermsPermissions(LuckPerms api) { this.api = java.util.Objects.requireNonNull(api); }
    @Override public boolean allowed(UUID player, String permission) {
        var user = api.getUserManager().getUser(player);
        return user != null && user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }
    @Override public java.util.concurrent.CompletableFuture<Void> prepare(UUID player) {
        if (api.getUserManager().getUser(player) != null) return java.util.concurrent.CompletableFuture.completedFuture(null);
        return api.getUserManager().loadUser(player).thenApply(user -> null);
    }
    @Override public String provider() { return "LuckPerms API 5.4"; }
}
