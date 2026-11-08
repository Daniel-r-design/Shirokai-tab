package com.shirokai.core.permissions;

import java.util.UUID;

public interface PermissionService {
    boolean allowed(UUID player, String permission);
    String provider();
}
