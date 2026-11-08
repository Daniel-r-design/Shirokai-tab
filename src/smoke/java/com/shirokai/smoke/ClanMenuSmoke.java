package com.shirokai.smoke;

import com.shirokai.core.clans.*;
import com.shirokai.core.permissions.PermissionService;
import com.shirokai.core.storage.SqliteStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import static net.minecraft.server.command.CommandManager.literal;

/** Handler integration in a real server, using synthetic players with no client transport. */
public final class ClanMenuSmoke implements ModInitializer {
    private final Function<BooleanSupplier,CompletableFuture<Void>> until;
    private boolean running;
    public ClanMenuSmoke(Function<BooleanSupplier,CompletableFuture<Void>> until){this.until=until;}
    @Override public void onInitialize(){CommandRegistrationCallback.EVENT.register((dispatcher,registry,environment)->dispatcher.register(
        literal("shirokai-clan-menu-smoke").requires(s->s.hasPermissionLevel(4)).executes(context->{
            if(running)return 0;running=true;var source=context.getSource();test(source.getServer()).whenCompleteAsync((v,error)->{
                running=false;if(error==null)source.sendFeedback(()->Text.literal("CLAN_MENU_SMOKE_PASS"),false);
                else{org.slf4j.LoggerFactory.getLogger("shirokai-smoke").error("CLAN_MENU_SMOKE_FAIL",error);source.sendError(Text.literal("CLAN_MENU_SMOKE_FAIL"));}
            },source.getServer());return 1;
        })));}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    private static ClanMenu menu(ServerPlayerEntity player){return (ClanMenu)player.currentScreenHandler;}
    private static void click(ServerPlayerEntity player,int slot){menu(player).onSlotClick(slot,0,SlotActionType.PICKUP,player);}
    private static int memberSlot(ServerPlayerEntity owner,UUID target){
        for(int slot=9;slot<45;slot++){var name=menu(owner).getSlot(slot).getStack().get(DataComponentTypes.CUSTOM_NAME);if(name!=null&&name.getString().equals(target.toString()))return slot;}
        throw new AssertionError("Member not listed");
    }
    private CompletableFuture<Void> test(MinecraftServer server){
        var storage=new SqliteStorage(FabricLoader.getInstance().getGameDir().resolve("clan-menu-"+UUID.randomUUID()+".db"));
        var leader=ProfileSmoke.player(server,"ClanLeader");var member=ProfileSmoke.player(server,"ClanMember");
        UUID a=leader.getUuid(),b=member.getUuid();leader.getInventory().setStack(0,new ItemStack(Items.DIAMOND,5));
        var permissions=new PermissionService(){public boolean allowed(UUID player,String node){return true;}public String provider(){return "test-only";}};
        var clans=new ClanService(new ClanRepository(storage,Clock.systemUTC()),permissions,server,ClanConfig.defaults());clans.cacheOnline(id->true);
        var flow=storage.ready().thenComposeAsync(v->clans.create(a,"MenuClan",0),server)
            .thenComposeAsync(clan->clans.invite(a,b),server).thenComposeAsync(clan->{ClanMenu.open(member,clans);return until.apply(()->member.currentScreenHandler instanceof ClanMenu);},server)
            .thenComposeAsync(v->{click(member,22);check(menu(member).page()==ClanMenu.Page.INVITES,"Invite page missing");click(member,9);return until.apply(()->clans.cached(b).isPresent()&&menu(member).page()==ClanMenu.Page.HOME);},server)
            .thenComposeAsync(v->{ClanMenu.open(leader,clans);return until.apply(()->leader.currentScreenHandler instanceof ClanMenu);},server)
            .thenComposeAsync(v->{var menu=menu(leader);check(!menu.canUse(member),"Other player owns menu");
                for(var action:SlotActionType.values())if(action!=SlotActionType.PICKUP)menu.onSlotClick(40,0,action,leader);
                menu.onSlotClick(40,0,SlotActionType.PICKUP,member);menu.onSlotClick(-999,0,SlotActionType.PICKUP,leader);
                check(menu.quickMove(leader,4).isEmpty(),"Menu icon transferred");check(leader.getInventory().getStack(0).getCount()==5,"Inventory altered");
                click(leader,20);click(leader,memberSlot(leader,b));click(leader,20);
                return until.apply(()->clans.cached(b).orElseThrow().members().get(b)==ClanSnapshot.Role.OFFICER&&menu(leader).page()==ClanMenu.Page.HOME);
            },server).thenComposeAsync(v->{
                click(leader,20);click(leader,memberSlot(leader,b));click(leader,40);check(menu(leader).page()==ClanMenu.Page.TRANSFER,"Transfer confirmation missing");
                return clans.transferOffer(b);
            },server).thenComposeAsync(offer->{check(offer.isEmpty(),"Transfer requested before confirming");click(leader,22);return until.apply(()->menu(leader).page()==ClanMenu.Page.HOME);},server)
            .thenComposeAsync(v->{click(member,49);return until.apply(()->!menu(member).getSlot(46).getStack().isEmpty());},server)
            .thenComposeAsync(v->{click(member,46);return until.apply(()->clans.cached(b).orElseThrow().leader().equals(b)&&!menu(member).getSlot(40).getStack().isEmpty());},server)
            .thenComposeAsync(v->{click(member,40);return until.apply(()->menu(member).page()==ClanMenu.Page.DISBAND);},server)
            .thenComposeAsync(v->{check(clans.cached(b).isPresent(),"First click deleted clan");click(member,24);return until.apply(()->menu(member).page()==ClanMenu.Page.HOME);},server)
            .thenComposeAsync(v->clans.disband(b,true).handleAsync((value,error)->{check(error!=null,"Cancel retained disband confirmation");return null;},server),server)
            .thenComposeAsync(v->{click(member,40);return until.apply(()->menu(member).page()==ClanMenu.Page.DISBAND);},server)
            .thenComposeAsync(v->{click(member,22);return until.apply(()->clans.cached(b).isEmpty()&&menu(member).page()==ClanMenu.Page.HOME);},server)
            .thenRunAsync(()->{check(clans.cached(a).isEmpty(),"Disband left membership");check(leader.getInventory().getStack(0).getCount()==5,"Items changed");
                var closed=menu(member);click(member,50);check(member.currentScreenHandler!=closed&&!closed.canUse(member),"Menu remained usable after close");
            },server);
        return flow.whenCompleteAsync((v,error)->{leader.closeHandledScreen();member.closeHandledScreen();try{storage.close();}catch(Exception close){throw new CompletionException(close);}},server);
    }
}
