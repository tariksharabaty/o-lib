package com.oidioomstudio.olib.api.options;

import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorOption extends OOption<Integer> {
    private final boolean hasAlpha;

    public ColorOption(String id, Component name, Component description, Integer defaultValue,
                       boolean hasAlpha, Supplier<Integer> getter, Consumer<Integer> setter) {
        super(id, name, description, defaultValue, getter, setter);
        this.hasAlpha = hasAlpha;
    }

    public boolean hasAlpha() {
        return hasAlpha;
    }

    public String getHexFormatted() {
        int color = getValue();
        if (hasAlpha) {
            return String.format(Locale.ROOT, "#%08X", color);
        } else {
            return String.format(Locale.ROOT, "#%06X", color & 0x00FFFFFF);
        }
    }

    public static Builder builder(String id, Component name) {
        return new Builder(id, name);
    }

    public static class Builder {
        private final String id;
        private final Component name;
        private Component description;
        private int defaultValue = 0xFFFFFFFF;
        private boolean hasAlpha = false;
        private Supplier<Integer> getter;
        private Consumer<Integer> setter;

        public Builder(String id, Component name) {
            this.id = id;
            this.name = name;
        }

        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(int defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder hasAlpha(boolean hasAlpha) {
            this.hasAlpha = hasAlpha;
            return this;
        }

        public Builder getter(Supplier<Integer> getter) {
            this.getter = getter;
            return this;
        }

        public Builder setter(Consumer<Integer> setter) {
            this.setter = setter;
            return this;
        }

        public ColorOption build() {
            return new ColorOption(id, name, description, defaultValue, hasAlpha, getter, setter);
        }
    }
}
