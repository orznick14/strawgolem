package com.commodorethrawn.strawgolem.client.renderer.entity.model;

import com.commodorethrawn.strawgolem.entity.EntityStrawGolem;
import com.commodorethrawn.strawgolem.entity.EntityStrawngGolem;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

public class ModelStrawngGolem extends SinglePartEntityModel<EntityStrawngGolem> {

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart headBand;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;

	public ModelStrawngGolem(ModelPart root) {
		this.root = root;
		head = root.getChild("head");
		headBand = head.getChild("headBand");
		leftArm = root.getChild("leftArm");
		rightArm = root.getChild("rightArm");
		leftLeg = root.getChild("leftLeg");
		rightLeg = root.getChild("rightLeg");
	}

	public static TexturedModelData createModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();

		modelPartData.addChild("head",
				ModelPartBuilder.create().uv(0, 33).cuboid(-6.0F, -21.0F, -6.0F, 12.0F, 3.0F, 12.0F).uv(0, 8).cuboid("accent", -6.0F, -18.0F, -6.0F, 12.0F, 12.0F, 12.0F),
				ModelTransform.pivot(0.0F, -19.0F, 0.0F))
			.addChild("headBand",
				ModelPartBuilder.create().uv(36, 17).cuboid(0.0F, -8.0F, 0.0F, 4.0F, 3.0F, 0.0F),
				ModelTransform.pivot(6.0F, -10.0F, 0.0F));
		modelPartData.addChild("body",
				ModelPartBuilder.create()
						.uv(50,18).cuboid(-12.0F, -13.75F, -7.5F, 24.0F, 15.0F, 15.0F)
						.uv(13, 107).cuboid(-9.0F, 1.25F, -5.5F, 18.0F, 10.0F, 11.0F),
				ModelTransform.pivot(-0.5F, -11.25F, -0.5F));
		modelPartData.addChild("leftArm",
			ModelPartBuilder.create()
					.uv(0, 64).cuboid(0.0F, -6.0F, -6.0F, 11.0F, 29.0F, 11.0F).mirrored()
					.uv(0, 51).cuboid(0.0F, -8.0F, -6.0F, 11.0F, 2.0F, 11.0F).mirrored(),
			ModelTransform.pivot(11.5F, -19.0F, 0.0F));
		modelPartData.addChild("rightArm",
			ModelPartBuilder.create()
					.uv(0, 64).cuboid(-11.0F, -6.0F, -5.5F, 11.0F, 29.0F, 11.0F)
					.uv(0, 51).cuboid(-11.0F, -8.0F, -5.5F, 11.0F, 2.0F, 11.0F),
			ModelTransform.pivot(-12.5F, -19.0F, -0.5F));
		ModelPartBuilder leg = ModelPartBuilder.create().uv(48, 72).cuboid(-4.0F, 0.0F, -4.0F, 8.0F, 24.0F, 8.0F);
		modelPartData.addChild("leftLeg", leg, ModelTransform.pivot(-6.5F, 0.0F, 0.0F));
		modelPartData.addChild("rightLeg", leg.mirrored(), ModelTransform.pivot(5.5F, 0.0F, 0.0F));

		return TexturedModelData.of(modelData, 128, 128);
	}

	@Override
	public ModelPart getPart() {
		return root;
	}

	@Override
	public void setAngles(EntityStrawngGolem entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch){
		setRotationAngle(headBand, 0.0F, -0.2618F, 0.0F);
		this.head.yaw = headYaw * ((float) Math.PI / 180F);
		this.head.pitch = headPitch * ((float) Math.PI / 180F);
		this.leftLeg.pitch = -1.5F * this.triangleWave(limbAngle) * limbDistance;
		this.rightLeg.pitch = 1.5F * this.triangleWave(limbAngle) * limbDistance;
		this.leftLeg.yaw = 0.0F;
		this.rightLeg.yaw = 0.0F;
	}

	@Override
	public void animateModel(EntityStrawngGolem entity, float limbAngle, float limbDistance, float tickDelta) {
		int attackTicks = entity.getAttackTicks();
		if (attackTicks > 0) {
			leftArm.pitch = - (float) Math.PI * (attackTicks - tickDelta) / 5;
			rightArm.pitch = - (float) Math.PI * (attackTicks - tickDelta) / 5;
		} else if (entity.getPassengerList().size() == 1 && entity.getPassengerList().get(0) instanceof EntityStrawGolem) {
			leftArm.pitch = -0.45F * (float) Math.PI;
			rightArm.pitch = -0.45F * (float) Math.PI;
			leftArm.yaw = 0.34F;
			rightArm.yaw = -0.34F;
		} else {
			this.rightArm.pitch = (-0.2F + 1.5F * MathHelper.wrap(limbAngle, 13.0F)) * limbDistance;
			this.leftArm.pitch = (-0.2F - 1.5F * MathHelper.wrap(limbAngle, 13.0F)) * limbDistance;
		}
	}

	public void setRotationAngle(ModelPart part, float x, float y, float z) {
		part.pitch = x;
		part.yaw = y;
		part.roll = z;
	}

	private float triangleWave(float f1) {
		return (Math.abs(f1 % (float) 13.0 - (float) 13.0 * 0.5F) - (float) 13.0 * 0.25F) / ((float) 13.0 * 0.25F);
	}
}