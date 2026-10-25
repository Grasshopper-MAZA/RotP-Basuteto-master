package com.grasshopper.bastet.action;

import java.util.UUID;

import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.grasshopper.bastet.block.BastetOutletBlock;
import com.grasshopper.bastet.event.BastetOutletRegistry;
import com.grasshopper.bastet.init.InitBlocks;
import com.grasshopper.bastet.tileentity.BastetOutletTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class BastetPlaceOutlet extends StandEntityAction {

    public BastetPlaceOutlet(Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (world.isClientSide()) return;

        PlayerEntity user = (PlayerEntity) userPower.getUser();
        if (user == null) return;

        RayTraceResult ray = user.pick(5.0D, 1.0F, false);
        if (ray == null || ray.getType() != RayTraceResult.Type.BLOCK) return;

        BlockRayTraceResult blockHit = (BlockRayTraceResult) ray;
        Direction side = blockHit.getDirection();
        if (!side.getAxis().isHorizontal()) return;

        BlockPos placePos = blockHit.getBlockPos().relative(side);

        if (!world.isEmptyBlock(placePos)) return;

        BlockState outlet = InitBlocks.BASTET_OUTLET.get().defaultBlockState()
                .setValue(BastetOutletBlock.FACING, side.getOpposite());

        if (outlet.canSurvive(world, placePos)) {
            removeOwnOutlets(world, user);
            world.setBlockAndUpdate(placePos, outlet);
            if (world.getBlockEntity(placePos) instanceof BastetOutletTileEntity) {
                ((BastetOutletTileEntity) world.getBlockEntity(placePos)).setOwnerUUID(user.getUUID());
            }
            BastetOutletRegistry.OUTLETS.add(placePos);
        }
    }

    private void removeOwnOutlets(World world, PlayerEntity user) {
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
    }
}