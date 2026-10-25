package com.grasshopper.bastet.event;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.grasshopper.bastet.BastetMain;
import com.grasshopper.bastet.block.BastetOutletBlock;
import com.grasshopper.bastet.magnet.MagnetizedData;
import com.grasshopper.bastet.tileentity.BastetOutletTileEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BastetMain.MOD_ID)
public class BastetOutletRangeHandler {

    private static final double MAX_RANGE = 20.0D;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.isClientSide()) return;
        if (!(event.world instanceof ServerWorld)) return;

        ServerWorld world = (ServerWorld) event.world;

        if (world.getGameTime() % 20 != 0) return;

        Iterator<BlockPos> iterator = BastetOutletRegistry.OUTLETS.iterator();

        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();

            if (!(world.getBlockState(pos).getBlock() instanceof BastetOutletBlock)) {
                iterator.remove();
                continue;
            }

            TileEntity te = world.getBlockEntity(pos);
            if (!(te instanceof BastetOutletTileEntity)) {
                iterator.remove();
                continue;
            }

            UUID owner = ((BastetOutletTileEntity) te).getOwnerUUID();
            if (owner == null) {
                world.removeBlock(pos, false);
                iterator.remove();
                clearMagnetizedAround(world, pos);
                continue;
            }

            ServerPlayerEntity ownerPlayer = world.getServer().getPlayerList().getPlayer(owner);
            if (ownerPlayer == null) {
                world.removeBlock(pos, false);
                iterator.remove();
                clearMagnetizedAround(world, pos);
                continue;
            }

            if (!isBastetUser(ownerPlayer)) {
                world.removeBlock(pos, false);
                iterator.remove();
                clearMagnetizedAround(world, pos);
            }
        }
    }

    private static boolean isBastetUser(ServerPlayerEntity player) {
        try {
            IStandPower power = IStandPower.getPlayerStandPower(player);
            if (power == null || !power.hasPower()) return false;
            if (power.getType() == null) return false;

            String name = power.getType().getRegistryName() != null
                    ? power.getType().getRegistryName().toString()
                    : "";

            return name.contains("bastet");
        } catch (Exception e) {
            return false;
        }
    }

    private static void clearMagnetizedAround(ServerWorld world, BlockPos pos) {
        AxisAlignedBB area = new AxisAlignedBB(pos).inflate(MAX_RANGE);
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : entities) {
            if (MagnetizedData.isMagnetized(entity)) {
                MagnetizedData.clearMagnetization(entity);
            }
        }
    }
}