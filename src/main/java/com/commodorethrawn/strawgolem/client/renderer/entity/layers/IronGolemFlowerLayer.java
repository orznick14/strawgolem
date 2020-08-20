package com.commodorethrawn.strawgolem.client.renderer.entity.layers;

import com.commodorethrawn.strawgolem.client.renderer.entity.model.ModelIronGolem;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.passive.IronGolemEntity;

public class IronGolemFlowerLayer extends LayerRenderer<IronGolemEntity, ModelIronGolem<IronGolemEntity>> {

    public IronGolemFlowerLayer(IEntityRenderer<IronGolemEntity, ModelIronGolem<IronGolemEntity>> renderer) {
        super(renderer);
    }

    public void render(IronGolemEntity entityIn, float p_212842_2_, float p_212842_3_, float p_212842_4_, float p_212842_5_, float p_212842_6_, float p_212842_7_, float p_212842_8_) {
        if (entityIn.getHoldRoseTick() != 0) {
            GlStateManager.enableRescaleNormal();
            GlStateManager.pushMatrix();
            GlStateManager.rotatef(5.0F + 180.0F * this.getEntityModel().getArmHoldingRose().rotateAngleX / (float)Math.PI, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translatef(-0.9375F, -0.625F, -0.9375F);
            GlStateManager.scalef(0.5F, -0.5F, 0.5F);
            int i = entityIn.getBrightnessForRender();
            int j = i % 65536;
            int k = i / 65536;
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)j, (float)k);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            Minecraft.getInstance().getBlockRendererDispatcher().renderBlockBrightness(Blocks.POPPY.getDefaultState(), 1.0F);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
        }
    }

    public boolean shouldCombineTextures() {
        return false;
    }
}
