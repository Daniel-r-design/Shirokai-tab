package com.shirokai.smoke;

import com.google.gson.*;
import com.shirokai.core.clans.*;
import com.shirokai.core.permissions.PermissionService;
import com.shirokai.core.storage.SqliteStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.*;
import static net.minecraft.server.command.CommandManager.literal;

/** Real server + JDBC component integration; test permission provider, no authentication bypass. */
public final class ClanSmoke implements ModInitializer {
    private boolean running;
    @Override public void onInitialize(){
        CommandRegistrationCallback.EVENT.register((dispatcher,registry,environment)->dispatcher.register(literal("shirokai-clan-smoke").requires(s->s.hasPermissionLevel(4)).executes(context->{
            if(running)return 0;running=true;var source=context.getSource();var server=source.getServer();
            test(server).whenCompleteAsync((v,error)->{running=false;if(error==null)source.sendFeedback(()->Text.literal("CLAN_SMOKE_PASS"),false);else{org.slf4j.LoggerFactory.getLogger("shirokai-clan-smoke").error("CLAN_SMOKE_FAIL",error);source.sendError(Text.literal("CLAN_SMOKE_FAIL"));}},server);return 1;
        })));
    }
    private static void check(boolean valid,String message){if(!valid)throw new AssertionError(message);}
    private static <T> CompletableFuture<Void> rejects(CompletableFuture<T> future,MinecraftServer server){return future.handleAsync((value,error)->{check(error!=null,"Expected operation rejection");return null;},server);}
    private CompletableFuture<Void> test(MinecraftServer server){
        var storage=new SqliteStorage(FabricLoader.getInstance().getGameDir().resolve("clan-smoke-"+UUID.randomUUID()+".db"));
        UUID leader=UUID.randomUUID(),a=UUID.randomUUID(),b=UUID.randomUUID(),c=UUID.randomUUID();var vip=new HashSet<UUID>();
        var object=new Gson().toJsonTree(ClanConfig.defaults()).getAsJsonObject();object.addProperty("defaultLimit",2);var limits=new JsonObject();limits.addProperty("shirokaicore.clan.limit.3",3);object.add("permissionLimits",limits);
        final ClanConfig config;
        try{config=ClanConfig.parse(object.toString());}catch(Exception error){throw new CompletionException(error);}
        var permissions=new PermissionService(){public boolean allowed(UUID player,String node){check(server.isOnThread(),"Permission check outside server thread");return node.equals("shirokaicore.clan.create")||vip.contains(player);}public String provider(){return "test-only";}};
        var service=new ClanService(new ClanRepository(storage,Clock.systemUTC()),permissions,server,config);
        var result=storage.ready().thenComposeAsync(v->service.create(leader,"SmokeClan",0),server)
            .thenComposeAsync(clan->{check(clan.tag()==null,"Tag must be manual");return service.identity(leader,"SMK",true);},server)
            .thenComposeAsync(clan->service.invite(leader,a),server).thenComposeAsync(clan->service.accept(a,"SmokeClan"),server)
            .thenComposeAsync(clan->service.invite(leader,b),server).thenComposeAsync(clan->rejects(service.accept(b,"SmokeClan"),server),server)
            .thenComposeAsync(v->{vip.add(leader);return service.accept(b,"SmokeClan");},server)
            .thenComposeAsync(clan->{check(clan.members().size()==3,"VIP capacity not used");vip.remove(leader);return service.own(leader);},server)
            .thenComposeAsync(clan->{check(clan.members().size()==3&&service.limit(leader)==2,"VIP expiry removed members");return service.transfer(leader,a);},server)
            .thenComposeAsync(clan->{check(clan.leader().equals(leader),"Unconfirmed transfer changed leader");return service.answerTransfer(a,true);},server)
            .thenComposeAsync(clan->{check(clan.leader().equals(a)&&clan.members().size()==3,"Leadership transfer lost membership");return service.invite(a,c);},server)
            .thenComposeAsync(clan->rejects(service.accept(c,"SmokeClan"),server),server)
            .thenComposeAsync(v->service.leave(b),server).thenComposeAsync(clan->{vip.add(a);return service.accept(c,"SmokeClan");},server)
            .thenComposeAsync(clan->rejects(service.disband(a,true),server),server)
            .thenComposeAsync(v->service.disband(a,false),server).thenComposeAsync(clan->service.disband(a,true),server)
            .thenComposeAsync(clan->rejects(service.own(a),server),server).thenComposeAsync(v->rejects(service.own(leader),server),server)
            .thenComposeAsync(v->storage.integrityCheck(),server).thenAcceptAsync(value->check(value.equals("ok"),"SQLite integrity failed"),server);
        return result.whenCompleteAsync((v,error)->{try{storage.close();}catch(Exception close){throw new CompletionException(close);}},server);
    }
}
