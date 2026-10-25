package com.grasshopper.bastet.init;

import com.grasshopper.bastet.BastetMain;
import com.grasshopper.bastet.tileentity.BastetOutletTileEntity;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitTileEntities {
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(
            ForgeRegistries.TILE_ENTITIES, BastetMain.MOD_ID);

    public static final RegistryObject<TileEntityType<BastetOutletTileEntity>> BASTET_OUTLET =
            TILE_ENTITIES.register("bastet_outlet",
                    () -> TileEntityType.Builder.of(BastetOutletTileEntity::new,
                            InitBlocks.BASTET_OUTLET.get()).build(null));
}