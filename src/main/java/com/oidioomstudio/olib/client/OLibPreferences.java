package com.oidioomstudio.olib.client;

import com.oidioomstudio.olib.api.i18n.OLanguage;

/**
 * Persistent preferences for O Lib client configuration.
 */
public class OLibPreferences {
    public OLanguage language = OLanguage.AUTO;
    public boolean smoothAnimations = true;
    public boolean showResetButtons = true;
    public int backgroundOpacityPercent = 45;
    public boolean autoSave = true;
    public String globalMenuKey = "key.keyboard.f8";
    public int primaryAccentColor = 0xFFFFFFFF;
}
