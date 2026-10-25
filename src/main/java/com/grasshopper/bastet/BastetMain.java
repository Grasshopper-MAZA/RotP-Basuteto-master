package com.grasshopper.bastet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.grasshopper.bastet.init.InitBlocks;
import com.grasshopper.bastet.init.InitEntities;
import com.grasshopper.bastet.init.InitSounds;
import com.grasshopper.bastet.init.InitStands;
import com.grasshopper.bastet.init.InitTileEntities;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BastetMain.MOD_ID)
public class BastetMain {
    public static final String MOD_ID = "bastet";
    public static final Logger LOGGER = LogManager.getLogger();

    public BastetMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        InitEntities.ENTITIES.register(modEventBus);
        InitSounds.SOUNDS.register(modEventBus);
        InitStands.ACTIONS.register(modEventBus);
        InitStands.STANDS.register(modEventBus);
        InitBlocks.BLOCKS.register(modEventBus);
        InitTileEntities.TILE_ENTITIES.register(modEventBus);
    }
}