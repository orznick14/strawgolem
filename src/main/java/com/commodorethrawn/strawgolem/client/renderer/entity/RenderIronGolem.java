package com.commodorethrawn.strawgolem.client.renderer.entity;

import com.commodorethrawn.strawgolem.client.renderer.entity.layers.IronGolemCracksLayer;
import com.commodorethrawn.strawgolem.client.renderer.entity.layers.IronGolemFlowerLayer;
import com.commodorethrawn.strawgolem.client.renderer.entity.model.ModelIronGolem;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public class RenderIronGolem extends MobEntityRenderer<IronGolemEntity, ModelIronGolem<IronGolemEntity>> {

    private static final Identifier TEXTURE = new Identifier("textures/entity/iron_golem/iron_golem.png");

    public RenderIronGolem(EntityRendererFactory.Context context) {
        super(context, new ModelIronGolem<>(context.getPart(EntityModelLayers.IRON_GOLEM)), 0.7F);
        this.addFeature(new IronGolemCracksLayer(this));
        this.addFeature(new IronGolemFlowerLayer(this));
    }

    public Identifier getTexture(IronGolemEntity ironGolemEntity) {
        return TEXTURE;
    }

    protected void setupTransforms(IronGolemEntity ironGolemEntity, MatrixStack matrixStack, float f, float g, float h) {
        super.setupTransforms(ironGolemEntity, matrixStack, f, g, h);
        if (!((double)ironGolemEntity.limbDistance < 0.01D)) {
            float i = 13.0F;
            float j = ironGolemEntity.limbAngle - ironGolemEntity.limbDistance * (1.0F - h) + 6.0F;
            float k = (Math.abs(j % 13.0F - 6.5F) - 3.25F) / 3.25F;
            matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(6.5F * k));
        }
    }
}
