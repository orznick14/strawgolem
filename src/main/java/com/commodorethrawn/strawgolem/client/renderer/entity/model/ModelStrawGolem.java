package com.commodorethrawn.strawgolem.client.renderer.entity.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.ModelBox;
import net.minecraft.entity.Entity;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;


// Made using Blockbench 3.5.3 by the talented Fr3nderman
// Exported for Minecraft version 1.15
public class ModelStrawGolem<T extends Entity> extends EntityModel<T> implements IHasArm {    private final RendererModel Head;
    private final RendererModel Body;
    private final RendererModel RightLeg;
    private final RendererModel LeftLeg;
    private final RendererModel RightArm;
    private final RendererModel LeftArm;


    public boolean holdingItem;
    public boolean holdingBlock;

    public ModelStrawGolem() {
        holdingItem = false;
        holdingBlock = false;
        textureWidth = 48;
        textureHeight = 48;

        Head = new RendererModel(this);
        Head.setRotationPoint(0.0F, 11.0F, 0.0F);
        Head.cubeList.add(new ModelBox(Head, 26, 24, -2.0F, -4.0F, -2.0F, 4, 4, 4, 0.0F, false));
        Head.cubeList.add(new ModelBox(Head, 11, 32, -2.0F, -5.0F, -2.0F, 4, 1, 4, 0.0F, false));

        Body = new RendererModel(this);
        Body.setRotationPoint(0.0F, 24.0F, 0.0F);
        Body.cubeList.add(new ModelBox(Body, 20, 32, -4.0F, -13.0F, -3.0F, 8, 10, 6, 0.0F, false));

        RightLeg = new RendererModel(this);
        RightLeg.setRotationPoint(-2.0F, 21.0F, 0.0F);
        RightLeg.cubeList.add(new ModelBox(RightLeg, 12, 43, -1.0F, 0.0F, -1.0F, 2, 3, 2, 0.0F, false));

        LeftLeg = new RendererModel(this);
        LeftLeg.setRotationPoint(2.0F, 21.0F, 0.0F);
        LeftLeg.cubeList.add(new ModelBox(LeftLeg, 12, 43, -1.0F, 0.0F, -1.0F, 2, 3, 2, 0.0F, false));

        RightArm = new RendererModel(this);
        RightArm.setRotationPoint(-5.0F, 12.0F, 0.0F);
        RightArm.cubeList.add(new ModelBox(RightArm, 4, 39, -1.0F, 0.0F, -1.0F, 2, 7, 2, 0.0F, false));

        LeftArm = new RendererModel(this);
        LeftArm.setRotationPoint(5.0F, 12.0F, 0.0F);
        LeftArm.cubeList.add(new ModelBox(LeftArm, 4, 39, -1.0F, 0.0F, -1.0F, 2, 7, 2, 0.0F, false));

    }

    @Override
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        this.Head.rotateAngleY = netHeadYaw * 0.017453292F;
        this.Head.rotateAngleX = headPitch * 0.017453292F;

        this.Body.rotateAngleY = 0.0F;

        float auxLimbSwing = limbSwing * 5.0F * 0.6662F;

        float swingAmountArm = 1.7F * limbSwingAmount;
        float swingAmoungLeg = 2.5F * limbSwingAmount;

        this.RightArm.rotateAngleX = MathHelper.cos(auxLimbSwing + (float) Math.PI) * swingAmountArm;
        this.LeftArm.rotateAngleX = MathHelper.cos(auxLimbSwing) * swingAmountArm;
        this.RightArm.rotateAngleZ = 0.0F;
        this.LeftArm.rotateAngleZ = 0.0F;
        this.RightLeg.rotateAngleX = MathHelper.cos(auxLimbSwing) * swingAmoungLeg;
        this.LeftLeg.rotateAngleX = MathHelper.cos(auxLimbSwing + (float) Math.PI) * swingAmoungLeg;
        this.RightLeg.rotateAngleY = 0.0F;
        this.LeftLeg.rotateAngleY = 0.0F;
        this.RightLeg.rotateAngleZ = 0.0F;
        this.LeftLeg.rotateAngleZ = 0.0F;

        this.RightArm.rotateAngleY = 0.0F;
        this.RightArm.rotateAngleZ = 0.0F;

        this.LeftArm.rotateAngleY = 0.0F;

        this.RightArm.rotateAngleY = 0.0F;

        this.Body.rotateAngleX = 0.0F;

        // Arms idle movement
        if (holdingBlock) {
            this.RightArm.rotateAngleX = (float) Math.PI;
            this.LeftArm.rotateAngleX = (float) Math.PI;
        } else if (holdingItem) {
            this.RightArm.rotateAngleX = (float) -(0.29D * Math.PI);
            this.RightArm.rotateAngleY = (float) -(0.12D * Math.PI);
            this.RightArm.rotateAngleZ = (float) (0.08D * Math.PI);
            this.LeftArm.rotateAngleX = (float) -(0.29D * Math.PI);
            this.LeftArm.rotateAngleY = (float) (0.12D * Math.PI);
            this.LeftArm.rotateAngleZ = (float) -(0.08D * Math.PI);
        } else {
            this.RightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.06F + 0.06F;
            this.LeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.06F + 0.06F;
            this.RightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.06F;
            this.LeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.06F;
        }
    }

    @Override
    public void render(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        Head.render(scale);
        Body.render(scale);
        RightLeg.render(scale);
        LeftLeg.render(scale);
        RightArm.render(scale);
        LeftArm.render(scale);

    }

    @Override
    public void postRenderArm(float scale, HandSide side) {
        if (holdingBlock) {
            GlStateManager.translatef(0.07F, -0.75F, 0.59F);
            GlStateManager.rotatef(15.0F, -1.0F, 0.0F, 0.0F);
            GlStateManager.scalef(1.5F, 1.5F, 1.5F);
        } else {
            GlStateManager.translatef(0.05F, 1.3F, 0.23F);
            GlStateManager.rotatef(90.0F, -1.0F, 0.0F, 0.0F);
        }
    }

    /**
     * Updates the holdingItem and holdingBlock properties of this model
     *
     * @param holdingItem  : the new value of this.holdingItem
     * @param holdingBlock : the new value of this.holdingBlock
     */
    public void setStatus(boolean holdingItem, boolean holdingBlock) {
        this.holdingItem = holdingItem;
        this.holdingBlock = holdingBlock;
    }
}
