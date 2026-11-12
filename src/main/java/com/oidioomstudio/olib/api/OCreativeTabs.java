package com.oidioomstudio.olib.api;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class OCreativeTabs {
    public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath("olib", "oidioom_tab")
    );

    private static Supplier<ItemStack> iconSupplier = () -> new ItemStack(Items.AMETHYST_CLUSTER);
    private static final List<Supplier<ItemStack>> TAB_ITEMS = new ArrayList<>();
    public static CreativeModeTab TAB;

    private OCreativeTabs() {}

    public static void setIcon(Supplier<ItemStack> supplier) {
        if (supplier != null) {
            iconSupplier = supplier;
        }
    }

    public static void accept(ItemLike item) {
        if (item != null) {
            TAB_ITEMS.add(() -> new ItemStack(item));
        }
    }

    public static void accept(Supplier<ItemStack> supplier) {
        if (supplier != null) {
            TAB_ITEMS.add(supplier);
        }
    }

    public static void register() {
        TAB = FabricCreativeModeTab.builder()
                .icon(() -> iconSupplier.get())
                .title(Component.translatable("itemGroup.olib.oidioom_tab"))
                .displayItems((itemDisplayParameters, output) -> {
                    for (Supplier<ItemStack> supplier : TAB_ITEMS) {
                        ItemStack stack = supplier.get();
                        if (stack != null && !stack.isEmpty()) {
                            output.accept(stack);
                        }
                    }
                })
                .build();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, TAB);
    }
}

