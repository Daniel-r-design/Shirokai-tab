package com.shirokai.core.storage;

import java.sql.*;

/** Schema3 migrations run exclusively inside the storage transaction. */
final class ClanSchema {
    private ClanSchema() {}
    static void migrate(Statement s) throws SQLException {
        s.execute("CREATE TABLE clans(id TEXT PRIMARY KEY NOT NULL,name TEXT NOT NULL,name_key TEXT NOT NULL UNIQUE,tag TEXT,tag_key TEXT UNIQUE,leader_uuid TEXT NOT NULL,created_at INTEGER NOT NULL,next_name_edit INTEGER NOT NULL DEFAULT 0,next_tag_edit INTEGER NOT NULL DEFAULT 0,FOREIGN KEY(id,leader_uuid) REFERENCES clan_members(clan_id,player_uuid) DEFERRABLE INITIALLY DEFERRED)");
        s.execute("CREATE TABLE clan_members(player_uuid TEXT PRIMARY KEY NOT NULL,clan_id TEXT NOT NULL REFERENCES clans(id) ON DELETE CASCADE,role TEXT NOT NULL CHECK(role IN ('MEMBER','OFFICER')),joined_at INTEGER NOT NULL,UNIQUE(clan_id,player_uuid))");
        s.execute("CREATE TABLE clan_invites(clan_id TEXT NOT NULL REFERENCES clans(id) ON DELETE CASCADE,target_uuid TEXT NOT NULL,inviter_uuid TEXT NOT NULL,expires_at INTEGER NOT NULL,PRIMARY KEY(clan_id,target_uuid))");
        s.execute("CREATE INDEX clan_invites_target ON clan_invites(target_uuid,expires_at)");
        s.execute("CREATE TABLE clan_invite_cooldowns(clan_id TEXT NOT NULL REFERENCES clans(id) ON DELETE CASCADE,actor_uuid TEXT NOT NULL,target_uuid TEXT NOT NULL,next_at INTEGER NOT NULL,PRIMARY KEY(clan_id,actor_uuid,target_uuid))");
        s.execute("CREATE TABLE clan_creation_cooldowns(player_uuid TEXT PRIMARY KEY NOT NULL,next_at INTEGER NOT NULL)");
        s.execute("CREATE TABLE clan_transfers(clan_id TEXT PRIMARY KEY NOT NULL REFERENCES clans(id) ON DELETE CASCADE,from_uuid TEXT NOT NULL,to_uuid TEXT NOT NULL,expires_at INTEGER NOT NULL)");
        s.execute("CREATE TABLE clan_disband_confirmations(clan_id TEXT PRIMARY KEY NOT NULL REFERENCES clans(id) ON DELETE CASCADE,actor_uuid TEXT NOT NULL,expires_at INTEGER NOT NULL)");
        s.execute("CREATE TABLE clan_history(id INTEGER PRIMARY KEY,clan_id TEXT NOT NULL,actor_uuid TEXT NOT NULL,action TEXT NOT NULL,details TEXT NOT NULL,created_at INTEGER NOT NULL)");
        s.execute("CREATE INDEX clan_history_clan ON clan_history(clan_id,id)");
    }
}
