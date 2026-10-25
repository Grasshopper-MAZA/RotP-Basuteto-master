package com.grasshopper.bastet.init;

import com.grasshopper.bastet.BastetMain;
import com.grasshopper.bastet.block.BastetOutletBlock;

import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
            ForgeRegistries.BLOCKS, BastetMain.MOD_ID);

    public static final RegistryObject<Block> BASTET_OUTLET = BLOCKS.register("bastet_outlet",
            BastetOutletBlock::new);
}