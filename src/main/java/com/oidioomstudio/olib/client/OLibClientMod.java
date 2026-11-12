package com.oidioomstudio.olib.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.oidioomstudio.olib.OLibMod;
import com.oidioomstudio.olib.api.OConfigCategory;
import com.oidioomstudio.olib.api.OConfigRegistry;
import com.oidioomstudio.olib.api.OModConfig;
import com.oidioomstudio.olib.api.i18n.OLanguage;
import com.oidioomstudio.olib.api.i18n.OLanguageManager;
import com.oidioomstudio.olib.api.options.*;
import com.oidioomstudio.olib.config.OConfigHolder;
import com.oidioomstudio.olib.gui.OConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class OLibClientMod implements ClientModInitializer {
    public static final KeyMapping.Category OLIB_CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("olib", "main")
    );

    public static final KeyMapping MENU_KEY_MAPPING = new KeyMapping(
        "key.olib.global_menu",
        InputConstants.Type.KEYBOARD,
        InputConstants.KEY_F8,
        OLIB_CATEGORY
    );

    public static class Preferences {
        public boolean smoothAnimations = true;
        public boolean showResetButtons = true;
        public int backgroundOpacityPercent = 45;
        public boolean autoSave = true;
        public int primaryAccentColor = 0xFFFFFFFF;
        public String languageCode = "auto";
        public String globalMenuKeyName = MENU_KEY_MAPPING.getDefaultKey().getName();
    }

    private static final OConfigHolder<Preferences> PREFS_HOLDER = new OConfigHolder<>(
        "olib",
        Preferences.class,
        Preferences::new
    );

    // O Lib user preferences
    public static boolean smoothAnimations = true;
    public static boolean showResetButtons = true;
    public static int backgroundOpacityPercent = 45; // Default: clean, transparent, light stone
    public static boolean autoSave = true;
    public static int primaryAccentColor = 0xFFFFFFFF;
    public static OLanguage modLanguage = OLanguage.AUTO;

    public static void loadPreferences() {
        Preferences p = PREFS_HOLDER.load();
        smoothAnimations = p.smoothAnimations;
        showResetButtons = p.showResetButtons;
        backgroundOpacityPercent = Math.max(0, Math.min(100, p.backgroundOpacityPercent));
        autoSave = p.autoSave;
        primaryAccentColor = p.primaryAccentColor;
        modLanguage = OLanguage.fromCode(p.languageCode);
    }

    public static void savePreferences() {
        Preferences p = PREFS_HOLDER.get();
        p.smoothAnimations = smoothAnimations;
        p.showResetButtons = showResetButtons;
        p.backgroundOpacityPercent = backgroundOpacityPercent;
        p.autoSave = autoSave;
        p.primaryAccentColor = primaryAccentColor;
        p.languageCode = modLanguage.getCode();
        InputConstants.Key currentBound = KeyMappingHelper.getBoundKeyOf(MENU_KEY_MAPPING);
        p.globalMenuKeyName = (currentBound != null) ? currentBound.getName() : MENU_KEY_MAPPING.getDefaultKey().getName();
        PREFS_HOLDER.save();
    }

    public static void setLanguage(OLanguage lang) {
        modLanguage = lang != null ? lang : OLanguage.AUTO;
        OLanguageManager.setLanguage(modLanguage);
        savePreferences();
    }

    @Override
    public void onInitializeClient() {
        OLibMod.LOGGER.info("[{}] O Lib client initializing. Loading preferences...", OLibMod.MOD_NAME);

        // Register key binding into vanilla Minecraft Controls (Options -> Controls -> Key Binds)
        KeyMappingHelper.registerKeyMapping(MENU_KEY_MAPPING);

        loadPreferences();

        // Initialize language override system
        OLanguageManager.init();
        OLanguageManager.setLanguage(modLanguage);

        // Client tick event to open the menu when the key mapping is activated
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            while (MENU_KEY_MAPPING.consumeClick()) {
                if (client.gui.screen() == null) {
                    client.gui.setScreen(new OConfigScreen(null));
                }
            }
        });

        // Register O Lib itself into OConfigRegistry
        OConfigRegistry.register(
            OModConfig.builder("olib", Component.translatable("olib.name"), OLibMod.MOD_VERSION)
                .category(OConfigCategory.builder("general", Component.translatable("olib.config.tab.general"))
                    .add(KeybindOption.builder("global_menu_key", Component.translatable("key.olib.global_menu"))
                        .description(Component.translatable("olib.config.global_menu_key.desc"))
                        .defaultValue(MENU_KEY_MAPPING.getDefaultKey())
                        .getter(() -> KeyMappingHelper.getBoundKeyOf(MENU_KEY_MAPPING))
                        .setter(val -> {
                            InputConstants.Key newKey = (val != null) ? val : InputConstants.UNKNOWN;
                            MENU_KEY_MAPPING.setKey(newKey);
                            KeyMapping.resetMapping();
                            Minecraft mc = Minecraft.getInstance();
                            if (mc != null && mc.options != null) {
                                mc.options.save();
                            }
                            if (autoSave) savePreferences();
                        })
                        .build())
                    .add(BooleanOption.builder("auto_save", Component.translatable("olib.config.auto_save"))
                        .description(Component.translatable("olib.config.auto_save.desc"))
                        .defaultValue(true)
                        .getter(() -> autoSave)
                        .setter(val -> {
                            autoSave = val;
                            if (autoSave) savePreferences();
                        })
                        .build())
                    .add(BooleanOption.builder("smooth_animations", Component.translatable("olib.config.smooth_animations"))
                        .description(Component.translatable("olib.config.smooth_animations.desc"))
                        .defaultValue(true)
                        .getter(() -> smoothAnimations)
                        .setter(val -> {
                            smoothAnimations = val;
                            if (autoSave) savePreferences();
                        })
                        .build())
                    .add(BooleanOption.builder("show_reset_buttons", Component.translatable("olib.config.show_reset_buttons"))
                        .description(Component.translatable("olib.config.show_reset_buttons.desc"))
                        .defaultValue(true)
                        .getter(() -> showResetButtons)
                        .setter(val -> {
                            showResetButtons = val;
                            if (autoSave) savePreferences();
                        })
                        .build())
                    .build())
                .category(OConfigCategory.builder("appearance", Component.translatable("olib.config.tab.graphics"))
                    .add(IntegerSliderOption.builder("bg_opacity", Component.translatable("olib.config.bg_opacity"))
                        .description(Component.translatable("olib.config.bg_opacity.desc"))
                        .range(0, 100, 5)
                        .defaultValue(45)
                        .formatter(val -> val == 0 ? Component.translatable("olib.config.transparent") : Component.literal(val + "%"))
                        .getter(() -> backgroundOpacityPercent)
                        .setter(val -> {
                            backgroundOpacityPercent = val;
                            if (autoSave) savePreferences();
                        })
                        .build())
                    .add(ColorOption.builder("accent_color", Component.translatable("olib.config.accent_color"))
                        .description(Component.translatable("olib.config.accent_color.desc"))
                        .defaultValue(0xFFFFFFFF)
                        .getter(() -> primaryAccentColor)
                        .setter(val -> {
                            primaryAccentColor = val;
                            if (autoSave) savePreferences();
                        })
                        .build())
                    .build())
                .onSave(OLibClientMod::savePreferences)
                .build()
        );
    }
}
