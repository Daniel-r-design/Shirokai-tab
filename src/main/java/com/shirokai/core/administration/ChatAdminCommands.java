package com.shirokai.core.administration;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.shirokai.core.CoreRuntime;
import com.shirokai.core.chat.ChatProfile;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import static net.minecraft.server.command.CommandManager.*;

public final class ChatAdminCommands {
    private final Supplier<CoreRuntime> runtime;private final BiPredicate<ServerCommandSource,String> allowed;
    public ChatAdminCommands(Supplier<CoreRuntime> runtime,BiPredicate<ServerCommandSource,String> allowed){this.runtime=runtime;this.allowed=allowed;}
    public LiteralArgumentBuilder<ServerCommandSource> node(){return literal("chat").requires(s->allowed.test(s,"chat.mute")||allowed.test(s,"chat.unmute")||allowed.test(s,"chat.inspect"))
        .then(literal("inspect").requires(s->allowed.test(s,"chat.inspect")).then(argument("uuid",StringArgumentType.word()).executes(c->execute(c.getSource(),()->runtime.get().chat().service().inspect(UUID.fromString(StringArgumentType.getString(c,"uuid")))))))
        .then(literal("unmute").requires(s->allowed.test(s,"chat.unmute")).then(argument("uuid",StringArgumentType.word()).executes(c->execute(c.getSource(),()->runtime.get().chat().service().mute(actor(c.getSource()),UUID.fromString(StringArgumentType.getString(c,"uuid")),null,"")))))
        .then(literal("mute").requires(s->allowed.test(s,"chat.mute")).then(argument("uuid",StringArgumentType.word()).then(argument("seconds",IntegerArgumentType.integer(0,31536000))
            .executes(c->mute(c.getSource(),StringArgumentType.getString(c,"uuid"),IntegerArgumentType.getInteger(c,"seconds"),"Moderação"))
            .then(argument("reason",StringArgumentType.greedyString()).executes(c->mute(c.getSource(),StringArgumentType.getString(c,"uuid"),IntegerArgumentType.getInteger(c,"seconds"),StringArgumentType.getString(c,"reason")))))));
    }
    private static UUID actor(ServerCommandSource source){return source.getPlayer()==null?null:source.getPlayer().getUuid();}
    private int mute(ServerCommandSource source,String uuid,int seconds,String reason){return execute(source,()->runtime.get().chat().service().mute(actor(source),UUID.fromString(uuid),seconds==0?0L:System.currentTimeMillis()+seconds*1000L,reason));}
    private int execute(ServerCommandSource source,Supplier<CompletableFuture<ChatProfile>> action){
        try{action.get().whenCompleteAsync((profile,error)->{
            if(error!=null)failure(source,error);else source.sendFeedback(()->Text.literal("Chat "+profile.player()+" canal="+profile.channel()+" mute="+profile.mute()+" ignores="+profile.ignored().size()),false);
        },source.getServer());return 1;}catch(Exception error){failure(source,error);return 0;}
    }
    private static void failure(ServerCommandSource source,Throwable error){while(error.getCause()!=null)error=error.getCause();source.sendError(Text.literal("Chat: "+error.getMessage()));}
}
