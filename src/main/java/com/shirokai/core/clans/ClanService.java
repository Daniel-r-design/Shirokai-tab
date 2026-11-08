package com.shirokai.core.clans;

import com.shirokai.core.permissions.PermissionService;
import java.util.*;
import java.util.concurrent.*;

/** Permission snapshots are captured on the server executor, never from JDBC callbacks. */
public final class ClanService {
    private final ClanRepository repository;
    private final PermissionService permissions;
    private final Executor server;
    private ClanConfig config;
    public ClanService(ClanRepository repository,PermissionService permissions,Executor server,ClanConfig config) {
        this.repository=repository;this.permissions=permissions;this.server=server;this.config=config;
    }
    public ClanConfig config(){return config;}
    public void replaceConfig(ClanConfig value){config=value;}
    public int limit(UUID leader){
        int limit=config.defaultLimit();
        for(var entry:config.permissionLimits().entrySet())if(permissions.allowed(leader,entry.getKey()))limit=Math.max(limit,entry.getValue());
        return limit;
    }
    public CompletableFuture<Map<String,Long>> invitations(UUID player){return repository.invitations(player);}
    public CompletableFuture<ClanSnapshot> own(UUID player){return repository.own(player);}
    public CompletableFuture<ClanSnapshot> find(String name){return repository.find(name);}
    public CompletableFuture<ClanSnapshot> create(UUID actor,String name,long playedSeconds){
        if(!config.creationEnabled())throw new IllegalStateException("Criação de clãs desativada");
        if(!permissions.allowed(actor,"shirokaicore.clan.create"))throw new IllegalStateException("Sem permissão shirokaicore.clan.create");
        if(playedSeconds<config.minimumPlaySeconds())throw new IllegalStateException("Tempo mínimo de jogo não atingido");
        return repository.create(actor,config.name(name),config);
    }
    public CompletableFuture<ClanSnapshot> identity(UUID actor,String value,boolean tag){return repository.identity(actor,tag?config.tag(value):config.name(value),tag,config);}
    public CompletableFuture<ClanSnapshot> invite(UUID actor,UUID target){return repository.invite(actor,target,config);}
    public CompletableFuture<ClanSnapshot> accept(UUID actor,String name){
        return repository.find(name).thenComposeAsync(clan -> permissions.prepare(clan.leader()).thenComposeAsync(v ->
                repository.accept(actor,clan.id(),clan.leader(),limit(clan.leader())),server),server);
    }
    public CompletableFuture<ClanSnapshot> decline(UUID actor,String name){return repository.decline(actor,name);}
    public CompletableFuture<ClanSnapshot> cancel(UUID actor,UUID target){return repository.cancel(actor,target);}
    public CompletableFuture<ClanSnapshot> leave(UUID actor){return repository.leave(actor);}
    public CompletableFuture<ClanSnapshot> memberAction(UUID actor,UUID target,String action){return repository.memberAction(actor,target,action);}
    public CompletableFuture<ClanSnapshot> transfer(UUID actor,UUID target){return repository.requestTransfer(actor,target,config);}
    public CompletableFuture<ClanSnapshot> answerTransfer(UUID actor,boolean accept){return repository.answerTransfer(actor,accept);}
    public CompletableFuture<ClanSnapshot> disband(UUID actor,boolean confirm){return repository.disband(actor,confirm,config);}
    public CompletableFuture<Integer> currentLimit(ClanSnapshot clan){return permissions.prepare(clan.leader()).thenApplyAsync(v -> limit(clan.leader()),server);}
}
