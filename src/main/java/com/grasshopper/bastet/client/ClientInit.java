package com.grasshopper.bastet.client;

import com.grasshopper.bastet.BastetMain;
import com.grasshopper.bastet.client.render.BastetOutletRenderer;
import com.grasshopper.bastet.client.render.BastetRenderer;
import com.grasshopper.bastet.init.InitStands;
import com.grasshopper.bastet.init.InitTileEntities;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = BastetMain.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInit {

    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(
                InitStands.STAND_BASTET.getEntityType(),
                BastetRenderer::new);

        ClientRegistry.bindTileEntityRenderer(
                InitTileEntities.BASTET_OUTLET.get(),
                BastetOutletRenderer::new);
    }
}