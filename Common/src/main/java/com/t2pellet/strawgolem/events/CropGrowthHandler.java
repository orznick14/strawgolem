package com.t2pellet.strawgolem.events;

import com.t2pellet.strawgolem.crop.WorldCrops;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Handles crop growth events, adds them to the crop handler
 */
public class CropGrowthHandler {

    private CropGrowthHandler() {
    }

    public static void onCropGrowth(Level world, BlockPos cropPos) {
        if (!world.isClientSide()) {
            WorldCrops.of(world).addCrop(cropPos);
        }
    }
}
