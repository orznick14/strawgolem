package com.commodorethrawn.strawgolem.client.renderer.entity;

import com.commodorethrawn.strawgolem.Strawgolem;
import com.commodorethrawn.strawgolem.client.renderer.entity.model.ModelStrawGolem;
import com.commodorethrawn.strawgolem.config.ConfigHelper;
import com.commodorethrawn.strawgolem.entity.EntityStrawGolem;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;

public class RenderStrawGolem extends MobRenderer<EntityStrawGolem, ModelStrawGolem<EntityStrawGolem>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Strawgolem.MODID, "textures/entity/straw_golem.png");
    private static final ResourceLocation TEXTURE_OLD = new ResourceLocation(Strawgolem.MODID, "textures/entity/old_straw_golem.png");
    private static final ResourceLocation TEXTURE_DYING = new ResourceLocation(Strawgolem.MODID, "textures/entity/dying_straw_golem.png");

    public RenderStrawGolem(EntityRendererManager rendermanagerIn) {
        super(rendermanagerIn, new ModelStrawGolem<>(), 0.5f);
        this.addLayer(new HeldItemLayer<>(this));
    }

    @Override
    public void doRender(EntityStrawGolem entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
        ModelStrawGolem<EntityStrawGolem> golem = this.getEntityModel();
        golem.setStatus(!entityIn.isHandEmpty(), entityIn.holdingFullBlock());
        // Shivering movement
        Biome b = entityIn.world.getBiome(entityIn.getPosition());
        if (ConfigHelper.isShiverEnabled() &&
                (b.getTempCategory() == Biome.TempCategory.COLD ||
                        (b.getTempCategory() == Biome.TempCategory.MEDIUM && entityIn.posY > 100))) {
            float offX = entityIn.getRNG().nextFloat() / 32 - 1 / 64F;
            float offZ = entityIn.getRNG().nextFloat() / 32 - 1 / 64F;
            GlStateManager.translatef(offX, 0, offZ);
        }
        super.doRender(entityIn, x, y, z, entityYaw, partialTicks);
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(EntityStrawGolem golem) {
        if (golem.getCurrentLifespan()  * 4 < ConfigHelper.getLifespan()) {
            return TEXTURE_DYING;
        } else if (golem.getCurrentLifespan() * 2 < ConfigHelper.getLifespan()) {
            return TEXTURE_OLD;
        }
        return TEXTURE;
    }

}
