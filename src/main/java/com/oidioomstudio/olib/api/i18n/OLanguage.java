package com.oidioomstudio.olib.api.i18n;

import net.minecraft.network.chat.Component;

/**
 * Supported UI languages for O Lib and Oidioom Studio mods.
 */
public enum OLanguage {
    AUTO("auto", "olib.lang.auto", "Mod Dili", "Mod Language"),
    TR_TR("tr_tr", "olib.lang.tr_tr", "Türkçe (TR)", "Turkish (TR)"),
    EN_US("en_us", "olib.lang.en_us", "English (US)", "English (US)"),
    DE_DE("de_de", "olib.lang.de_de", "Deutsch (DE)", "German (DE)"),
    ES_ES("es_es", "olib.lang.es_es", "Español (ES)", "Spanish (ES)"),
    FR_FR("fr_fr", "olib.lang.fr_fr", "Français (FR)", "French (FR)"),
    RU_RU("ru_ru", "olib.lang.ru_ru", "Русский (RU)", "Russian (RU)"),
    PT_BR("pt_br", "olib.lang.pt_br", "Português (BR)", "Portuguese (BR)"),
    JA_JP("ja_jp", "olib.lang.ja_jp", "日本語 (JP)", "Japanese (JP)"),
    ZH_CN("zh_cn", "olib.lang.zh_cn", "简体中文 (CN)", "Chinese Simplified (CN)"),
    KO_KR("ko_kr", "olib.lang.ko_kr", "한국어 (KR)", "Korean (KR)");

    private final String code;
    private final String translationKey;
    private final String nativeName;
    private final String englishName;

    OLanguage(String code, String translationKey, String nativeName, String englishName) {
        this.code = code;
        this.translationKey = translationKey;
        this.nativeName = nativeName;
        this.englishName = englishName;
    }

    public String getCode() {
        return code;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public String getNativeName() {
        return nativeName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public Component getDisplayName() {
        return Component.literal(nativeName);
    }

    public Component getButtonLabel() {
        return Component.literal(nativeName);
    }

    public static OLanguage fromCode(String code) {
        if (code == null || code.isBlank() || code.equalsIgnoreCase("system") || code.equalsIgnoreCase("auto")) return AUTO;
        for (OLanguage lang : values()) {
            if (lang.code.equalsIgnoreCase(code)) {
                return lang;
            }
        }
        return AUTO;
    }
}