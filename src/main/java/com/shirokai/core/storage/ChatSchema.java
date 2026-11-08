package com.shirokai.core.storage;

import java.sql.*;

final class ChatSchema {
    private ChatSchema(){}
    static void migrate(Statement s)throws SQLException{
        s.execute("CREATE TABLE chat_profiles(player_uuid TEXT PRIMARY KEY NOT NULL,channel TEXT NOT NULL CHECK(channel IN ('GLOBAL','LOCAL','CLAN','STAFF')))");
        s.execute("CREATE TABLE chat_ignores(owner_uuid TEXT NOT NULL,target_uuid TEXT NOT NULL,CHECK(owner_uuid<>target_uuid),PRIMARY KEY(owner_uuid,target_uuid))");
        s.execute("CREATE TABLE chat_mutes(player_uuid TEXT PRIMARY KEY NOT NULL,expires_at INTEGER NOT NULL CHECK(expires_at>=0),reason TEXT NOT NULL,actor_uuid TEXT,created_at INTEGER NOT NULL)");
    }
}
