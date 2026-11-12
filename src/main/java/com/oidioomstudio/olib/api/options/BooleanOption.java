package com.oidioomstudio.olib.api.options;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BooleanOption extends OOption<Boolean> {
    private final Component trueLabel;
    private final Component falseLabel;
    private final boolean actionButton;

    public BooleanOption(String id, Component name, Component description, Boolean defaultValue,
                         Supplier<Boolean> getter, Consumer<Boolean> setter,
                         Component trueLabel, Component falseLabel) {
        this(id, name, description, defaultValue, getter, setter, trueLabel, falseLabel, null, false);
    }

    public BooleanOption(String id, Component name, Component description, Boolean defaultValue,
                         Supplier<Boolean> getter, Consumer<Boolean> setter,
                         Component trueLabel, Component falseLabel,
                         Supplier<ItemStack> iconSupplier) {
        this(id, name, description, defaultValue, getter, setter, trueLabel, falseLabel, iconSupplier, false);
    }

    public BooleanOption(String id, Component name, Component description, Boolean defaultValue,
                         Supplier<Boolean> getter, Consumer<Boolean> setter,
                         Component trueLabel, Component falseLabel,
                         Supplier<ItemStack> iconSupplier, boolean actionButton) {
        super(id, name, description, defaultValue, getter, setter, iconSupplier);
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
        this.actionButton = actionButton;
    }

    public boolean isActionButton() {
        return actionButton || (trueLabel != null && falseLabel != null && trueLabel.getString().equals(falseLabel.getString()));
    }

    public Component getTrueLabel() {
        return trueLabel != null ? trueLabel : Component.translatable("olib.config.enabled");
    }

    public Component getFalseLabel() {
        return falseLabel != null ? falseLabel : Component.translatable("olib.config.disabled");
    }

    public boolean hasCustomLabels() {
        return trueLabel != null || falseLabel != null;
    }

    public void toggle() {
        setValue(!getValue());
    }

    public static Builder builder(String id, Component name) {
        return new Builder(id, name);
    }

    public static class Builder {
        private final String id;
        private final Component name;
        private Component description;
        private Boolean defaultValue = false;
        private Supplier<Boolean> getter;
        private Consumer<Boolean> setter;
        private Component trueLabel;
        private Component falseLabel;
        private Supplier<ItemStack> iconSupplier;
        private boolean actionButton = false;

        public Builder(String id, Component name) {
            this.id = id;
            this.name = name;
        }

        public Builder actionButton(boolean actionButton) {
            this.actionButton = actionButton;
            return this;
        }

        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(Boolean defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder getter(Supplier<Boolean> getter) {
            this.getter = getter;
            return this;
        }

        public Builder setter(Consumer<Boolean> setter) {
            this.setter = setter;
            return this;
        }

        public Builder labels(Component trueLabel, Component falseLabel) {
            this.trueLabel = trueLabel;
            this.falseLabel = falseLabel;
            return this;
        }

        public Builder icon(Item item) {
            this.iconSupplier = new Supplier<ItemStack>() {
                private ItemStack cached = null;

                @Override
                public ItemStack get() {
                    if (cached != null && !cached.isEmpty()) {
                        return cached;
                    }
                    if (item == null) {
                        return ItemStack.EMPTY;
                    }
                    try {
                        if (item.builtInRegistryHolder().areComponentsBound()) {
                            cached = new ItemStack(item);
                            return cached;
                        }
                    } catch (Throwable ignored) {}

                    try {
                        net.minecraft.core.component.PatchedDataComponentMap map = new net.minecraft.core.component.PatchedDataComponentMap(net.minecraft.core.component.DataComponentMap.EMPTY);
                        map.set(net.minecraft.core.component.DataComponents.ITEM_MODEL, net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item));
                        java.lang.reflect.Constructor<ItemStack> ctor = ItemStack.class.getDeclaredConstructor(net.minecraft.core.Holder.class, int.class, net.minecraft.core.component.PatchedDataComponentMap.class);
                        ctor.setAccessible(true);
                        cached = ctor.newInstance(item.builtInRegistryHolder(), 1, map);
                        return cached;
                    } catch (Throwable ignored) {}

                    return ItemStack.EMPTY;
                }
            };
            return this;
        }

        public Builder icon(Supplier<ItemStack> iconSupplier) {
            this.iconSupplier = iconSupplier;
            return this;
        }

        public BooleanOption build() {
            return new BooleanOption(id, name, description, defaultValue, getter, setter, trueLabel, falseLabel, iconSupplier, actionButton);
        }
    }
}
