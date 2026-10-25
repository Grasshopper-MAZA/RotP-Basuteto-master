package com.grasshopper.bastet.client.render;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.grasshopper.bastet.BastetMain;
import com.grasshopper.bastet.entity.BastetStandEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class BastetRenderer extends StandEntityRenderer<BastetStandEntity, StandEntityModel<BastetStandEntity>> {

    public BastetRenderer(EntityRendererManager renderManager) {
        super(renderManager,
                StandModelRegistry.registerModel(
                        new ResourceLocation(BastetMain.MOD_ID, "bastet"),
                        BastetModel::new),
                new ResourceLocation(BastetMain.MOD_ID, "textures/entity/stand/bastet.png"),
                0);
    }
}