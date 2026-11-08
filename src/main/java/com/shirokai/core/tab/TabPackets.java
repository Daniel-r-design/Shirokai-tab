package com.shirokai.core.tab;

import com.shirokai.core.mixin.PlayerListPacketAccessor;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import java.util.*;

public final class TabPackets {
    private TabPackets(){}
    public static PlayerListS2CPacket names(List<PlayerListS2CPacket.Entry> entries){
        var packet=new PlayerListS2CPacket(EnumSet.of(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME),List.of());
        ((PlayerListPacketAccessor)packet).shirokai$setEntries(List.copyOf(entries));return packet;
    }
}
