package com.grasshopper.bastet.tileentity;

import com.grasshopper.bastet.block.BastetOutletBlock;
import com.grasshopper.bastet.init.InitTileEntities;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;
import java.util.UUID;

public class BastetOutletTileEntity extends TileEntity {

    @Nullable
    private UUID ownerUUID;

    public BastetOutletTileEntity(TileEntityType<?> type) {
        super(type);
    }

    public BastetOutletTileEntity() {
        super(InitTileEntities.BASTET_OUTLET.get());
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.ownerUUID = uuid;
        setChanged();
    }

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public Direction getFacing() {
        BlockState state = this.getBlockState();
        if (state.hasProperty(BastetOutletBlock.FACING)) {
            return state.getValue(BastetOutletBlock.FACING);
        }
        return Direction.NORTH;
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        if (ownerUUID != null) {
            nbt.putUUID("Owner", ownerUUID);
        }
        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        if (nbt.hasUUID("Owner")) {
            ownerUUID = nbt.getUUID("Owner");
        }
    }
}