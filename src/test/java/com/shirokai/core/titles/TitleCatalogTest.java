package com.shirokai.core.titles;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class TitleCatalogTest {
    @TempDir Path directory;
    @Test void createsDefaultsAndNeverOverwritesCustomOrBrokenCatalog() throws Exception {
        Path file=directory.resolve("titles.json"); assertEquals(TitleCatalog.defaults(),TitleCatalog.load(file));
        String original=Files.readString(file); assertEquals(TitleCatalog.defaults(),TitleCatalog.parse(original));
        Files.writeString(file,"broken"); assertThrows(Exception.class,()->TitleCatalog.load(file)); assertEquals("broken",Files.readString(file));
    }
    @Test void rejectsUnknownFieldsTypesUnsupportedClansAndMismatchedIds() throws Exception {
        Path file=directory.resolve("titles.json");TitleCatalog.load(file);String json=Files.readString(file);
        for(String bad:new String[]{"[]","null","{schemaVersion:1}",json+" {}",json.replace("\"schemaVersion\": 1","\"schemaVersion\": 2"),
                json.replace("\"SPECIAL\"","\"CLAN\""),json.replace("\"id\": \"traveler\"","\"id\": \"other\""),
                json.replace("\"chat\": true","\"chat\": \"true\""),json.replace("\"priority\": 0","\"priority\": 1.5"),
                json.replace("\"chat\": true","\"typo\": true")}) assertThrows(Exception.class,()->TitleCatalog.parse(bad),bad);
    }
}
