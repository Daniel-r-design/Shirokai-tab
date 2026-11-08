package com.shirokai.core.module;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;

/** Invoked only on the server thread. Independent modules survive lifecycle failures. */
public final class ModuleManager implements AutoCloseable {
    public enum State { REGISTERED, RUNNING, DISABLED, BLOCKED, FAILED, STOPPED }
    private final Map<String, CoreModule> modules = new LinkedHashMap<>();
    private final Map<String, State> states = new LinkedHashMap<>();
    private final BiConsumer<String, Exception> errors;
    public ModuleManager(BiConsumer<String, Exception> errors) {
        this.errors = java.util.Objects.requireNonNull(errors);
    }
    public void register(CoreModule module) {
        String id = module.id();
        if (id == null || !id.matches("[a-z][a-z0-9_-]*")) throw new IllegalArgumentException("Invalid module ID");
        if (modules.putIfAbsent(id, module) != null) throw new IllegalArgumentException("Duplicate module: " + id);
        states.put(id, State.REGISTERED);
    }
    public void start(String id, boolean enabled) {
        CoreModule module = modules.get(id);
        if (module == null) throw new IllegalArgumentException("Unknown module: " + id);
        if (states.get(id) != State.REGISTERED) throw new IllegalStateException("Module already processed: " + id);
        if (!enabled) { states.put(id, State.DISABLED); return; }
        for (String dependency : module.dependencies()) {
            if (states.get(dependency) != State.RUNNING) {
                states.put(id, State.BLOCKED);
                errors.accept(id, new IllegalStateException("Required module unavailable: " + dependency));
                return;
            }
        }
        try {
            module.start(); states.put(id, State.RUNNING);
        } catch (Exception failure) {
            states.put(id, State.FAILED);
            try { module.stop(); } catch (Exception cleanup) { failure.addSuppressed(cleanup); }
            errors.accept(id, failure);
        }
    }
    public Map<String, State> states() { return Map.copyOf(states); }
    @Override public void close() {
        var ids = new ArrayList<>(modules.keySet());
        Collections.reverse(ids);
        for (String id : ids) {
            if (states.get(id) != State.RUNNING) continue;
            try { modules.get(id).stop(); states.put(id, State.STOPPED); }
            catch (Exception failure) { states.put(id, State.FAILED); errors.accept(id, failure); }
        }
    }
}
