package com.oidioomstudio.olib.api.i18n;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.locale.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages UI language localization for O Lib and all Oidioom Studio mods,
 * independently of Minecraft's global game language.
 */
public final class OLanguageManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("O Lib/I18n");
    private static OLanguage currentLanguage = OLanguage.AUTO;
    private static Language baseLanguage = null;
    private static final Map<String, String> activeTranslations = new ConcurrentHashMap<>();
    private static final Set<String> knownModIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();

    static {
        knownModIds.add("olib");
        knownModIds.add("fastminer");
        knownModIds.add("purplestick");
        knownModIds.add("advancedenchant");
        knownModIds.add("slotforelytra");
        knownModIds.add("bonebone");
        knownModIds.add("fasttree");
        knownModIds.add("torchmanator");
        knownModIds.add("crystalwand");
        knownModIds.add("megacoppergolems");
    }

    private OLanguageManager() {
    }

    public static void init() {
        if (baseLanguage == null) {
            Language current = Language.getInstance();
            if (current instanceof ODelegatingLanguage delegating) {
                baseLanguage = delegating.getParent();
            } else {
                baseLanguage = current;
            }
        }
        applyLanguage(currentLanguage);
    }

    public static OLanguage getCurrentLanguage() {
        return currentLanguage;
    }

    public static OLanguage getSelectedLanguage() {
        return currentLanguage;
    }

    public static void registerMod(String modId) {
        if (modId != null && !modId.isBlank()) {
            knownModIds.add(modId.toLowerCase(Locale.ROOT));
            if (currentLanguage != OLanguage.AUTO) {
                applyLanguage(currentLanguage);
            }
        }
    }

    public static synchronized void setLanguage(OLanguage language) {
        if (language == null) language = OLanguage.AUTO;
        currentLanguage = language;
        applyLanguage(language);

        for (Runnable listener : LISTENERS) {
            try {
                listener.run();
            } catch (Throwable t) {
                LOGGER.error("Error notifying language listener", t);
            }
        }
    }

    public static synchronized void applyLanguage(OLanguage language) {
        Language current = Language.getInstance();
        if (current instanceof ODelegatingLanguage delegating) {
            baseLanguage = delegating.getParent();
        } else if (current != null) {
            baseLanguage = current;
        }

        if (language == OLanguage.AUTO) {
            activeTranslations.clear();
            if (baseLanguage != null && Language.getInstance() instanceof ODelegatingLanguage) {
                Language.inject(baseLanguage);
            }
            LOGGER.info("[O Lib] Switched language to Game Default (Auto).");
            return;
        }

        activeTranslations.clear();

        // 1. Load English fallback for all known mods
        for (String modId : knownModIds) {
            loadModLang(modId, "en_us", activeTranslations);
        }

        // 2. Load chosen language on top
        if (!language.getCode().equals("en_us")) {
            for (String modId : knownModIds) {
                loadModLang(modId, language.getCode(), activeTranslations);
            }
        }

        // 3. Inject wrapper
        if (baseLanguage != null) {
            ODelegatingLanguage wrapper = new ODelegatingLanguage(baseLanguage, activeTranslations);
            Language.inject(wrapper);
            LOGGER.info("[O Lib] Successfully injected O Lib language: {} ({} translation keys loaded)", language.name(), activeTranslations.size());
        }
    }

    public static void ensureActive() {
        if (currentLanguage != OLanguage.AUTO) {
            Language current = Language.getInstance();
            if (!(current instanceof ODelegatingLanguage)) {
                applyLanguage(currentLanguage);
            }
        }
    }

    private static void loadModLang(String modId, String langCode, Map<String, String> targetMap) {
        String resourcePath = "assets/" + modId + "/lang/" + langCode + ".json";

        // 1. Try FabricLoader findPath first
        try {
            var containerOpt = FabricLoader.getInstance().getModContainer(modId);
            if (containerOpt.isPresent()) {
                var pathOpt = containerOpt.get().findPath(resourcePath);
                if (pathOpt.isPresent() && Files.exists(pathOpt.get())) {
                    try (var reader = Files.newBufferedReader(pathOpt.get(), StandardCharsets.UTF_8)) {
                        JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                        for (var entry : obj.entrySet()) {
                            if (entry.getValue().isJsonPrimitive()) {
                                targetMap.put(entry.getKey(), entry.getValue().getAsString());
                            }
                        }
                        return;
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        // 2. Fallback to ClassLoader getResourceAsStream
        try {
            InputStream is = OLanguageManager.class.getResourceAsStream("/" + resourcePath);
            if (is == null) {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            }
            if (is != null) {
                try (InputStream stream = is; var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                    for (var entry : obj.entrySet()) {
                        if (entry.getValue().isJsonPrimitive()) {
                            targetMap.put(entry.getKey(), entry.getValue().getAsString());
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public static String get(String key) {
        return activeTranslations.get(key);
    }

    public static boolean has(String key) {
        return activeTranslations.containsKey(key);
    }

    public static void addChangeListener(Runnable listener) {
        if (listener != null) {
            LISTENERS.add(listener);
        }
    }

    public static void removeChangeListener(Runnable listener) {
        if (listener != null) {
            LISTENERS.remove(listener);
        }
    }
}