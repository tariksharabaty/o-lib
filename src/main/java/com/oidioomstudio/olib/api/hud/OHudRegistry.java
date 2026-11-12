package com.oidioomstudio.olib.api.hud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry and manager for all modular HUD elements in O Lib.
 */
public final class OHudRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger("O Lib/HUD");
    private static final Map<String, OHudElement> ELEMENTS = new LinkedHashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/olib/hud_layout.json");
    private static boolean registeredToFabric = false;

    private OHudRegistry() {}

    public static synchronized void register(OHudElement element) {
        if (element == null) return;
        ELEMENTS.put(element.getId(), element);
        loadLayoutFor(element);
        ensureFabricRegistration();
    }

    public static Collection<OHudElement> getAll() {
        return Collections.unmodifiableCollection(ELEMENTS.values());
    }

    public static OHudElement get(String id) {
        return ELEMENTS.get(id);
    }

    public static void ensureFabricRegistration() {
        if (registeredToFabric) return;
        registeredToFabric = true;
        try {
            HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("olib", "hud_elements"), (extractor, deltaTracker) -> {
                for (OHudElement element : ELEMENTS.values()) {
                    if (element.isVisible()) {
                        element.render(extractor, deltaTracker);
                    }
                }
            });
        } catch (Throwable t) {
            LOGGER.error("Failed to register O Lib HUD elements to Fabric HudElementRegistry", t);
        }
    }

    public static synchronized void saveLayout() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }

            JsonObject root = new JsonObject();
            for (OHudElement element : ELEMENTS.values()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("x", element.getX());
                obj.addProperty("y", element.getY());
                obj.addProperty("width", element.getWidth());
                obj.addProperty("height", element.getHeight());
                obj.addProperty("visible", element.isVisible());
                root.add(element.getId(), obj);
            }

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Could not save O Lib HUD layout to {}", CONFIG_FILE, e);
        }
    }

    public static synchronized void loadLayout() {
        if (!CONFIG_FILE.exists()) return;
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (OHudElement element : ELEMENTS.values()) {
                if (root.has(element.getId())) {
                    JsonObject obj = root.getAsJsonObject(element.getId());
                    if (obj.has("x")) element.setX(obj.get("x").getAsInt());
                    if (obj.has("y")) element.setY(obj.get("y").getAsInt());
                    if (obj.has("width")) element.setWidth(obj.get("width").getAsInt());
                    if (obj.has("height")) element.setHeight(obj.get("height").getAsInt());
                    if (obj.has("visible")) element.setVisible(obj.get("visible").getAsBoolean());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Could not load O Lib HUD layout from {}", CONFIG_FILE, e);
        }
    }

    private static void loadLayoutFor(OHudElement element) {
        if (!CONFIG_FILE.exists()) return;
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.has(element.getId())) {
                JsonObject obj = root.getAsJsonObject(element.getId());
                if (obj.has("x")) element.setX(obj.get("x").getAsInt());
                if (obj.has("y")) element.setY(obj.get("y").getAsInt());
                if (obj.has("width")) element.setWidth(obj.get("width").getAsInt());
                if (obj.has("height")) element.setHeight(obj.get("height").getAsInt());
                if (obj.has("visible")) element.setVisible(obj.get("visible").getAsBoolean());
            }
        } catch (Exception ignored) {}
    }

    public static synchronized void resetAllToDefaults() {
        for (OHudElement element : ELEMENTS.values()) {
            element.resetToDefault();
        }
        saveLayout();
    }
}
