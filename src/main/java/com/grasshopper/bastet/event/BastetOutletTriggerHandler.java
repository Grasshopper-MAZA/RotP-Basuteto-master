package com.grasshopper.bastet.event;

import com.grasshopper.bastet.BastetMain;
import com.grasshopper.bastet.block.BastetOutletBlock;
import com.grasshopper.bastet.magnet.MagnetizedData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BastetMain.MOD_ID)
public class BastetOutletTriggerHandler {

    private static final double TRIGGER_RADIUS = 1.0D;
    private static final float DAMAGE = 5.0F;

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        World world = entity.level;
        if (world.isClientSide()) return;

        AxisAlignedBB area = entity.getBoundingBox().inflate(TRIGGER_RADIUS);
        BlockPos min = new BlockPos(area.minX, area.minY, area.minZ);
        BlockPos max = new BlockPos(area.maxX, area.maxY, area.maxZ);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (!(world.getBlockState(pos).getBlock() instanceof BastetOutletBlock)) continue;

            CompoundNBT nbt = entity.getPersistentData();
            String key = "BastetOutletHurt_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();

            if (!nbt.getBoolean(key)) {
                entity.hurt(DamageSource.MAGIC, DAMAGE);
                nbt.putBoolean(key, true);
            }

            if (!MagnetizedData.isMagnetized(entity)) {
                MagnetizedData.magnetize(entity);
            }

            return;
        }
    }
}