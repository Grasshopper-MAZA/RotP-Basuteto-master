package com.grasshopper.bastet.event;

import com.grasshopper.bastet.BastetMain;
import com.grasshopper.bastet.magnet.MagnetizedData;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BastetMain.MOD_ID)
public class MagnetArmorLockHandler {

    private static final EquipmentSlotType[] ARMOR_SLOTS = new EquipmentSlotType[] {
            EquipmentSlotType.HEAD,
            EquipmentSlotType.CHEST,
            EquipmentSlotType.LEGS,
            EquipmentSlotType.FEET
    };

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        PlayerEntity player = event.player;
        if (player.level.isClientSide()) return;

        if (MagnetizedData.isMagnetized(player)) {
            lockArmor(player);
        } else {
            unlockArmor(player);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.level.isClientSide()) return;
        if (!(entity instanceof MobEntity)) return;

        if (MagnetizedData.isMagnetized(entity)) {
            lockMobArmor(entity);
        }
    }

    private static void lockArmor(PlayerEntity player) {
        for (EquipmentSlotType slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!isIronArmor(stack)) continue;

            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BINDING_CURSE, stack) <= 0) {
                stack.enchant(Enchantments.BINDING_CURSE, 1);
                player.setItemSlot(slot, stack);
            }
        }
    }

    private static void unlockArmor(PlayerEntity player) {
        for (EquipmentSlotType slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;

            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BINDING_CURSE, stack) > 0) {
                ItemStack copy = stack.copy();
                EnchantmentHelper.setEnchantments(
                        removeCurse(EnchantmentHelper.getEnchantments(copy)),
                        copy
                );
                player.setItemSlot(slot, copy);
            }
        }
    }

    private static java.util.Map<net.minecraft.enchantment.Enchantment, Integer> removeCurse(
            java.util.Map<net.minecraft.enchantment.Enchantment, Integer> map) {
        java.util.Map<net.minecraft.enchantment.Enchantment, Integer> result = new java.util.HashMap<>(map);
        result.remove(Enchantments.BINDING_CURSE);
        return result;
    }

    private static void lockMobArmor(LivingEntity entity) {
        for (EquipmentSlotType slot : ARMOR_SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!isIronArmor(stack)) continue;

            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BINDING_CURSE, stack) <= 0) {
                stack.enchant(Enchantments.BINDING_CURSE, 1);
                entity.setItemSlot(slot, stack);
            }
        }
    }

    private static boolean isIronArmor(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() == Items.IRON_HELMET
                || stack.getItem() == Items.IRON_CHESTPLATE
                || stack.getItem() == Items.IRON_LEGGINGS
                || stack.getItem() == Items.IRON_BOOTS
                || stack.getItem() == Items.CHAINMAIL_HELMET
                || stack.getItem() == Items.CHAINMAIL_CHESTPLATE
                || stack.getItem() == Items.CHAINMAIL_LEGGINGS
                || stack.getItem() == Items.CHAINMAIL_BOOTS;
    }
}