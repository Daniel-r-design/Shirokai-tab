package com.shirokai.core.clans;

import java.util.*;

public record ClanSnapshot(UUID id, String name, String tag, UUID leader, long nextNameEdit, long nextTagEdit,
                           Map<UUID, Role> members) {
    public enum Role { LEADER, OFFICER, MEMBER }
    public ClanSnapshot { members = Map.copyOf(members); }
}
