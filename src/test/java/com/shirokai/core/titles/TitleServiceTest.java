package com.shirokai.core.titles;

import com.shirokai.core.storage.SqliteStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class TitleServiceTest {
    @TempDir Path directory;
    @Test void actionsRevalidatePermissionsAndPersistRemovalOnServerExecutor() throws Exception {
        var server=Executors.newSingleThreadExecutor(r->new Thread(r,"test-server"));
        try(var storage=new SqliteStorage(directory.resolve("data.db"))) {
            UUID player=UUID.randomUUID();var permission=new AtomicBoolean(true);
            var role=TitleRulesTest.definition("vip",TitleDefinition.Category.ROLE,1,"group.vip");
            var service=new TitleService(storage,TitleRulesTest.catalog(role),(id,node)->{
                assertEquals("test-server",Thread.currentThread().getName());return permission.get();
            },server,id->true,Clock.systemUTC(),(message,error)->fail(error));
            server.submit(()->service.equip(player,"vip",false)).get().get();
            assertEquals(Optional.of("[vip]"),server.submit(()->service.placeholder(player,"shirokai:selected_title")).get());
            permission.set(false);
            assertThrows(ExecutionException.class,()->server.submit(()->service.equip(player,"vip",true)).get().get());
            var state=server.submit(()->service.view(player)).get().get();
            assertNull(state.selectedVisual());assertNull(storage.titleState(player).get().selectedVisual());
            assertTrue(server.submit(()->service.placeholder(player,"shirokai:selected_title")).get().isEmpty());
        } finally { server.shutdown();assertTrue(server.awaitTermination(10,TimeUnit.SECONDS)); }
    }
    @Test void offlineDiagnosticNeverErasesSelectionsBecauseLuckPermsUserIsUnloaded() throws Exception {
        var server=Executors.newSingleThreadExecutor();
        try(var storage=new SqliteStorage(directory.resolve("data.db"))) {
            UUID player=UUID.randomUUID();storage.saveTitles(new TitleState(player,0,Map.of(),"vip",List.of("vip"))).get();
            var role=TitleRulesTest.definition("vip",TitleDefinition.Category.ROLE,1,"group.vip");
            var service=new TitleService(storage,TitleRulesTest.catalog(role),(id,node)->false,server,id->false,Clock.systemUTC(),(m,e)->fail(e));
            server.submit(()->service.view(player)).get().get();
            assertEquals("vip",storage.titleState(player).get().selectedVisual());
            assertTrue(server.submit(()->service.cached(player)).get().isEmpty());
        } finally { server.shutdown();assertTrue(server.awaitTermination(10,TimeUnit.SECONDS)); }
    }
    @Test void expiredGrantCannotBeSelectedAndClearIsDurable() throws Exception {
        var server=Executors.newSingleThreadExecutor();
        try(var storage=new SqliteStorage(directory.resolve("data.db"))) {
            UUID player=UUID.randomUUID();var event=TitleRulesTest.definition("event",TitleDefinition.Category.SPECIAL,0,null);
            storage.grantTitle(player,"event",50,null).get();
            var service=new TitleService(storage,TitleRulesTest.catalog(event),(id,node)->false,server,id->true,Clock.fixed(Instant.ofEpochMilli(100),ZoneOffset.UTC),(m,e)->fail(e));
            assertThrows(ExecutionException.class,()->server.submit(()->service.equip(player,"event",false)).get().get());
            assertThrows(IllegalArgumentException.class,()->service.grant(null,player,"event",99));
            server.submit(()->service.grant(null,player,"event",0)).get().get();
            server.submit(()->service.equip(player,"event",false)).get().get();
            server.submit(()->service.equip(player,"event",true)).get().get();
            var cleared=server.submit(()->service.clear(player)).get().get();
            assertNull(cleared.selectedVisual());assertTrue(cleared.chatIds().isEmpty());assertTrue(cleared.grants().containsKey("event"));
        } finally { server.shutdown();assertTrue(server.awaitTermination(10,TimeUnit.SECONDS)); }
    }
}
