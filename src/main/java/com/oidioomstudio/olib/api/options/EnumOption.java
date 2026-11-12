package com.oidioomstudio.olib.api.options;

import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EnumOption<E extends Enum<E>> extends OOption<E> {
    private final Class<E> enumClass;
    private final E[] values;
    private final Function<E, Component> nameMapper;

    public EnumOption(String id, Component name, Component description, E defaultValue, Class<E> enumClass,
                      Function<E, Component> nameMapper, Supplier<E> getter, Consumer<E> setter) {
        super(id, name, description, defaultValue, getter, setter);
        this.enumClass = enumClass;
        this.values = enumClass.getEnumConstants();
        this.nameMapper = nameMapper != null ? nameMapper : (e -> Component.literal(e.name()));
    }

    public Class<E> getEnumClass() {
        return enumClass;
    }

    public E[] getValues() {
        return values;
    }

    public Component getDisplayName(E value) {
        return nameMapper.apply(value);
    }

    public Component getCurrentDisplayName() {
        return getDisplayName(getValue());
    }

    public void cycle() {
        E current = getValue();
        int nextOrdinal = (current.ordinal() + 1) % values.length;
        setValue(values[nextOrdinal]);
    }

    public void cyclePrevious() {
        E current = getValue();
        int prevOrdinal = (current.ordinal() - 1 + values.length) % values.length;
        setValue(values[prevOrdinal]);
    }

    public static <E extends Enum<E>> Builder<E> builder(String id, Component name, Class<E> enumClass) {
        return new Builder<>(id, name, enumClass);
    }

    public static class Builder<E extends Enum<E>> {
        private final String id;
        private final Component name;
        private final Class<E> enumClass;
        private Component description;
        private E defaultValue;
        private Function<E, Component> nameMapper;
        private Supplier<E> getter;
        private Consumer<E> setter;

        public Builder(String id, Component name, Class<E> enumClass) {
            this.id = id;
            this.name = name;
            this.enumClass = enumClass;
            E[] constants = enumClass.getEnumConstants();
            if (constants.length > 0) {
                this.defaultValue = constants[0];
            }
        }

        public Builder<E> description(Component description) {
            this.description = description;
            return this;
        }

        public Builder<E> defaultValue(E defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder<E> nameMapper(Function<E, Component> nameMapper) {
            this.nameMapper = nameMapper;
            return this;
        }

        public Builder<E> getter(Supplier<E> getter) {
            this.getter = getter;
            return this;
        }

        public Builder<E> setter(Consumer<E> setter) {
            this.setter = setter;
            return this;
        }

        public EnumOption<E> build() {
            return new EnumOption<>(id, name, description, defaultValue, enumClass, nameMapper, getter, setter);
        }
    }
}
