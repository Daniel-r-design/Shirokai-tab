package com.shirokai.core.placeholders;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PlaceholderServiceTest {
    private final PlaceholderService service = new PlaceholderService((key, error) -> fail(error));
    @Test void usesRealSnapshotValuesAndFallbackForUnavailableData() {
        UUID id = UUID.randomUUID(); var context = new PlaceholderService.Context(id, "Ash", 7);
        assertEquals("Ash/7/" + id + "/N/D", service.render("%shirokai:player%/%shirokai:online%/%shirokai:uuid%/%shirokai:dollars%", context, "N/D"));
        assertEquals("?", service.render("%shirokai:player%", new PlaceholderService.Context(null, null, 0), "?"));
    }
    @Test void replacementIsLiteralNonrecursiveAndDoesNotInterpretUserMarkup() {
        String malicious = "$1\\%shirokai:online%<click:run_command:'/op x'>";
        assertEquals(malicious, service.render("%shirokai:player%", new PlaceholderService.Context(null, malicious, 99), "?"));
        assertThrows(IllegalArgumentException.class, () -> service.render("x".repeat(8193), new PlaceholderService.Context(null, null, 0), "?"));
    }
}
