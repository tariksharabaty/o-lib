package com.oidioomstudio.olib.api;

import com.oidioomstudio.olib.api.i18n.OLanguageManager;
import java.util.*;

public final class OConfigRegistry {
    private static final Map<String, OModConfig> CONFIGS = new LinkedHashMap<>();

    private OConfigRegistry() {
    }

    public static synchronized void register(OModConfig config) {
        if (config != null) {
            String modId = config.getModId().toLowerCase(Locale.ROOT);
            CONFIGS.put(modId, config);
            OLanguageManager.registerMod(modId);
        }
    }

    public static synchronized OModConfig get(String modId) {
        if (modId == null)
            return null;
        return CONFIGS.get(modId.toLowerCase(Locale.ROOT));
    }

    public static synchronized Collection<OModConfig> getAll() {
        return Collections.unmodifiableCollection(new ArrayList<>(CONFIGS.values()));
    }

    public static synchronized boolean has(String modId) {
        return modId != null && CONFIGS.containsKey(modId.toLowerCase(Locale.ROOT));
    }

    public static synchronized OModConfig getDefault() {
        if (CONFIGS.containsKey("olib")) {
            return CONFIGS.get("olib");
        }
        return null;
    }

    public static synchronized Set<String> getRegisteredModIds() {
        return Collections.unmodifiableSet(new HashSet<>(CONFIGS.keySet()));
    }
}
