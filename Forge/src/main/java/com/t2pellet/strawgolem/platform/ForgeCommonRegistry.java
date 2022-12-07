package com.t2pellet.strawgolem.platform;

import com.t2pellet.strawgolem.StrawgolemCommon;
import com.t2pellet.strawgolem.events.CropGrowthHandler;
import com.t2pellet.strawgolem.events.StrawGolemEvents;
import com.t2pellet.strawgolem.events.WorldInteractHandler;
import com.t2pellet.strawgolem.platform.services.ICommonRegistry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ForgeCommonRegistry implements ICommonRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, StrawgolemCommon.MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, StrawgolemCommon.MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, StrawgolemCommon.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, StrawgolemCommon.MODID);

    @Override
    public Supplier<SimpleParticleType> registerParticle(ResourceLocation id) {
        return PARTICLES.register(id.getPath(), () -> new SimpleParticleType(true) {
        });
    }

    @Override
    public <T extends LivingEntity> Supplier<EntityType<T>> registerEntity(String name, EntityType.EntityFactory<T> factory, float width, float height, Supplier<AttributeSupplier.Builder> builder) {
        var result = ENTITIES.register(name, () -> EntityType.Builder.of(factory, MobCategory.CREATURE)
                .clientTrackingRange(48).updateInterval(3).sized(width, height)
                .build(name));
        FMLJavaModLoadingContext.get().getModEventBus().addListener((Consumer<EntityAttributeCreationEvent>) event -> event.put(result.get(), builder.get().build()));
        return result;
    }

    @Override
    public void registerSound(SoundEvent id) {
        SOUNDS.register(id.getLocation().getPath(), () -> id);
    }

    @Override
    public Supplier<Item> registerItem(ResourceLocation location, Item.Properties properties) {
        return ITEMS.register(location.getPath(), () -> new Item(properties));
    }

    @Override
    public void registerEvents() {
        /*  Crop Registry */
        // Register crops from vanilla registry
        MinecraftForge.EVENT_BUS.addListener((Consumer<StrawGolemEvents.BlockRegisteredEvent>) event -> {
            ICommonRegistry.registerCrop(event.block);
        });
        // Can't mixin to Forge stuff, so just gonna wait until its done and then get all the stuff in the forge block registry
        FMLJavaModLoadingContext.get().getModEventBus().addListener((Consumer<FMLLoadCompleteEvent>) event -> {
            ForgeRegistries.BLOCKS.forEach(ICommonRegistry::registerCrop);
        });
        //Crop growth handling
        MinecraftForge.EVENT_BUS.addListener((Consumer<StrawGolemEvents.CropGrowthEvent>) event -> CropGrowthHandler.onCropGrowth((Level) event.getLevel(), event.getPos()));
        MinecraftForge.EVENT_BUS.addListener((Consumer<PlayerInteractEvent.RightClickBlock>) event -> {
            // Chest Handling
            if (WorldInteractHandler.setPriorityChest(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec()) == InteractionResult.CONSUME) {
                event.setCanceled(true);
            }
        });

    }


}
