package com.shirokai.core.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class ConfigServiceTest {
    @TempDir Path directory;
    @Test void createsDefaultsOnceAndPreservesCustomizedFile() throws Exception {
        Path path = directory.resolve("core.json"); var service = new ConfigService(path);
        assertEquals(CoreConfig.defaults(), service.load());
        String customized = "{\"schemaVersion\":1,\"unavailablePlaceholder\":\"Indisponível\"}";
        Files.writeString(path, customized);
        assertEquals("Indisponível", service.load().unavailablePlaceholder());
        assertEquals(customized, Files.readString(path));
    }
    @Test void rejectsInvalidJsonTypesVersionsAndUnsafePaths() {
        for (String json : new String[]{"null", "[]", "{schemaVersion:1}", "{\"schemaVersion\":1,}",
                "{\"schemaVersion\":1} {}", "{\"schemaVersion\":2}", "{\"schemaVersion\":1,\"typo\":true}",
                "{\"schemaVersion\":1,\"modules\":{\"storage\":\"true\"}}",
                "{\"schemaVersion\":1,\"modules\":{\"fake\":true}}",
                "{\"schemaVersion\":1,\"databaseFile\":\"../secrets.db\"}"}) {
            assertThrows(Exception.class, () -> ConfigService.parse(json), json);
        }
    }
    @Test void failedLoadDoesNotOverwriteUserFile() throws Exception {
        Path path = directory.resolve("core.json"); Files.writeString(path, "broken JSON");
        assertThrows(Exception.class, () -> new ConfigService(path).load());
        assertEquals("broken JSON", Files.readString(path));
    }
    @Test void distinguishesHotReloadFromRestartSettings() throws Exception {
        var hot = ConfigService.parse("{\"schemaVersion\":1,\"unavailablePlaceholder\":\"?\"}");
        var restart = ConfigService.parse("{\"schemaVersion\":1,\"modules\":{\"storage\":false}}");
        assertFalse(hot.requiresRestartComparedTo(CoreConfig.defaults()));
        assertTrue(restart.requiresRestartComparedTo(CoreConfig.defaults()));
        assertThrows(UnsupportedOperationException.class, () -> hot.modules().put("storage", false));
    }
}
