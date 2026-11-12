package com.oidioomstudio.olib.api.i18n;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.Map;
import java.util.Objects;

/**
 * A delegating Language implementation that overrides Oidioom Studio mod translations
 * while perfectly preserving vanilla Minecraft translations.
 */
public class ODelegatingLanguage extends Language {
    private final Language parent;
    private final Map<String, String> translations;

    public ODelegatingLanguage(Language parent, Map<String, String> translations) {
        this.parent = Objects.requireNonNull(parent, "Parent language cannot be null");
        this.translations = translations;
    }

    public Language getParent() {
        return parent;
    }

    @Override
    public String getOrDefault(String key, String defaultValue) {
        if (translations != null && translations.containsKey(key)) {
            return translations.get(key);
        }
        return parent.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean has(String key) {
        if (translations != null && translations.containsKey(key)) {
            return true;
        }
        return parent.has(key);
    }

    @Override
    public boolean isDefaultRightToLeft() {
        return parent.isDefaultRightToLeft();
    }

    @Override
    public FormattedCharSequence getVisualOrder(FormattedText text) {
        return parent.getVisualOrder(text);
    }
}
