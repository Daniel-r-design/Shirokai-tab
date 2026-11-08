package com.shirokai.core.module;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ModuleManagerTest {
    private static CoreModule module(String id, List<String> calls, boolean fails) {
        return new CoreModule() {
            public String id() { return id; }
            public void start() { calls.add(id + ":start"); if (fails) throw new IllegalStateException("test"); }
            public void stop() { calls.add(id + ":stop"); }
        };
    }
    @Test void failureIsCleanedUpAndDoesNotBlockAnotherModule() {
        var calls = new ArrayList<String>(); var errors = new ArrayList<String>();
        var manager = new ModuleManager((id, failure) -> errors.add(id));
        manager.register(module("clans", calls, true)); manager.register(module("tab", calls, false));
        manager.start("clans", true); manager.start("tab", true);
        assertEquals(ModuleManager.State.FAILED, manager.states().get("clans"));
        assertEquals(ModuleManager.State.RUNNING, manager.states().get("tab"));
        assertEquals(List.of("clans"), errors);
        manager.close(); manager.close();
        assertEquals(List.of("clans:start", "clans:stop", "tab:start", "tab:stop"), calls);
    }
    @Test void disabledModulesNeverStartOrStop() {
        var calls = new ArrayList<String>(); var manager = new ModuleManager((id, failure) -> fail(failure));
        manager.register(module("chat", calls, false)); manager.start("chat", false); manager.close();
        assertTrue(calls.isEmpty()); assertEquals(ModuleManager.State.DISABLED, manager.states().get("chat"));
    }
    @Test void rejectsDuplicateAndUnknownModules() {
        var manager = new ModuleManager((id, failure) -> fail(failure)); var calls = new ArrayList<String>();
        manager.register(module("tab", calls, false));
        assertThrows(IllegalArgumentException.class, () -> manager.register(module("tab", calls, false)));
        assertThrows(IllegalArgumentException.class, () -> manager.start("missing", true));
        manager.start("tab", true);
        assertThrows(IllegalStateException.class, () -> manager.start("tab", true));
        manager.close();
    }
    @Test void shutdownUsesReverseRegistrationOrder() {
        var calls = new ArrayList<String>(); var manager = new ModuleManager((id, failure) -> fail(failure));
        manager.register(module("storage", calls, false)); manager.register(module("titles", calls, false));
        manager.start("storage", true); manager.start("titles", true); manager.close();
        assertEquals(List.of("storage:start", "titles:start", "titles:stop", "storage:stop"), calls);
    }
    @Test void shutdownFailureDoesNotPreventOtherResourcesClosing() {
        var calls = new ArrayList<String>(); var errors = new ArrayList<String>();
        var manager = new ModuleManager((id, failure) -> errors.add(id));
        manager.register(module("storage", calls, false));
        manager.register(new CoreModule() {
            public String id() { return "chat"; }
            public void start() { calls.add("chat:start"); }
            public void stop() { calls.add("chat:stop"); throw new IllegalStateException("shutdown"); }
        });
        manager.start("storage", true); manager.start("chat", true); manager.close();
        assertEquals(List.of("chat"), errors);
        assertEquals(ModuleManager.State.STOPPED, manager.states().get("storage"));
        assertEquals(ModuleManager.State.FAILED, manager.states().get("chat"));
        assertEquals(List.of("storage:start", "chat:start", "chat:stop", "storage:stop"), calls);
    }
    @Test void missingDependencyBlocksOnlyItsConsumer() {
        var errors = new ArrayList<String>(); var calls = new ArrayList<String>();
        var manager = new ModuleManager((id, failure) -> errors.add(id));
        manager.register(module("storage", calls, false));
        manager.register(new CoreModule() {
            public String id() { return "titles"; }
            public java.util.Set<String> dependencies() { return java.util.Set.of("storage"); }
            public void start() { fail("Must not start without storage"); }
            public void stop() { fail("Never started"); }
        });
        manager.register(module("tab", calls, false));
        manager.start("storage", false); manager.start("titles", true); manager.start("tab", true);
        assertEquals(ModuleManager.State.BLOCKED, manager.states().get("titles"));
        assertEquals(ModuleManager.State.RUNNING, manager.states().get("tab"));
        assertEquals(List.of("titles"), errors); manager.close();
    }
}
