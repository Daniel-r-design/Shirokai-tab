package com.shirokai.core.tab;

import net.minecraft.scoreboard.*;
import java.util.*;
import java.util.function.*;

/** Opt-in scoreboard ownership: never adopts existing teams or moves foreign members. */
public final class TeamOwnership implements AutoCloseable{
    public record Player(UUID id,String name,int priority){}
    private final Scoreboard scoreboard;private final Consumer<String> warning;
    private final Map<String,Team> owned=new HashMap<>();private final Map<String,Team> assigned=new HashMap<>();private final Set<String> warned=new LinkedHashSet<>();
    private boolean closed;private long conflicts;
    public TeamOwnership(Scoreboard scoreboard,Consumer<String> warning){this.scoreboard=Objects.requireNonNull(scoreboard);this.warning=warning;}
    public static String teamName(int priority){int bounded=Math.max(-1000000,Math.min(1000000,priority));return String.format(Locale.ROOT,"sk%07d",1000000-bounded);}
    public boolean owns(Team team){return team!=null&&owned.get(team.getName())==team&&scoreboard.getTeam(team.getName())==team;}
    private void note(String key){if(warned.add(key)){conflicts++;warning.accept(key);if(warned.size()>128)warned.remove(warned.iterator().next());}}
    public void reconcile(Collection<Player> players){
        if(closed)return;var active=new HashSet<String>();for(var player:players)active.add(player.name());
        for(String name:List.copyOf(assigned.keySet()))if(!active.contains(name))release(name);
        for(var player:players){
            var current=scoreboard.getScoreHolderTeam(player.name());if(current!=null&&!owns(current)){assigned.remove(player.name());note("Jogador "+player.name()+" possui team externo "+current.getName());continue;}
            String name=teamName(player.priority());var desired=scoreboard.getTeam(name);
            if(desired!=null&&!owns(desired)){note("Nome de team já ocupado: "+name);continue;}
            if(desired==null){desired=scoreboard.addTeam(name);desired.setShowFriendlyInvisibles(false);desired.setFriendlyFireAllowed(true);desired.setCollisionRule(AbstractTeam.CollisionRule.ALWAYS);owned.put(name,desired);}
            if(current!=desired){if(current!=null)scoreboard.removeScoreHolderFromTeam(player.name(),current);scoreboard.addScoreHolderToTeam(player.name(),desired);}assigned.put(player.name(),desired);
        }
        prune();
    }
    private void release(String name){var expected=assigned.remove(name);if(owns(expected)&&scoreboard.getScoreHolderTeam(name)==expected)scoreboard.removeScoreHolderFromTeam(name,expected);}
    private void prune(){for(var entry:List.copyOf(owned.entrySet())){
        if(scoreboard.getTeam(entry.getKey())!=entry.getValue()){owned.remove(entry.getKey());continue;}
        if(entry.getValue().getPlayerList().isEmpty()){scoreboard.removeTeam(entry.getValue());owned.remove(entry.getKey());}
    }}
    public int ownedTeams(){return owned.size();}public long conflicts(){return conflicts;}
    @Override public void close(){if(closed)return;for(String name:List.copyOf(assigned.keySet()))release(name);prune();for(var team:owned.values())note("Team com integrantes externos preservado: "+team.getName());owned.clear();closed=true;}
}
