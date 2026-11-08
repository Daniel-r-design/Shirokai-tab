package com.shirokai.core.clans;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.shirokai.core.CoreRuntime;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import static net.minecraft.server.command.CommandManager.*;

public final class ClanCommands {
    private final Supplier<CoreRuntime> runtime;
    public ClanCommands(Supplier<CoreRuntime> runtime){this.runtime=runtime;}
    public void register(CommandDispatcher<ServerCommandSource> dispatcher){
        if(dispatcher.getRoot().getChild("clan")!=null){org.slf4j.LoggerFactory.getLogger("shirokai-core").warn("[clans] /clan já existe; comando do Core não registrado");return;}
        var root=literal("clan").executes(c->openMenu(c.getSource()));
        root.then(literal("menu").executes(c->openMenu(c.getSource())));
        root.then(literal("info").executes(c->execute(c.getSource(),(service,id)->service.own(id))));
        root.then(literal("members").executes(c->execute(c.getSource(),(service,id)->service.own(id))));
        root.then(literal("create").then(argument("name",StringArgumentType.greedyString()).executes(c->execute(c.getSource(),(service,id)->{
            var player=c.getSource().getPlayer();
            long seconds=player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME))/20L;
            return service.create(id,StringArgumentType.getString(c,"name"),seconds);
        }))));
        for(String action:new String[]{"rename","tag","accept","decline"})root.then(literal(action).then(argument("value",StringArgumentType.greedyString()).executes(c->execute(c.getSource(),(service,id)->{
            String value=StringArgumentType.getString(c,"value");return switch(action){
                case "rename" -> service.identity(id,value,false);case "tag" -> service.identity(id,value,true);
                case "accept" -> service.accept(id,value);default -> service.decline(id,value);};
        }))));
        for(String action:new String[]{"invite","cancel","kick","promote","demote","transfer"})root.then(literal(action).then(argument("player",StringArgumentType.word()).suggests((c,b)->{
            for(var player:c.getSource().getServer().getPlayerManager().getPlayerList())if(player.getGameProfile().getName().toLowerCase(java.util.Locale.ROOT).startsWith(b.getRemainingLowerCase()))b.suggest(player.getGameProfile().getName());return b.buildFuture();
        }).executes(c->execute(c.getSource(),(service,id)->{
            String value=StringArgumentType.getString(c,"player");UUID target;
            try{target=UUID.fromString(value);}catch(IllegalArgumentException invalid){var player=c.getSource().getServer().getPlayerManager().getPlayer(value);if(player==null)throw new IllegalArgumentException("Jogador offline: informe seu UUID");target=player.getUuid();}
            var result = switch(action){case "invite"->service.invite(id,target);case "cancel"->service.cancel(id,target);case "transfer"->service.transfer(id,target);default->service.memberAction(id,target,action);};
            UUID recipient = target;
            return result.thenApplyAsync(clan -> {
                var online=c.getSource().getServer().getPlayerManager().getPlayer(recipient);
                if (online != null && action.equals("invite")) online.sendMessage(Text.literal("Convite do clã "+clan.name()+". Use /clan accept "+clan.name()+" ou /clan decline "+clan.name()),false);
                if (online != null && action.equals("transfer")) online.sendMessage(Text.literal("Transferência de liderança solicitada. Use /clan transferaccept ou /clan transferdecline."),false);
                return clan;
            },c.getSource().getServer());
        }))));
        root.then(literal("leave").executes(c->execute(c.getSource(),(service,id)->service.leave(id))));
        root.then(literal("transferaccept").executes(c->execute(c.getSource(),(service,id)->service.answerTransfer(id,true))));
        root.then(literal("transferdecline").executes(c->execute(c.getSource(),(service,id)->service.answerTransfer(id,false))));
        root.then(literal("disband").executes(c->execute(c.getSource(),(service,id)->service.disband(id,false).thenApplyAsync(clan -> {
            c.getSource().sendFeedback(()->Text.literal("Confirme com /clan disband confirm em "+service.config().confirmationSeconds()+" segundos."),false);return clan;
        },c.getSource().getServer())))
            .then(literal("cancel").executes(c->execute(c.getSource(),(service,id)->service.cancelDisband(id))))
            .then(literal("confirm").executes(c->execute(c.getSource(),(service,id)->service.disband(id,true)))));
        root.then(literal("invites").executes(c -> {
            var source=c.getSource();var core=runtime.get();
            if(source.getPlayer()==null || core==null || !core.running("clans")){source.sendError(Text.literal("Clãs exigem jogador e módulo ativo"));return 0;}
            core.clans().invitations(source.getPlayer().getUuid()).whenCompleteAsync((invites,error)->{
                if(error!=null)failure(source,error);else source.sendFeedback(()->Text.literal("Convites ativos: "+invites.keySet()),false);
            },source.getServer());return 1;
        }));
        dispatcher.register(root);
    }
    private int openMenu(ServerCommandSource source){
        var core=runtime.get();if(source.getPlayer()==null){source.sendError(Text.literal("/clan exige um jogador"));return 0;}
        if(core==null||!core.running("clans")){source.sendError(Text.literal("Clãs indisponíveis"));return 0;}
        ClanMenu.open(source.getPlayer(),core.clans());return 1;
    }
    private int execute(ServerCommandSource source,BiFunction<ClanService,UUID,CompletableFuture<ClanSnapshot>> action){
        var core=runtime.get();
        if(source.getPlayer()==null){source.sendError(Text.literal("/clan exige um jogador"));return 0;}
        if(core==null||!core.running("clans")){source.sendError(Text.literal("Clãs indisponíveis"));return 0;}
        try{
            action.apply(core.clans(),source.getPlayer().getUuid()).thenComposeAsync(clan->core.clans().currentLimit(clan).thenApplyAsync(limit->{
                source.sendFeedback(()->Text.literal("Clã: "+clan.name()+" | Sigla: "+(clan.tag()==null?"Escolha com /clan tag <sigla>":clan.tag())+" | Líder: "+clan.leader()+" | Membros: "+clan.members().size()+"/"+limit+" "+clan.members()),false);
                return clan;
            },source.getServer()),source.getServer()).whenCompleteAsync((clan,error)->{if(error!=null)failure(source,error);},source.getServer());
            source.sendFeedback(()->Text.literal("Operação de clã iniciada."),false);return 1;
        }catch(Exception error){failure(source,error);return 0;}
    }
    private static void failure(ServerCommandSource source,Throwable error){while(error.getCause()!=null)error=error.getCause();source.sendError(Text.literal("Clã: "+error.getMessage()));}
}
