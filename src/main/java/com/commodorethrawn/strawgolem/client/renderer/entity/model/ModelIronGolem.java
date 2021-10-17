package com.commodorethrawn.strawgolem.client.renderer.entity.model;

import com.commodorethrawn.strawgolem.entity.EntityStrawGolem;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.CompositeEntityModel;
import net.minecraft.client.render.entity.model.IronGolemEntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.math.MathHelper;

/**
 * Replacement for vanilla golem model
 */
public class ModelIronGolem<T extends IronGolemEntity> extends SinglePartEntityModel<T> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public ModelIronGolem(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(T ironGolemEntity, float f, float g, float h, float i, float j) {
        this.head.yaw = i * 0.017453292F;
        this.head.pitch = j * 0.017453292F;
        this.rightLeg.pitch = -1.5F * MathHelper.wrap(f, 13.0F) * g;
        this.leftLeg.pitch = 1.5F * MathHelper.wrap(f, 13.0F) * g;
        this.rightLeg.yaw = 0.0F;
        this.leftLeg.yaw = 0.0F;
    }

    @Override
    public void animateModel(T entity, float f, float g, float h) {
        int i = entity.getAttackTicksLeft();
        if (i > 0) {
            this.rightArm.pitch = -2.0F + 1.5F * MathHelper.wrap((float)i - h, 10.0F);
            this.leftArm.pitch = -2.0F + 1.5F * MathHelper.wrap((float)i - h, 10.0F);
        } else {
            int j = entity.getLookingAtVillagerTicks();
            if (j > 0) {
                this.rightArm.pitch = -0.8F + 0.025F * MathHelper.wrap((float)j, 70.0F);
                this.leftArm.pitch = 0.0F;
            } else if (entity.getPassengerList().size() == 1 && entity.getPassengerList().get(0) instanceof EntityStrawGolem) {
                leftArm.pitch = -0.45F * (float) Math.PI;
                rightArm.pitch = -0.45F * (float) Math.PI;
                leftArm.yaw = 0.18F;
                rightArm.yaw = -0.18F;
            } else {
                this.rightArm.pitch = (-0.2F + 1.5F * MathHelper.wrap(f, 13.0F)) * g;
                this.leftArm.pitch = (-0.2F - 1.5F * MathHelper.wrap(f, 13.0F)) * g;
            }
        }
    }

    public ModelPart getRightArm() {
        return this.rightArm;
    }
}
