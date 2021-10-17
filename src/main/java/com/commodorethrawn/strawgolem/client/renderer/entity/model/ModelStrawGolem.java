package com.commodorethrawn.strawgolem.client.renderer.entity.model;

import com.commodorethrawn.strawgolem.entity.EntityStrawGolem;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

// Originally made by the talented Fr3nderman
public class ModelStrawGolem extends SinglePartEntityModel<EntityStrawGolem> implements ModelWithArms {
    private final ModelPart head;
    private final ModelPart root;
    private final ModelPart rightleg;
    private final ModelPart leftleg;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private boolean holdingBlock;
    private boolean holdingItem;
    private boolean isHungry;
    private boolean tempted;

    public ModelStrawGolem(ModelPart root) {
        this.root = root;
        head = root.getChild("head");
        rightleg = root.getChild("rightLeg");
        leftleg = root.getChild("leftLeg");
        rightArm = root.getChild("rightArm");
        leftArm = root.getChild("leftArm");

        holdingBlock = false;
        holdingItem = false;
        isHungry = false;
        tempted = false;
    }

    public static TexturedModelData createModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        modelPartData.addChild("head",
                ModelPartBuilder.create()
                        .uv(26, 24).cuboid(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F)
                        .uv(11, 32).cuboid( -2.0F, -5.0F, -2.0F, 4.0F, 1.0F, 4.0F),
                ModelTransform.pivot(0.0F, 11.0F, 0.0F));
        modelPartData.addChild("body",
                ModelPartBuilder.create().uv(20, 32).cuboid(-4.0F, -13.0F, -3.0F, 8.0F, 10.0F, 6.0F),
                ModelTransform.pivot(0.0F, 24.0F, 0.0F));
        ModelPartBuilder leg = ModelPartBuilder.create().uv(12, 43).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F);
        modelPartData.addChild("rightLeg", leg, ModelTransform.pivot(-2.0F, 21.0F, 0.0F));
        modelPartData.addChild("leftLeg", leg, ModelTransform.pivot(2.0F, 21.0F, 0.0F));
        ModelPartBuilder arm = ModelPartBuilder.create().uv(4, 39).cuboid(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F);
        modelPartData.addChild("rightArm", arm, ModelTransform.pivot(-5.0F, 12.0F, 0.0F));
        modelPartData.addChild("leftArm", arm, ModelTransform.pivot(5.0F, 12.0F, 0.0F));

        return TexturedModelData.of(modelData, 48, 48);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(EntityStrawGolem entity, float limbAngle, float limbDistance, float tickDelta, float headYaw, float headPitch) {
        //Head rotation
        this.head.yaw = headYaw * 0.017453292F;
        this.head.pitch = headPitch * 0.017453292F;
        // Movement
        float swingAmountArm = 1.7F * limbDistance;
        float swingAmountLeg = 2.4F * limbDistance;
        float auxLimbSwing = limbAngle * 3.331F;
        this.rightArm.pitch = MathHelper.cos(auxLimbSwing + (float) Math.PI) * swingAmountArm;
        this.leftArm.pitch = MathHelper.cos(auxLimbSwing) * swingAmountArm;
        this.rightleg.pitch = MathHelper.cos(auxLimbSwing) * swingAmountLeg;
        this.leftleg.pitch = MathHelper.cos(auxLimbSwing + (float) Math.PI) * swingAmountLeg;
        // Animations
        if (isHungry) {
            if (tempted) greedyArms(tickDelta);
            else idleArms(tickDelta);
            this.leftleg.pitch = -(float) Math.PI / 2;
            this.leftleg.yaw = -(float) Math.PI / 8;
            this.rightleg.pitch = -(float) Math.PI / 2;
            this.rightleg.yaw = (float) Math.PI / 8;
        } else if (holdingBlock) {
            this.rightArm.pitch = (float) Math.PI;
            this.leftArm.pitch = (float) Math.PI;
        } else if (holdingItem) {
            this.rightArm.pitch = (float) -(0.29D * Math.PI);
            this.rightArm.yaw = (float) -(0.12D * Math.PI);
            this.rightArm.roll = (float) (0.08D * Math.PI);
            this.leftArm.pitch = (float) -(0.29D * Math.PI);
            this.leftArm.yaw = (float) (0.12D * Math.PI);
            this.leftArm.roll = (float) -(0.08D * Math.PI);
        } else if (tempted) greedyArms(tickDelta);
        else idleArms(tickDelta);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        if (isHungry) matrices.translate(0.0F, -0.06F, -0.18F);
        super.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    @Override
    public void setArmAngle(Arm arm, MatrixStack matrices) {
        if (holdingBlock) {
            matrices.translate(0.035F, -0.75F, 0.58F);
            matrices.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(15.0F));
            matrices.scale(1.5F, 1.5F, 1.5F);
        } else {
            matrices.translate(0.05F, 1.3F, 0.23F);
            matrices.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(90.0F));
        }
    }

    public void setHoldingBlock(boolean holdingBlock) {
        this.holdingBlock = holdingBlock;
    }

    public void setHoldingItem(boolean holdingItem) {
        this.holdingItem = holdingItem;
    }

    public void setHungry(boolean isHungry) {
        this.isHungry = isHungry;
    }

    public void setTempted(boolean tempted) {
        this.tempted = tempted;
    }

    /**
     * Greedy arms animation
     * @param tickDelta the animation progress
     */
    private void greedyArms(float tickDelta) {
        this.rightArm.pitch = -(float) Math.PI / 1.6F;
        this.rightArm.yaw = -(float) Math.PI / 12 + MathHelper.cos(tickDelta * 1.1F) * 0.075F;
        this.leftArm.pitch = -(float) Math.PI / 1.6F;
        this.leftArm.yaw = (float) Math.PI / 12 - MathHelper.cos(tickDelta * 1.1F) * 0.075F;
    }

    /**
     * Idle arm swinging
     * @param animationProgress the animation progress
     */
    private void idleArms(float animationProgress) {
        float roll = MathHelper.cos(animationProgress * 0.09F) * 0.06F + 0.06F;
        float pitch = MathHelper.sin(animationProgress * 0.067F) * 0.06F;
        this.rightArm.roll = roll;
        this.rightArm.pitch += pitch;
        this.leftArm.roll = -roll;
        this.leftArm.pitch -= pitch;
    }

}