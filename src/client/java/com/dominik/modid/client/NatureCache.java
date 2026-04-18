package com.dominik.modid.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NatureCache {
    public static final Map<Integer, String> ENTITY_NATURES = new ConcurrentHashMap<>();
    public static boolean FEATURE_AVAILABLE = false;

    public static void reset() {
        ENTITY_NATURES.clear();
        FEATURE_AVAILABLE = false;
    }
}
