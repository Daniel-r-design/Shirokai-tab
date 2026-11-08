package com.shirokai.core.tab;

import com.mojang.brigadier.CommandDispatcher;
import com.shirokai.core.CoreRuntime;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import java.util.function.Supplier;
import static net.minecraft.server.command.CommandManager.literal;

public final class TabCommands{
    private final Supplier<CoreRuntime> runtime;
    public TabCommands(Supplier<CoreRuntime> runtime){this.runtime=runtime;}
    public void register(CommandDispatcher<ServerCommandSource> dispatcher){
        if(dispatcher.getRoot().getChild("tab")!=null){org.slf4j.LoggerFactory.getLogger("shirokai-core").warn("[tab] /tab já registrado; alias Core preservado sem sobrescrever terceiro");return;}
        dispatcher.register(literal("tab").executes(c->refresh(c.getSource())).then(literal("refresh").executes(c->refresh(c.getSource())))
            .then(literal("animations").executes(c->{var source=c.getSource();if(!available(source))return 0;
                runtime.get().tab().toggleAnimations(source.getPlayer().getUuid()).whenCompleteAsync((enabled,error)->{
                    if(error!=null)source.sendError(Text.literal("Não foi possível salvar preferência de animação"));
                    else source.sendFeedback(()->Text.literal("Animações: "+(enabled?"ativadas":"desativadas")),false);
                },source.getServer());return 1;
            })));
    }
    private boolean available(ServerCommandSource source){if(source.getPlayer()==null){source.sendError(Text.literal("TAB exige jogador"));return false;}var core=runtime.get();if(core==null||!core.running("tab")){source.sendError(Text.literal("TAB indisponível"));return false;}return true;}
    private int refresh(ServerCommandSource source){if(!available(source))return 0;runtime.get().tab().refresh(source.getPlayer().getUuid());source.sendFeedback(()->Text.literal("TAB atualizado"),false);return 1;}
}
