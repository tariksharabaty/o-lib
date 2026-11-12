package com.oidioomstudio.olib;

import com.oidioomstudio.olib.api.OCreativeTabs;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OLibMod implements ModInitializer {
    public static final String MOD_ID = "olib";
    public static final String MOD_NAME = "O Lib";
    public static final String MOD_VERSION = "1.1.2";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("[{}] O Lib v{} initializing for Oidioom Studio mods...", MOD_NAME, MOD_VERSION);
        OCreativeTabs.register();
    }
}
