package com.oidioomstudio.olib.api.options;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Base abstract class for all configuration options in O Lib.
 *
 * @param <T> The option value type
 */
public abstract class OOption<T> {
    private final String id;
    private final Component name;
    private final Component description;
    private final T defaultValue;
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private Supplier<ItemStack> iconSupplier;

    public OOption(String id, Component name, Component description, T defaultValue, Supplier<T> getter, Consumer<T> setter) {
        this(id, name, description, defaultValue, getter, setter, null);
    }

    public OOption(String id, Component name, Component description, T defaultValue, Supplier<T> getter, Consumer<T> setter, Supplier<ItemStack> iconSupplier) {
        this.id = Objects.requireNonNull(id, "Option ID cannot be null");
        this.name = Objects.requireNonNull(name, "Option name cannot be null");
        this.description = description;
        this.defaultValue = defaultValue;
        this.getter = Objects.requireNonNull(getter, "Option getter cannot be null");
        this.setter = Objects.requireNonNull(setter, "Option setter cannot be null");
        this.iconSupplier = iconSupplier;
    }

    public String getId() {
        return id;
    }

    public Component getName() {
        return name;
    }

    public Component getDescription() {
        return description;
    }

    private Component groupName;

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getValue() {
        return getter.get();
    }

    public void setValue(T value) {
        setter.accept(value);
    }

    public ItemStack getIcon() {
        if (iconSupplier != null) {
            try {
                ItemStack stack = iconSupplier.get();
                return stack != null ? stack : ItemStack.EMPTY;
            } catch (Throwable ignored) {}
        }
        return ItemStack.EMPTY;
    }

    public boolean hasIcon() {
        return iconSupplier != null;
    }

    public void setIconSupplier(Supplier<ItemStack> iconSupplier) {
        this.iconSupplier = iconSupplier;
    }

    public Component getGroupName() {
        return groupName;
    }

    public void setGroupName(Component groupName) {
        this.groupName = groupName;
    }

    public boolean isChanged() {
        return !Objects.equals(getValue(), defaultValue);
    }

    public void reset() {
        setValue(defaultValue);
    }

    public boolean matchesSearch(String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String q = query.trim().toLowerCase();
        String qRoot = query.trim().toLowerCase(Locale.ROOT);

        String nameStr = name.getString().toLowerCase();
        String nameRoot = name.getString().toLowerCase(Locale.ROOT);
        if (nameStr.contains(q) || nameRoot.contains(qRoot)) {
            return true;
        }

        if (id.toLowerCase().contains(q) || id.toLowerCase(Locale.ROOT).contains(qRoot)) {
            return true;
        }

        if (groupName != null) {
            String grpStr = groupName.getString().toLowerCase();
            String grpRoot = groupName.getString().toLowerCase(Locale.ROOT);
            if (grpStr.contains(q) || grpRoot.contains(qRoot)) {
                return true;
            }
        }

        if (description != null) {
            String descStr = description.getString().toLowerCase();
            String descRoot = description.getString().toLowerCase(Locale.ROOT);
            return descStr.contains(q) || descRoot.contains(qRoot);
        }

        return false;
    }
}
