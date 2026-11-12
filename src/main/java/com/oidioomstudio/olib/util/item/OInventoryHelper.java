package com.oidioomstudio.olib.util.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Common inventory, item drop, and item consumption utilities across Oidioom Studio mods.
 */
public final class OInventoryHelper {
    private OInventoryHelper() {}

    /**
     * Attempts to place an item stack directly into the player's inventory;
     * drops remaining items at the player's feet if inventory is full.
     */
    public static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) return;

        boolean added = player.getInventory().add(stack);
        if (!added || !stack.isEmpty()) {
            ItemEntity entity = new ItemEntity(player.level(), player.getX(), player.getY() + 0.5, player.getZ(), stack);
            entity.setNoPickUpDelay();
            player.level().addFreshEntity(entity);
        }
    }

    /**
     * Checks if a tool can safely be used without breaking (preserving at least 1 durability point).
     */
    public static boolean isSafeToDamage(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.isDamageableItem()) {
            return true;
        }
        return (stack.getMaxDamage() - stack.getDamageValue()) > 1;
    }

    /**
     * Counts the total amount of the specified item across the player's inventory.
     * Returns Integer.MAX_VALUE if the player is in creative mode.
     */
    public static int countItem(Player player, Item item) {
        if (player == null || item == null) return 0;
        if (player.isCreative()) return Integer.MAX_VALUE;

        Inventory inv = player.getInventory();
        int count = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Consumes the requested amount of an item from the player's inventory.
     * Always succeeds (returning true) if the player is in creative mode.
     */
    public static boolean consumeItem(Player player, Item item, int amount) {
        if (player == null || item == null || amount <= 0) return false;
        if (player.isCreative()) return true;

        if (countItem(player, item) < amount) {
            return false;
        }

        Inventory inv = player.getInventory();
        int remaining = amount;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                int toTake = Math.min(remaining, stack.getCount());
                stack.shrink(toTake);
                if (stack.isEmpty()) {
                    inv.setItem(i, ItemStack.EMPTY);
                }
                remaining -= toTake;
                if (remaining <= 0) {
                    return true;
                }
            }
        }
        return remaining <= 0;
    }

    /**
     * Damages the item currently held in the specified hand safely.
     */
    public static void damageHeldItem(Player player, InteractionHand hand, int amount) {
        if (player == null || amount <= 0 || player.isCreative()) return;

        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty() && stack.isDamageableItem()) {
            EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(amount, player, slot);
        }
    }
}
