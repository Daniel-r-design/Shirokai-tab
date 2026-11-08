package com.shirokai.smoke;

import com.shirokai.core.tab.TeamOwnership;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.text.Text;
import static net.minecraft.server.command.CommandManager.literal;

import net.minecraft.scoreboard.Scoreboard;
import java.util.*;

public final class TeamOwnershipSmoke implements ModInitializer{
    @Override public void onInitialize(){CommandRegistrationCallback.EVENT.register((dispatcher,registry,environment)->dispatcher.register(literal("shirokai-teams-smoke").requires(s->s.hasPermissionLevel(4)).executes(context->{
        samePrioritySharesTeamAndForeignPlayersAreNeverMoved();foreignNameCollisionAndReplacementAreNotAdoptedOrDeleted();changedPriorityAndDisconnectPruneOnlyEmptyOwnedTeams();
        context.getSource().sendFeedback(()->Text.literal("TEAM_OWNERSHIP_SMOKE_PASS"),false);return 1;
    })));}
    private static void assertSame(Object expected,Object actual){if(expected!=actual)throw new AssertionError("Identity mismatch");}
    private static void assertNull(Object actual){if(actual!=null)throw new AssertionError("Expected null");}
    private static void assertTrue(boolean value){if(!value)throw new AssertionError("Expected true");}
    private static void samePrioritySharesTeamAndForeignPlayersAreNeverMoved(){
        var scoreboard=new Scoreboard();var foreign=scoreboard.addTeam("external");scoreboard.addScoreHolderToTeam("Alice",foreign);
        var manager=new TeamOwnership(scoreboard,message->{});
        manager.reconcile(List.of(new TeamOwnership.Player(UUID.randomUUID(),"Alice",100),new TeamOwnership.Player(UUID.randomUUID(),"Bob",100),new TeamOwnership.Player(UUID.randomUUID(),"Carol",100)));
        assertSame(foreign,scoreboard.getScoreHolderTeam("Alice"));assertSame(scoreboard.getScoreHolderTeam("Bob"),scoreboard.getScoreHolderTeam("Carol"));
        assertTrue(TeamOwnership.teamName(100).compareTo(TeamOwnership.teamName(0))<0);assertTrue(TeamOwnership.teamName(-1000000).length()<=16);
        manager.close();assertSame(foreign,scoreboard.getScoreHolderTeam("Alice"));assertNull(scoreboard.getScoreHolderTeam("Bob"));assertSame(foreign,scoreboard.getTeam("external"));
    }
    private static void foreignNameCollisionAndReplacementAreNotAdoptedOrDeleted(){
        var scoreboard=new Scoreboard();var collision=scoreboard.addTeam(TeamOwnership.teamName(100));var manager=new TeamOwnership(scoreboard,message->{});
        manager.reconcile(List.of(new TeamOwnership.Player(UUID.randomUUID(),"Alice",100)));assertNull(scoreboard.getScoreHolderTeam("Alice"));
        manager.reconcile(List.of(new TeamOwnership.Player(UUID.randomUUID(),"Bob",0)));var original=scoreboard.getScoreHolderTeam("Bob");scoreboard.removeTeam(original);var replacement=scoreboard.addTeam(original.getName());scoreboard.addScoreHolderToTeam("Bob",replacement);
        manager.reconcile(List.of(new TeamOwnership.Player(UUID.randomUUID(),"Bob",0)));manager.close();assertSame(collision,scoreboard.getTeam(collision.getName()));assertSame(replacement,scoreboard.getScoreHolderTeam("Bob"));
    }
    private static void changedPriorityAndDisconnectPruneOnlyEmptyOwnedTeams(){
        var scoreboard=new Scoreboard();var manager=new TeamOwnership(scoreboard,message->{});UUID id=UUID.randomUUID();
        manager.reconcile(List.of(new TeamOwnership.Player(id,"Alice",0)));String old=scoreboard.getScoreHolderTeam("Alice").getName();manager.reconcile(List.of(new TeamOwnership.Player(id,"Alice",100)));assertNull(scoreboard.getTeam(old));
        var team=scoreboard.getScoreHolderTeam("Alice");scoreboard.addScoreHolderToTeam("ExternalHolder",team);manager.close();assertNull(scoreboard.getScoreHolderTeam("Alice"));assertSame(team,scoreboard.getScoreHolderTeam("ExternalHolder"));
    }
}
