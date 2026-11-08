package com.shirokai.core.titles;

import java.util.*;

public record TitleState(UUID player, long revision, Map<String, Long> grants, String selectedVisual, List<String> chatIds) {
    public TitleState { grants = Map.copyOf(grants); chatIds = List.copyOf(chatIds); }
}
