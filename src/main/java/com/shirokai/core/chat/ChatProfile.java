package com.shirokai.core.chat;

import java.util.*;

public record ChatProfile(UUID player, Channel channel, Set<UUID> ignored, Mute mute) {
    public record Mute(long expiresAt,String reason){public boolean active(long now){return expiresAt==0||expiresAt>now;}}
    public ChatProfile { ignored=Set.copyOf(ignored); }
}
