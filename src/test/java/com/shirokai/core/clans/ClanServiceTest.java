package com.shirokai.core.clans;

import com.shirokai.core.permissions.PermissionService;
import com.shirokai.core.storage.SqliteStorage;
import com.google.gson.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ClanServiceTest {
    @TempDir Path directory;
    private static ClanConfig config()throws Exception{
        var object=new Gson().toJsonTree(ClanConfig.defaults()).getAsJsonObject();object.addProperty("defaultLimit",1);object.addProperty("creationCooldownSeconds",0);
        var limits=new JsonObject();limits.addProperty("shirokaicore.clan.limit.3",3);object.add("permissionLimits",limits);return ClanConfig.parse(object.toString());
    }
    @Test void creationChecksPermissionAndPlaytimeBeforeAnyMutation()throws Exception{
        try(var storage=new SqliteStorage(directory.resolve("data.db"))){
            var repository=new ClanRepository(storage,new ClanRepositoryTest.Time());
            var permissions=new PermissionService(){public boolean allowed(UUID id,String node){return false;}public String provider(){return "test";}};
            var service=new ClanService(repository,permissions,Runnable::run,config());
            assertThrows(IllegalStateException.class,()->service.create(UUID.randomUUID(),"FreeClan",0));
            var object=new Gson().toJsonTree(config()).getAsJsonObject();object.addProperty("minimumPlaySeconds",120);
            var allowed=new PermissionService(){public boolean allowed(UUID id,String node){return true;}public String provider(){return "test";}};
            var gated=new ClanService(repository,allowed,Runnable::run,ClanConfig.parse(object.toString()));assertThrows(IllegalStateException.class,()->gated.create(UUID.randomUUID(),"FreeClan",119));
            assertThrows(ExecutionException.class,()->repository.find("FreeClan").get());
            assertEquals("FreeClan",gated.create(UUID.randomUUID(),"FreeClan",120).get().name());
        }
    }
    @Test void vipExpiryAndLeadershipChangeRecalculateLimitWithoutExpulsion()throws Exception{
        var server=Executors.newSingleThreadExecutor(r->new Thread(r,"clan-test-server"));
        try(var storage=new SqliteStorage(directory.resolve("data.db"))){
            var elevated=new HashSet<UUID>();UUID leader=UUID.randomUUID(),a=UUID.randomUUID(),b=UUID.randomUUID(),c=UUID.randomUUID();
            var permissions=new PermissionService(){public boolean allowed(UUID id,String node){
                assertEquals("clan-test-server",Thread.currentThread().getName());return node.equals("shirokaicore.clan.create")||elevated.contains(id);
            }public String provider(){return "test";}};
            var repository=new ClanRepository(storage,new ClanRepositoryTest.Time());var service=new ClanService(repository,permissions,server,config());
            server.submit(()->elevated.add(leader)).get();
            server.submit(()->service.create(leader,"VipClan",0)).get().get();
            for(UUID member:List.of(a,b,c))server.submit(()->service.invite(leader,member)).get().get();
            for(UUID member:List.of(a,b))server.submit(()->service.accept(member,"VipClan")).get().get();
            server.submit(()->elevated.remove(leader)).get();
            assertThrows(ExecutionException.class,()->server.submit(()->service.accept(c,"VipClan")).get().get());
            assertEquals(3,service.own(leader).get().members().size());
            server.submit(()->elevated.add(leader)).get();
            server.submit(()->service.transfer(leader,a)).get().get();
            server.submit(()->service.answerTransfer(a,true)).get().get();
            assertEquals(1,server.submit(()->service.limit(a)).get());assertEquals(3,service.own(a).get().members().size());
            assertThrows(ExecutionException.class,()->server.submit(()->service.accept(c,"VipClan")).get().get());
            server.submit(()->elevated.add(a)).get();server.submit(()->service.leave(b)).get().get();
            assertEquals(3,server.submit(()->service.limit(a)).get());
            server.submit(()->service.accept(c,"VipClan")).get().get();assertEquals(3,service.own(a).get().members().size());
        }finally{server.shutdown();assertTrue(server.awaitTermination(10,TimeUnit.SECONDS));}
    }
    @Test void validatesIdentityAndPreservesCustomConfigOnFailure()throws Exception{
        Path path=directory.resolve("clans.json");assertEquals(ClanConfig.defaults(),ClanConfig.load(path));
        var config=ClanConfig.load(path);
        for(String name:List.of("ad","admin","BAD§Name","","x".repeat(33)))assertThrows(IllegalArgumentException.class,()->config.name(name));
        for(String tag:List.of("A","123456","A B","§a"))assertThrows(IllegalArgumentException.class,()->config.tag(tag));
        assertEquals("AKT",config.tag("AKT"));
        for(String text:List.of("null","[]","{schemaVersion:1}","{\"schemaVersion\":1,\"unknown\":true}","{\"schemaVersion\":1,\"defaultLimit\":0}","{\"schemaVersion\":1,\"creationEnabled\":\"true\"}"))assertThrows(Exception.class,()->ClanConfig.parse(text));
        Files.writeString(path,"broken");assertThrows(Exception.class,()->ClanConfig.load(path));assertEquals("broken",Files.readString(path));
    }
}
