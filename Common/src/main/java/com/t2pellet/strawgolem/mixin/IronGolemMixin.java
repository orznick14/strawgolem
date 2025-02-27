package com.t2pellet.strawgolem.mixin;

import com.t2pellet.strawgolem.config.StrawgolemConfig;
import com.t2pellet.strawgolem.entity.ai.PickupGolemGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IronGolem.class)
public class IronGolemMixin extends Mob {

    protected IronGolemMixin(EntityType<? extends Mob> $$0, Level $$1) {
        super($$0, $$1);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    public void registerGoals(CallbackInfo ci) {
        if (StrawgolemConfig.Miscellaneous.isGolemInteract()) {
            goalSelector.addGoal(2, new PickupGolemGoal((IronGolem) (Object) this, 0.8D));
        }
    }


}
