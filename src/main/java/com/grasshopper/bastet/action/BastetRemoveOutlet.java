package com.grasshopper.bastet.action;

import java.util.UUID;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.grasshopper.bastet.block.BastetOutletBlock;
import com.grasshopper.bastet.event.BastetOutletRegistry;
import com.grasshopper.bastet.magnet.MagnetizedData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BastetRemoveOutlet extends StandEntityAction {

    public BastetRemoveOutlet(Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide()) return;

        PlayerEntity user = (PlayerEntity) userPower.getUser();
        if (user == null) return;

        BlockPos playerPos = user.blockPosition();
        int radius = 64;
        UUID uuid = user.getUUID();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    if (world.getBlockState(pos).getBlock() instanceof BastetOutletBlock) {
                        UUID owner = BastetOutletBlock.getOwner(world, pos);
                        if (uuid.equals(owner)) {
                            world.removeBlock(pos, false);
                            BastetOutletRegistry.OUTLETS.remove(pos);
                        }
                    }
                }
            }
        }

        clearMagnetizedEntities(world, user, radius);
    }

    private void clearMagnetizedEntities(World world, PlayerEntity user, int radius) {
        AxisAlignedBB area = user.getBoundingBox().inflate(radius);
        java.util.List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : entities) {
            if (MagnetizedData.isMagnetized(entity)) {
                MagnetizedData.clearMagnetization(entity);
            }
        }
    }
}