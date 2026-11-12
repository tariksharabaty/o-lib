package com.oidioomstudio.olib.client;

import com.oidioomstudio.olib.gui.OConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Standard base class for Oidioom Studio mods to integrate with ModMenu with a single line.
 */
public abstract class OModMenuApi implements ModMenuApi {
    private final String modId;

    public OModMenuApi() {
        this.modId = null;
    }

    public OModMenuApi(String modId) {
        this.modId = modId;
    }

    protected String getModId() {
        return this.modId;
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new OConfigScreen(parent, getModId());
    }
}
