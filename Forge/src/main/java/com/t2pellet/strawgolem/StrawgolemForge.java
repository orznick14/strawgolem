package com.t2pellet.strawgolem;

import com.t2pellet.strawgolem.client.compat.ClothConfigCompat;
import com.t2pellet.strawgolem.platform.ForgeCommonRegistry;
import com.t2pellet.strawgolem.platform.Services;
import com.t2pellet.strawgolem.storage.StrawgolemSaveData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;

import java.util.function.Consumer;

@Mod(StrawgolemCommon.MODID)
public class StrawgolemForge {

    public StrawgolemForge() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::onCommonSetup);
        bus.addListener(this::onClientSetup);
        // Pre-init
        StrawgolemCommon.preInit();
        // Deferred Registers
        ForgeCommonRegistry.SOUNDS.register(bus);
        ForgeCommonRegistry.PARTICLES.register(bus);
        ForgeCommonRegistry.ENTITIES.register(bus);
        ForgeCommonRegistry.ITEMS.register(bus);
        // Client init
        if (FMLLoader.getDist().isClient()) {
            StrawgolemCommon.initClient();
        }
        // Save Data
        MinecraftForge.EVENT_BUS.addListener((Consumer<FMLServerStartingEvent>) event -> {
            StrawgolemSaveData data = new StrawgolemSaveData(ServerLifecycleHooks.getCurrentServer());
            try {
                data.loadData(ServerLifecycleHooks.getCurrentServer());
            } catch (Exception e) {
                StrawgolemCommon.LOG.error("Failed to load legacy strawgolem save data:", e);
            }
        });
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        StrawgolemCommon.init();
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        if (Services.PLATFORM.isModLoaded("cloth_config")) {
            ClothConfigCompat.registerConfigMenu();
        }
    }
}