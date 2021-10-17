package com.commodorethrawn.strawgolem.registry;

import com.commodorethrawn.strawgolem.Strawgolem;
import com.commodorethrawn.strawgolem.entity.EntityStrawGolem;
import com.commodorethrawn.strawgolem.entity.EntityStrawngGolem;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class StrawgolemEntities {

    static EntityType<EntityStrawGolem> STRAW_GOLEM_TYPE;
    static EntityType<EntityStrawngGolem> STRAWNG_GOLEM_TYPE;

    public static EntityType<EntityStrawGolem> getStrawGolemType() {
        return STRAW_GOLEM_TYPE;
    }
    public static EntityType<EntityStrawngGolem> getStrawngGolemType() {
        return STRAWNG_GOLEM_TYPE;
    }

    public static void register() {
        STRAW_GOLEM_TYPE = registerEntity("strawgolem", EntityStrawGolem::new, 0.6F, 0.9F);
        STRAWNG_GOLEM_TYPE = registerEntity("strawnggolem", EntityStrawngGolem::new, 1.25F, 3.5F);
        FabricDefaultAttributeRegistry.register(STRAW_GOLEM_TYPE, EntityStrawGolem.createMob());
        FabricDefaultAttributeRegistry.register(STRAWNG_GOLEM_TYPE, EntityStrawngGolem.createMob());
    }

    private static <T extends Entity> EntityType<T> registerEntity(String name, EntityType.EntityFactory<T> factory, float width, float height) {
        return Registry.register(
                Registry.ENTITY_TYPE,
                new Identifier(Strawgolem.MODID, name),
                EntityType.Builder.create(factory, SpawnGroup.CREATURE)
                    .maxTrackingRange(48).trackingTickInterval(3).setDimensions(width, height)
                    .build(name));
    }

}
