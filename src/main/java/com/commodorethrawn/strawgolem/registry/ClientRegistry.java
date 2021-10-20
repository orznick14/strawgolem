package com.commodorethrawn.strawgolem.registry;

import com.commodorethrawn.strawgolem.client.renderer.entity.RenderIronGolem;
import com.commodorethrawn.strawgolem.client.renderer.entity.RenderStrawGolem;
import com.commodorethrawn.strawgolem.client.renderer.entity.RenderStrawngGolem;
import com.commodorethrawn.strawgolem.client.renderer.entity.model.ModelStrawGolem;
import com.commodorethrawn.strawgolem.client.renderer.entity.model.ModelStrawngGolem;
import com.commodorethrawn.strawgolem.config.StrawgolemConfig;
import com.commodorethrawn.strawgolem.util.scheduler.ActionScheduler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.EntityType;

import static com.commodorethrawn.strawgolem.registry.StrawgolemEntities.STRAWNG_GOLEM_TYPE;
import static com.commodorethrawn.strawgolem.registry.StrawgolemEntities.STRAW_GOLEM_TYPE;

@Environment(EnvType.CLIENT)
public class ClientRegistry {

    public static void register() {
        ClientTickEvents.END_WORLD_TICK.register(ActionScheduler.INSTANCE::tickClient);
        ParticleRegistry.register();
        registerEntityRenderer();
        registerEntityModelLayer();
    }

    private static void registerEntityRenderer() {
        EntityRendererRegistry.register(STRAW_GOLEM_TYPE, RenderStrawGolem::new);
        EntityRendererRegistry.register(STRAWNG_GOLEM_TYPE, RenderStrawngGolem::new);
        if (StrawgolemConfig.Miscellaneous.isGolemInteract()) {
            EntityRendererRegistry.register(EntityType.IRON_GOLEM, RenderIronGolem::new);
        }
    }

    private static void registerEntityModelLayer() {
        EntityModelLayerRegistry.registerModelLayer(Entity.STRAW_GOLEM_MODEL, ModelStrawGolem::createModelData);
        EntityModelLayerRegistry.registerModelLayer(Entity.STRAWNG_GOLEM_MODEL, ModelStrawngGolem::createModelData);
    }

    public static class Entity {
        private static final EntityModelLayer STRAW_GOLEM_MODEL = new EntityModelLayer(STRAW_GOLEM_TYPE.getLootTableId(), "main");
        private static final EntityModelLayer STRAWNG_GOLEM_MODEL = new EntityModelLayer(STRAWNG_GOLEM_TYPE.getLootTableId(), "main");

        public static EntityModelLayer getStrawGolemModel() {
            return STRAW_GOLEM_MODEL;
        }
        public static EntityModelLayer getStrawngGolemModel() {
            return STRAWNG_GOLEM_MODEL;
        }
    }
}
