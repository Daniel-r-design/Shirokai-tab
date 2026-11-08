package com.shirokai.core.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class SqliteStorageTest {
    @TempDir Path directory;
    @Test void migratesPersistsRestartsAndBacksUp() throws Exception {
        Path db = directory.resolve("data.db"), backup = directory.resolve("backups/snapshot.db");
        UUID id = UUID.randomUUID();
        try (var storage = new SqliteStorage(db)) {
            storage.ready().get(10, TimeUnit.SECONDS);
            assertTrue(storage.preferences(id).get().scoreboardEnabled());
            storage.setScoreboard(id, false).get();
            storage.audit(id, "test").get();
            assertEquals("ok", storage.integrityCheck().get());
            assertEquals(backup, storage.backup(backup).get());
            assertThrows(ExecutionException.class, () -> storage.backup(backup).get());
        }
        for (Path path : List.of(db, backup)) {
            try (var storage = new SqliteStorage(path)) {
                storage.ready().get();
                assertFalse(storage.preferences(id).get().scoreboardEnabled());
                assertEquals("ok", storage.integrityCheck().get());
            }
        }
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + backup);
             Statement s = c.createStatement(); ResultSet rows = s.executeQuery("SELECT COUNT(*) FROM admin_audit")) {
            assertTrue(rows.next()); assertEquals(1, rows.getInt(1));
        }
    }
    @Test void acceptsConcurrentRequestsAndDrainsWritesOnClose() throws Exception {
        Path db = directory.resolve("data.db");
        var ids = new ArrayList<UUID>();
        var storage = new SqliteStorage(db);
        var futures = new ArrayList<CompletableFuture<Void>>();
        for (int i = 0; i < 80; i++) { UUID id = UUID.randomUUID(); ids.add(id); futures.add(storage.setScoreboard(id, false)); }
        storage.close();
        for (var future : futures) assertTrue(future.isDone() && !future.isCompletedExceptionally());
        assertThrows(ExecutionException.class, () -> storage.setScoreboard(UUID.randomUUID(), true).get());
        storage.close();
        try (var reopened = new SqliteStorage(db)) {
            for (UUID id : ids) assertFalse(reopened.preferences(id).get().scoreboardEnabled());
        }
    }
    @Test void rejectsFutureSchemaWithoutChangingItsContents() throws Exception {
        Path db = directory.resolve("future.db");
        Class.forName("org.sqlite.JDBC");
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + db); var s = c.createStatement()) {
            s.execute("PRAGMA user_version=42"); s.execute("CREATE TABLE important(value TEXT)");
            s.execute("INSERT INTO important VALUES('preserve')");
        }
        try (var storage = new SqliteStorage(db)) {
            assertThrows(ExecutionException.class, () -> storage.ready().get());
            assertThrows(ExecutionException.class, () -> storage.preferences(UUID.randomUUID()).get());
        }
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + db); var s = c.createStatement();
             var rows = s.executeQuery("SELECT value FROM important")) {
            assertTrue(rows.next()); assertEquals("preserve", rows.getString(1));
        }
    }
    @Test void failedMigrationRollsBackAllSchemaChanges() throws Exception {
        Path db = directory.resolve("broken.db");
        Class.forName("org.sqlite.JDBC");
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + db); var s = c.createStatement()) {
            s.execute("CREATE TABLE admin_audit(existing TEXT)");
        }
        try (var storage = new SqliteStorage(db)) { assertThrows(ExecutionException.class, () -> storage.ready().get()); }
        try (var c = DriverManager.getConnection("jdbc:sqlite:" + db); var s = c.createStatement();
             var rows = s.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE name='player_preferences'")) {
            assertTrue(rows.next()); assertEquals(0, rows.getInt(1));
        }
    }
    @Test void cancellingResponseNeverDropsAcceptedWriteOrLeaksQueueSlot() throws Exception {
        Path db = directory.resolve("cancel.db"); UUID first = UUID.randomUUID(), cancelled = UUID.randomUUID();
        var storage = new SqliteStorage(db); storage.ready().get();
        try (var lock = DriverManager.getConnection("jdbc:sqlite:" + db); var statement = lock.createStatement()) {
            statement.execute("BEGIN IMMEDIATE");
            var firstWrite = storage.setScoreboard(first, false);
            var response = storage.setScoreboard(cancelled, false);
            assertTrue(response.cancel(false));
            statement.execute("COMMIT");
            firstWrite.get();
        } finally { storage.close(); }
        assertEquals(0, storage.pendingOperations());
        try (var reopened = new SqliteStorage(db)) {
            assertFalse(reopened.preferences(cancelled).get().scoreboardEnabled());
        }
    }
}
