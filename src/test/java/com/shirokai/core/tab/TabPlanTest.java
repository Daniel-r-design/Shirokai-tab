package com.shirokai.core.tab;

import com.google.gson.*;
import com.shirokai.core.permissions.PermissionService;
import com.shirokai.core.styles.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class TabPlanTest{
    @TempDir Path directory;
    @Test void defaultsPreserveFilesAndRejectUnknownTypesAndOwnershipChanges()throws Exception{
        Path file=directory.resolve("tab.json"),styles=directory.resolve("styles.json");assertEquals(TabConfig.defaults(),TabConfig.load(file));assertEquals(StyleConfig.defaults(),StyleConfig.load(styles));
        for(String bad:List.of("null","[]","{schemaVersion:1}",Files.readString(file).replace("\"manageTeams\": false","\"manageTeams\": \"false\""),Files.readString(file).replace("\"updateTicks\": 20","\"updateTicks\": 1")))assertThrows(Exception.class,()->TabConfig.parse(bad));
        var modified=new Gson().toJsonTree(TabConfig.defaults()).getAsJsonObject();modified.addProperty("manageTeams",true);assertTrue(TabConfig.parse(modified.toString()).requiresRestartComparedTo(TabConfig.defaults()));
        Files.writeString(file,"broken");assertThrows(Exception.class,()->TabConfig.load(file));assertEquals("broken",Files.readString(file));
    }
    @Test void permissionGroupWorldAndPrioritySelectLayoutsWithoutFallbackLeak(){
        var base=TabConfig.defaults();var condition=new TabConfig.Condition("layout.vip","vip",Set.of("minecraft:overworld"));
        var vip=new TabConfig.Layout("vip",100,condition,List.of("VIP"),List.of());var layouts=new ArrayList<>(base.layouts());layouts.add(vip);
        var plan=new TabPlan(new TabConfig(1,20,true,false,base.nameTemplate(),layouts,List.of()),new StyleEngine(StyleConfig.defaults()));
        var meta=new PermissionService.Metadata("vip","","",10);assertEquals("vip",plan.header(meta,"minecraft:overworld",node->true).definition().id());
        assertEquals("default",plan.header(meta,"minecraft:overworld",node->false).definition().id());assertEquals("default",plan.header(meta,"minecraft:the_nether",node->true).definition().id());
    }
    @Test void invalidAnimationsAndNoUnconditionalDefaultFailBeforePublish(){
        var base=TabConfig.defaults();
        assertThrows(IllegalArgumentException.class,()->new TabPlan(new TabConfig(1,20,true,false,"x".repeat(257),base.layouts(),List.of()),new StyleEngine(StyleConfig.defaults())));
        var unknown=new TabConfig.Layout("default",0,TabConfig.Condition.any(),List.of("%animation:absent%"),List.of());
        assertThrows(IllegalArgumentException.class,()->new TabPlan(new TabConfig(1,20,true,false,base.nameTemplate(),List.of(unknown),List.of()),new StyleEngine(StyleConfig.defaults())));
        assertThrows(IllegalArgumentException.class,()->new TabConfig(1,20,true,false,base.nameTemplate(),List.of(new TabConfig.Layout("default",0,new TabConfig.Condition("vip",null,Set.of()),List.of(),List.of())),List.of()));
    }
}
