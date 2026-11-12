package com.oidioomstudio.olib.api.options;

import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringOption extends OOption<String> {

    public StringOption(String id, Component name, Component description, String defaultValue,
                        Supplier<String> getter, Consumer<String> setter) {
        super(id, name, description, defaultValue != null ? defaultValue : "", getter, setter);
    }

    public static Builder builder(String id, Component name) {
        return new Builder(id, name);
    }

    public static class Builder {
        private final String id;
        private final Component name;
        private Component description;
        private String defaultValue = "";
        private Supplier<String> getter;
        private Consumer<String> setter;

        public Builder(String id, Component name) {
            this.id = id;
            this.name = name;
        }

        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder getter(Supplier<String> getter) {
            this.getter = getter;
            return this;
        }

        public Builder setter(Consumer<String> setter) {
            this.setter = setter;
            return this;
        }

        public StringOption build() {
            return new StringOption(id, name, description, defaultValue, getter, setter);
        }
    }
}
