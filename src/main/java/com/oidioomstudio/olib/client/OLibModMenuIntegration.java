package com.oidioomstudio.olib.client;

import com.oidioomstudio.olib.api.OConfigRegistry;
import com.oidioomstudio.olib.gui.OConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.util.HashMap;
import java.util.Map;

public class OLibModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return OConfigScreen::new;
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        Map<String, ConfigScreenFactory<?>> factories = new HashMap<>();
        for (String modId : OConfigRegistry.getRegisteredModIds()) {
            if (!"olib".equalsIgnoreCase(modId)) {
                factories.put(modId, parent -> new OConfigScreen(parent, modId));
            }
        }
        return factories;
    }
}

