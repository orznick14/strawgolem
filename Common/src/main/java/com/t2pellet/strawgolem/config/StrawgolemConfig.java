package com.t2pellet.strawgolem.config;

import com.t2pellet.strawgolem.StrawgolemCommon;
import com.t2pellet.strawgolem.util.io.Config;
import com.t2pellet.strawgolem.util.io.ConfigHelper.Section;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class StrawgolemConfig extends Config {

    static final String FILTER_MODE_WHITELIST = "whitelist";
    static final String FILTER_MODE_BLACKLIST = "blacklist";

    public StrawgolemConfig() {
        super(StrawgolemCommon.MODID);
    }

    @Section("Creating")
    public static class Creation {
        
        @Section.Comment("Allow constructing head by shearing a pumpkin")
        private static boolean shearConstructionEnabled = true;
        @Section.Comment("Allow constructing golem with dispenser")
        private static boolean dispenserConstructionEnabled = true;

        public static boolean isShearConstructionEnabled() {
            return shearConstructionEnabled;
        }

        public static boolean isDispenserConstructionEnabled() {
            return dispenserConstructionEnabled;
        }
    }

    @Section("Harvesting")
    public static class Harvest {
        @Section.Comment("Enables golems replanting crops")
        private static boolean replantEnabled = true;

        @Section.Comment("The range of crops golems can detect")
        private static int searchRange = 24;

        @Section.Comment("The golem filtration mode. Enter 'whitelist' or 'blacklist'")
        private static String filterMode = FILTER_MODE_BLACKLIST;
        @Section.Comment("Format Example: whitelist = [minecraft:carrots,minecraft:wheat]")
        private static List<String> filterList = new ArrayList<>();

        public static boolean isHarvestAllowed(Block block) {
            return blockMatchesFilter(block, filterList, !filterMode.equals(FILTER_MODE_WHITELIST));
        }

        public static boolean isReplantEnabled() {
            return replantEnabled;
        }

        public static int getSearchRange() {
            return searchRange;
        }
    }

    @Section("Delivering")
    public static class Delivery {
        @Section.Comment("Enables golems delivering crops to a chest")
        private static boolean deliveryEnabled = true;

        @Section.Comment("The golem filtration mode. Enter 'whitelist' or 'blacklist'")
        private static String filterMode = FILTER_MODE_BLACKLIST;
        @Section.Comment("Format Example: whitelist = [minecraft:carrots,minecraft:wheat]")
        private static List<String> filterList = new ArrayList<>();

        public static boolean isDeliveryEnabled() {
            return deliveryEnabled;
        }

        public static boolean isDeliveryAllowed(Block block) {
            return blockMatchesFilter(block, filterList, filterMode.equals(FILTER_MODE_BLACKLIST));
        }
    }

    @Section("Miscellaneous")
    public static class Miscellaneous {
        @Section.Comment("Enables golem sounds")
        private static boolean soundsEnabled = true;
        @Section.Comment("Enables golems shivering in the cold & rain")
        private static boolean shiverEnabled = true;
        @Section.Comment("Enables Iron & Strawng Golem's picking up Straw Golems")
        private static boolean golemInteract = true;
        @Section.Comment("Enables sheep and cows ocassionaly munching on straw golems")
        private static boolean golemMunch = true;
        @Section.Comment("Enables HWYLA Compat")
        private static boolean enableHwyla = true;

        public static boolean isSoundsEnabled() {
            return soundsEnabled;
        }

        public static boolean isShiverEnabled() {
            return shiverEnabled;
        }

        public static boolean isGolemInteract() {
            return golemInteract;
        }

        public static boolean isEnableHwyla() {
            return enableHwyla;
        }

        public static boolean isGolemMunch() {
            return golemMunch;
        }
    }

    @Section("Tether")
    public static class Tether {
        @Section.Comment("Enables tether system preventing golems from wandering too far")
        private static boolean tetherEnabled = true;
        @Section.Comment("Enables whether tempting a golem away with an apple will change its tether")
        private static boolean temptResetsTether = true;
        @Section.Comment("The maximum range away from its tether the golem should wander")
        private static int tetherMaxRange = 36;
        @Section.Comment("The min distance to the tether the golem should return to when it wanders too far")
        private static int tetherMinRange = 0;

        public static boolean isTetherEnabled() {
            return tetherEnabled;
        }

        public static boolean doesTemptResetTether() {
            return temptResetsTether;
        }

        public static int getTetherMaxRange() {
            return tetherMaxRange;
        }

        public static int getTetherMinRange() {
            return tetherMinRange;
        }
    }

    @Section("Health")
    public static class Health {
        @Section.Comment("Golem lifespan in ticks. Set to -1 for infinite")
        private static int lifespan = 168000;
        @Section.Comment("Golem hunger in ticks. Set to -1 for infinite")
        private static int hunger = 48000;
        @Section.Comment("Enables lifespan penalty in the rain (-1 extra / tick)")
        private static boolean rainPenalty = true;
        @Section.Comment("Enables lifespan penalty in water (-1 extra / tick)")
        private static boolean waterPenalty = true;
        @Section.Comment("Enables lifespan heavy penalty, such as carrying a gourd block (-1 extra / tick)")
        private static boolean heavyPenalty = true;
        @Section.Comment("Amount of lifespan restored with wheat")
        private static int wheatTicks = 6000;
        @Section.Comment("Amount of hunger restored with apple")
        private static int foodTicks = 6000;
        @Section.Comment("Tempt/food item. Default is apple")
        private static String foodItem = "minecraft:apple";

        public static int getLifespan() {
            return lifespan;
        }

        public static int getHunger() {
            return hunger;
        }

        public static boolean isRainPenalty() {
            return rainPenalty;
        }

        public static boolean isWaterPenalty() {
            return waterPenalty;
        }

        public static boolean isHeavyPenalty() {
            return heavyPenalty;
        }

        public static int getWheatTicks() {
            return wheatTicks;
        }

        public static int getFoodTicks() {
            return foodTicks;
        }

        public static Item getFoodItem() {
            return Registry.ITEM.get(new ResourceLocation(foodItem));
        }

    }

    private static boolean blockMatchesFilter(Block block, List<String> filter, boolean invert) {
        String blockStr = Registry.BLOCK.getResourceKey(block).get().location().toString();
        if (invert) {
            return filter.stream().noneMatch(str -> str.trim().equalsIgnoreCase(blockStr));
        }
        return filter.stream().anyMatch(str -> str.trim().equalsIgnoreCase(blockStr));
    }

}
