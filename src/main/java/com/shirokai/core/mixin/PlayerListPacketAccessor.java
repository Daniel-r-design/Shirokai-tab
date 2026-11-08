package com.shirokai.core.mixin;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.List;

/** Version-pinned access to the real packet entries; no additional packet actions are invented. */
@Mixin(PlayerListS2CPacket.class)
public interface PlayerListPacketAccessor {
    @Mutable @Accessor("entries") void shirokai$setEntries(List<PlayerListS2CPacket.Entry> entries);
}
