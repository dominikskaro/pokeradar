package com.dominik.modid.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HiddenAbilityCache {
    public static final Map<Integer, Boolean> ENTITY_HIDDEN_ABILITY = new ConcurrentHashMap<>();
    public static boolean FEATURE_AVAILABLE = false;

    public static void reset() {
        ENTITY_HIDDEN_ABILITY.clear();
        FEATURE_AVAILABLE = false;
    }
}
