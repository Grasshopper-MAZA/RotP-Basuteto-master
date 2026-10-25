package com.grasshopper.bastet.client.render;

import com.grasshopper.bastet.block.BastetOutletBlock;
import com.grasshopper.bastet.tileentity.BastetOutletTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class BastetOutletRenderer extends TileEntityRenderer<BastetOutletTileEntity> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            "bastet", "textures/block/bastet_outlet.png");

    public BastetOutletRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(BastetOutletTileEntity te, float partialTicks, MatrixStack matrixStack,
                       IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {

        Direction facing = te.getFacing();

        matrixStack.pushPose();
        matrixStack.translate(0.5D, 0.5D, 0.5D);

        switch (facing) {
            case UP:
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90F));
                break;
            case DOWN:
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(90F));
                break;
            case NORTH:
                break;
            case SOUTH:
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(180F));
                break;
            case WEST:
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(90F));
                break;
            case EAST:
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(-90F));
                break;
        }

        matrixStack.translate(0.0D, 0.0D, -0.499D);

        Matrix4f matrix = matrixStack.last().pose();
        IVertexBuilder builder = buffer.getBuffer(RenderType.entityCutout(TEXTURE));

        float nx = 0.0F;
        float ny = 0.0F;
        float nz = 1.0F;

        builder.vertex(matrix, -0.5F, -0.5F, 0F).color(255, 255, 255, 255).uv(0F, 1F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(nx, ny, nz).endVertex();
        builder.vertex(matrix,  0.5F, -0.5F, 0F).color(255, 255, 255, 255).uv(1F, 1F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(nx, ny, nz).endVertex();
        builder.vertex(matrix,  0.5F,  0.5F, 0F).color(255, 255, 255, 255).uv(1F, 0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(nx, ny, nz).endVertex();
        builder.vertex(matrix, -0.5F,  0.5F, 0F).color(255, 255, 255, 255).uv(0F, 0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(nx, ny, nz).endVertex();

        matrixStack.popPose();
    }
}