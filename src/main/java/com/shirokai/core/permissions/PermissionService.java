package com.shirokai.core.permissions;

import java.util.UUID;

public interface PermissionService {
    boolean allowed(UUID player, String permission);
    String provider();
    record Metadata(String group,String prefix,String suffix,int weight){public static Metadata unavailable(){return new Metadata(null,"","",0);}}
    default Metadata metadata(UUID player){return Metadata.unavailable();}
    default java.util.concurrent.CompletableFuture<Void> prepare(UUID player) {
        return java.util.concurrent.CompletableFuture.completedFuture(null);
    }
}
