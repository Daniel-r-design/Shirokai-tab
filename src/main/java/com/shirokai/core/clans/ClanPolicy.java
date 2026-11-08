package com.shirokai.core.clans;

import java.util.*;

/** Configurable internal actions with immutable leadership and hierarchy invariants. */
public final class ClanPolicy {
    public static final Set<String> ACTIONS=Set.of("invite","kick","promote","demote","rename","tag");
    private ClanPolicy(){}
    public static void require(ClanSnapshot clan, UUID actor,String action,ClanConfig config){
        var role=clan.members().get(actor);
        if(role==null || !config.roleActions().getOrDefault(role.name(),Set.of()).contains(action))throw new IllegalStateException("Seu cargo não permite "+action);
    }
}
