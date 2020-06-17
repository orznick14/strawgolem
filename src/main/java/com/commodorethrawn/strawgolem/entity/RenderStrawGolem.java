package com.commodorethrawn.strawgolem.entity;

import com.commodorethrawn.strawgolem.Strawgolem;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderStrawGolem extends MobRenderer<EntityStrawGolem, ModelStrawGolem<EntityStrawGolem>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Strawgolem.MODID, "textures/entity/straw_golem.png");

    public RenderStrawGolem(EntityRendererManager rendermanagerIn) {
        super(rendermanagerIn, new ModelStrawGolem(), 0.5f);
        this.addLayer(new HeldItemLayer(this));
    }

    @Override
    public void doRender(EntityStrawGolem entity, double x, double y, double z, float entityYaw, float partialTicks) {
        ModelStrawGolem golem = this.getEntityModel();
        golem.holdingItem = !entity.isHandEmpty();
        golem.holdingBlock = entity.holdingBlockCrop();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    public ResourceLocation getEntityTexture(EntityStrawGolem entity) {
        return TEXTURE;
    }

    @Override
    protected boolean canRenderName(EntityStrawGolem entity) {
        return entity.hasCustomName();
    }

}
