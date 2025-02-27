package com.t2pellet.strawgolem.client.compat;

import com.t2pellet.strawgolem.StrawgolemCommon;
import com.t2pellet.strawgolem.config.StrawgolemConfig;
import com.t2pellet.strawgolem.entity.StrawGolem;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

@WailaPlugin("strawgolem")
public class CompatHwyla implements IWailaPlugin, IEntityComponentProvider {

    protected static final ResourceLocation LIFESPAN = new ResourceLocation(StrawgolemCommon.MODID, "lifespan");
    protected static final ResourceLocation HUNGER = new ResourceLocation(StrawgolemCommon.MODID, "hunger");
    private static final IEntityComponentProvider INSTANCE = new CompatHwyla();

    @Override
    public void registerClient(IWailaClientRegistration registrar) {
        if (StrawgolemConfig.Miscellaneous.isEnableHwyla()) {
            StrawgolemCommon.LOG.info("Registering Strawgolem HWYLA Compat");
            registrar.registerComponentProvider(INSTANCE, TooltipPosition.BODY, StrawGolem.class);
        } else {
            StrawgolemCommon.LOG.info("Strawgolem HWYLA compat is disabled");
        }
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.addConfig(LIFESPAN, StrawgolemConfig.Health.getLifespan() >= 0);
        registration.addConfig(HUNGER, StrawgolemConfig.Health.getHunger() >= 0);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (accessor.getEntity() instanceof StrawGolem golem) {
            if (config.get(LIFESPAN) && StrawgolemConfig.Health.getLifespan() > 0) {
                float daysLeftLife = golem.getLifespan().get() / 24000F;
                if (daysLeftLife >= 1) {
                    tooltip.add(new TranslatableComponent("strawgolem.lifespan", Math.round(daysLeftLife)));
                } else if (golem.getLifespan().get() < 0) {
                    tooltip.add(new TranslatableComponent("strawgolem.lifespan", '\u221e'));
                } else {
                    tooltip.add(new TranslatableComponent("strawgolem.lifespan", "<1"));
                }
            }
            if (config.get(HUNGER) && StrawgolemConfig.Health.getHunger() > 0) {
                float hungerLeft = golem.getHunger().get();
                if (hungerLeft >= StrawgolemConfig.Health.getHunger()) {
                    tooltip.add(new TranslatableComponent("strawgolem.hunger", "Not At All Hungry"));
                } else if (hungerLeft >= StrawgolemConfig.Health.getHunger() / 2F) {
                    tooltip.add(new TranslatableComponent("strawgolem.hunger", "A Little Bit Hungry"));
                } else if (hungerLeft >= StrawgolemConfig.Health.getHunger() / 4F) {
                    tooltip.add(new TranslatableComponent("strawgolem.hunger", "Pretty Hungry"));
                } else if (hungerLeft > 0) {
                    tooltip.add(new TranslatableComponent("strawgolem.hunger", "Very Hungry"));
                } else {
                    tooltip.add(new TranslatableComponent("strawgolem.hunger", "Starving"));
                }
            }
        }
    }
}
