package com.grasshopper.bastet.event;

import java.util.UUID;

import com.grasshopper.bastet.BastetMain;
import com.grasshopper.bastet.magnet.MagnetizedData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BastetMain.MOD_ID)
public class MagnetItemRecoveryHandler {

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.isClientSide()) return;
        if (!(event.world instanceof ServerWorld)) return;

        ServerWorld world = (ServerWorld) event.world;

        if (world.getGameTime() % 20 != 0) return;

        for (Entity entity : world.getAllEntities()) {
            if (!(entity instanceof ItemEntity)) continue;

            ItemEntity item = (ItemEntity) entity;
            CompoundNBT nbt = item.getPersistentData();

            if (!nbt.hasUUID("BastetMagnetOwner")) continue;

            UUID ownerId = nbt.getUUID("BastetMagnetOwner");
            Entity owner = world.getEntity(ownerId);

            boolean ownerValid = owner instanceof LivingEntity
                    && owner.isAlive()
                    && MagnetizedData.isMagnetized((LivingEntity) owner);

            if (!ownerValid) {
                item.setNoGravity(false);
                item.lifespan = 6000;
                item.setPickUpDelay(10);
                nbt.remove("BastetMagnetOwner");
                nbt.remove("BastetHurtOnce");
                item.hurtMarked = true;
            }
        }
    }
}