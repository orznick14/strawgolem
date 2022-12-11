package com.t2pellet.strawgolem.entity.ai;

import com.t2pellet.strawgolem.entity.StrawGolem;
import com.t2pellet.strawgolem.entity.StrawngGolem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class TrackStrawngGolemTargetGoal extends TargetGoal {
    private final StrawngGolem golem;
    private LivingEntity target;
    private final TargetingConditions targetPredicate = TargetingConditions.DEFAULT.allowNonAttackable().range(48.0D);

    public TrackStrawngGolemTargetGoal(StrawngGolem golem) {
        super(golem, false, true);
        this.golem = golem;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    public boolean canUse() {
        AABB box = this.golem.getBoundingBox().inflate(10.0D, 8.0D, 10.0D);
        List<StrawGolem> list = this.golem.level.getNearbyEntities(StrawGolem.class, this.targetPredicate, this.golem, box);

        for (StrawGolem strawGolem : list) {
            if (strawGolem.getLastDamageSource() != null && strawGolem.getLastDamageSource().getEntity() instanceof LivingEntity) {
                this.target = (LivingEntity) strawGolem.getLastDamageSource().getEntity();
            }
        }

        if (this.target == null) {
            return false;
        } else
            return !(this.target instanceof Player) || !this.target.isSpectator() && !((Player) this.target).isCreative();
    }

    public void start() {
        this.golem.setTarget(this.target);
        super.start();
    }
}
