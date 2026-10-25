package com.grasshopper.bastet.magnet;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.grasshopper.bastet.BastetMain;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BastetMain.MOD_ID)
public class MagnetizedData {

    public static final int MAX_DURATION = 2400;
    private static final double MAX_RANGE = 20.0D;

    public static final Map<UUID, Integer> MAGNETIZED = new HashMap<>();

    public static boolean isMagnetized(LivingEntity entity) {
        return MAGNETIZED.containsKey(entity.getUUID());
    }

    public static int getDuration(LivingEntity entity) {
        Integer t = MAGNETIZED.get(entity.getUUID());
        return t == null ? 0 : t;
    }

    public static float getProgress(LivingEntity entity) {
        int t = getDuration(entity);
        if (t <= 0) return 0F;
        return 1F - (float) t / MAX_DURATION;
    }

    public static void magnetize(LivingEntity entity) {
        MAGNETIZED.put(entity.getUUID(), MAX_DURATION);
    }

    public static void clearMagnetization(LivingEntity entity) {
        MAGNETIZED.remove(entity.getUUID());
        MagnetForceHandler.releaseItems(entity.getUUID());
    }

    public static void saveToNBT(CompoundNBT nbt) {
        CompoundNBT map = new CompoundNBT();
        MAGNETIZED.forEach((uuid, ticks) -> map.putInt(uuid.toString(), ticks));
        nbt.put("BastetMagnetized", map);
    }

    public static void loadFromNBT(CompoundNBT nbt) {
        MAGNETIZED.clear();
        if (nbt.contains("BastetMagnetized")) {
            CompoundNBT map = nbt.getCompound("BastetMagnetized");
            for (String key : map.getAllKeys()) {
                try {
                    MAGNETIZED.put(UUID.fromString(key), map.getInt(key));
                } catch (Exception ignored) {}
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity.level.isClientSide()) return;

        if (!entity.isAlive()) {
            MAGNETIZED.remove(entity.getUUID());
            MagnetForceHandler.releaseItems(entity.getUUID());
            return;
        }

        UUID uuid = entity.getUUID();
        Integer time = MAGNETIZED.get(uuid);
        if (time == null) return;

        if (time <= 0) {
            MAGNETIZED.remove(uuid);
            MagnetForceHandler.releaseItems(uuid);
            return;
        }

        if (!isWithinBastetRange(entity)) {
            MAGNETIZED.remove(uuid);
            MagnetForceHandler.releaseItems(uuid);
            return;
        }

        MAGNETIZED.put(uuid, time - 1);
        MagnetForceHandler.applyMagnetForce(entity);
    }

    private static boolean isWithinBastetRange(LivingEntity entity) {
        if (!(entity.level instanceof ServerWorld)) return false;
        ServerWorld world = (ServerWorld) entity.level;

        for (ServerPlayerEntity player : world.getPlayers(p -> true)) {
            if (player.distanceToSqr(entity) > MAX_RANGE * MAX_RANGE) continue;

            try {
                IStandPower power = IStandPower.getPlayerStandPower(player);
                if (power == null || !power.hasPower()) continue;
                if (power.getType() == null) continue;

                String name = power.getType().getRegistryName() != null
                        ? power.getType().getRegistryName().toString()
                        : "";

                if (name.contains("bastet")) {
                    return true;
                }
            } catch (Exception ignored) {}
        }

        return false;
    }
}