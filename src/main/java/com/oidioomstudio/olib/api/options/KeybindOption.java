package com.oidioomstudio.olib.api.options;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class KeybindOption extends OOption<InputConstants.Key> {

    public KeybindOption(String id, Component name, Component description, InputConstants.Key defaultValue,
                         Supplier<InputConstants.Key> getter, Consumer<InputConstants.Key> setter) {
        super(id, name, description, defaultValue != null ? defaultValue : InputConstants.UNKNOWN, getter, setter);
    }

    public Component getKeyDisplayName() {
        InputConstants.Key key = getValue();
        if (key == null || key == InputConstants.UNKNOWN) {
            return Component.translatable("olib.config.key_none");
        }
        return key.getDisplayName();
    }

    public static Builder builder(String id, Component name) {
        return new Builder(id, name);
    }

    public static class Builder {
        private final String id;
        private final Component name;
        private Component description;
        private InputConstants.Key defaultValue = InputConstants.UNKNOWN;
        private Supplier<InputConstants.Key> getter;
        private Consumer<InputConstants.Key> setter;

        public Builder(String id, Component name) {
            this.id = id;
            this.name = name;
        }

        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(InputConstants.Key defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder defaultValue(int keyCode) {
            this.defaultValue = InputConstants.Type.KEYBOARD.getOrCreate(keyCode);
            return this;
        }

        public Builder getter(Supplier<InputConstants.Key> getter) {
            this.getter = getter;
            return this;
        }

        public Builder setter(Consumer<InputConstants.Key> setter) {
            this.setter = setter;
            return this;
        }

        public KeybindOption build() {
            return new KeybindOption(id, name, description, defaultValue, getter, setter);
        }
    }
}
