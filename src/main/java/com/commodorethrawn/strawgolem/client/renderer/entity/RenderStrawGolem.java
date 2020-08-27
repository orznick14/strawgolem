package com.commodorethrawn.strawgolem.client.renderer.entity;

import com.commodorethrawn.strawgolem.Strawgolem;
import com.commodorethrawn.strawgolem.client.renderer.entity.model.ModelStrawGolem;
import com.commodorethrawn.strawgolem.config.ConfigHelper;
import com.commodorethrawn.strawgolem.entity.EntityStrawGolem;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class RenderStrawGolem extends MobRenderer<EntityStrawGolem, ModelStrawGolem<EntityStrawGolem>> {

    private static final Map<String, ResourceLocation> TEXTURE_MAP;
    private static final String TEXTURE_DEFAULT, TEXTURE_OLD, TEXTURE_DYING, TEXTURE_WINTER;
    private static final boolean IS_DECEMBER;

    static {
        TEXTURE_DEFAULT = "golem";
        TEXTURE_OLD = "old_golem";
        TEXTURE_DYING = "dying_golem";
        TEXTURE_WINTER = "winter_golem";
        TEXTURE_MAP = new HashMap<>();
        InputStream stream = Strawgolem.class.getResourceAsStream("/assets/strawgolem/textures/entity");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            while (reader.ready()) {
                String name = reader.readLine();
                if (name != null) name = name.replace(".png", "");
                TEXTURE_MAP.put(name, new ResourceLocation(Strawgolem.MODID, "textures/entity/" + name + ".png"));
            }
            stream.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        IS_DECEMBER = GregorianCalendar.getInstance().get(Calendar.MONTH) == Calendar.DECEMBER;
    }

    public RenderStrawGolem(EntityRendererManager rendermanagerIn) {
        super(rendermanagerIn, new ModelStrawGolem<>(), 0.5f);
        this.addLayer(new HeldItemLayer<>(this));
    }

    @Override
    public void doRender(EntityStrawGolem entityIn, double x, double y, double z, float entityYaw, float partialTicks) {
        ModelStrawGolem<EntityStrawGolem> golem = this.getEntityModel();
        golem.setStatus(!entityIn.isHandEmpty(), entityIn.holdingFullBlock());
        // Shivering movement
        Biome b = entityIn.world.getBiome(entityIn.getPosition());
        if (ConfigHelper.isShiverEnabled() &&
                (b.getTemperature(entityIn.getPosition()) < 0.15
                        || (entityIn.isInRain()))) {
            float offX = entityIn.getRNG().nextFloat() / 32 - 1 / 64F;
            float offZ = entityIn.getRNG().nextFloat() / 32 - 1 / 64F;
            GlStateManager.translatef(offX, 0, offZ);
        }
        super.doRender(entityIn, x, y, z, entityYaw, partialTicks);
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(EntityStrawGolem golem) {
        int lifespan = golem.getCurrentLifespan();
        int maxLifespan = ConfigHelper.getLifespan();
        if (lifespan * 4 < maxLifespan) return TEXTURE_MAP.get(TEXTURE_DYING);
        String name = golem.getDisplayName().getString().toLowerCase();
        if (TEXTURE_MAP.containsKey(name)) return TEXTURE_MAP.get(name);
        if (IS_DECEMBER) return TEXTURE_MAP.get(TEXTURE_WINTER);
        return lifespan * 2 < maxLifespan ? TEXTURE_MAP.get(TEXTURE_OLD) : TEXTURE_MAP.get(TEXTURE_DEFAULT);
    }

    @Override
    public void renderName(EntityStrawGolem entity, double x, double y, double z) {
        if (!TEXTURE_MAP.containsKey(entity.getDisplayName().getString().toLowerCase()))
            super.renderName(entity, x, y, z);
    }
}
