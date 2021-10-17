package com.commodorethrawn.strawgolem.events;

import com.commodorethrawn.strawgolem.entity.EntityStrawGolem;
import com.commodorethrawn.strawgolem.entity.ai.MunchGolemGoal;
import com.commodorethrawn.strawgolem.entity.ai.PickupGolemGoal;
import com.commodorethrawn.strawgolem.mixin.GoalSelectorAccessor;
import com.commodorethrawn.strawgolem.mixin.TargetSelectorAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.server.world.ServerWorld;

public class EntityPatchHandler {

    private EntityPatchHandler() {
    }

    public static void onEntityLoad(Entity entity, ServerWorld serverWorld) {
        patchIronGolem(entity);
        patchRaider(entity);
        patchAnimal(entity);
    }

    private static void patchIronGolem(Entity entity) {
        if (entity instanceof IronGolemEntity ironGolem) {
            ((GoalSelectorAccessor) ironGolem).goalSelector().add(2, new PickupGolemGoal(ironGolem, 0.8D));
        }
    }

    private static void patchRaider(Entity entity) {
        if (entity instanceof RaiderEntity raider) {
            ((TargetSelectorAccessor) raider).targetSelector().add(2, new FollowTargetGoal<>(raider, EntityStrawGolem.class, true));
        }
    }

    private static void patchAnimal(Entity entity) {
        if (entity instanceof AnimalEntity animal) {
            ((GoalSelectorAccessor) animal).goalSelector().add(2, new MunchGolemGoal(animal, 0.8D));
        }
    }

}
