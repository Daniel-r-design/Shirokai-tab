package com.shirokai.core.permissions;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.util.Tristate;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.*;

class LuckPermsPermissionsTest {
    private static <T> T api(Class<T> type, Function<String, Object> result) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> result.apply(method.getName())));
    }
    @Test void permissionLossIsObservedAndUnloadedUsersAreDenied() {
        var value = new AtomicReference<>(Tristate.TRUE);
        CachedPermissionData permissionData = api(CachedPermissionData.class, method -> {
            assertEquals("checkPermission", method); return value.get();
        });
        CachedDataManager data = api(CachedDataManager.class, method -> {
            assertEquals("getPermissionData", method); return permissionData;
        });
        User user = api(User.class, method -> { assertEquals("getCachedData", method); return data; });
        var loaded = new AtomicReference<>(user);
        UserManager users = api(UserManager.class, method -> { assertEquals("getUser", method); return loaded.get(); });
        LuckPerms api = api(LuckPerms.class, method -> { assertEquals("getUserManager", method); return users; });
        var service = new LuckPermsPermissions(api); UUID player = UUID.randomUUID();
        assertTrue(service.allowed(player, "shirokaicore.command.reload"));
        value.set(Tristate.FALSE);
        assertFalse(service.allowed(player, "shirokaicore.command.reload"));
        value.set(Tristate.UNDEFINED);
        assertFalse(service.allowed(player, "shirokaicore.command.reload"));
        loaded.set(null);
        assertFalse(service.allowed(player, "shirokaicore.command.reload"));
    }
}
