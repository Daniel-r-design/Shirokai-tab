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
    @Test void groupWeightAndContextualMetadataComeFromRealApiContract(){
        var meta=api(net.luckperms.api.cacheddata.CachedMetaData.class,method->switch(method){case "getPrefix"->"[VIP] ";case "getSuffix"->null;default->throw new AssertionError(method);});
        var cached=api(CachedDataManager.class,method->{assertEquals("getMetaData",method);return meta;});
        var user=api(User.class,method->switch(method){case "getPrimaryGroup"->"vip";case "getCachedData"->cached;default->throw new AssertionError(method);});
        var loaded=new AtomicReference<>(user);
        var users=api(UserManager.class,method->{assertEquals("getUser",method);return loaded.get();});
        var group=api(net.luckperms.api.model.group.Group.class,method->{assertEquals("getWeight",method);return java.util.OptionalInt.of(100);});
        var groups=api(net.luckperms.api.model.group.GroupManager.class,method->{assertEquals("getGroup",method);return group;});
        var luckPerms=api(LuckPerms.class,method->switch(method){case "getUserManager"->users;case "getGroupManager"->groups;default->throw new AssertionError(method);});
        var service=new LuckPermsPermissions(luckPerms);UUID player=UUID.randomUUID();var result=service.metadata(player);
        assertEquals("vip",result.group());assertEquals("[VIP] ",result.prefix());assertEquals("",result.suffix());assertEquals(100,result.weight());
        loaded.set(null);assertNull(service.metadata(player).group());
    }
}
