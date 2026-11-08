package com.shirokai.core.module;

/** Lifecycle boundary. Implementations own and release their resources. */
public interface CoreModule {
    String id();
    default java.util.Set<String> dependencies() { return java.util.Set.of(); }
    void start() throws Exception;
    void stop() throws Exception;
}
