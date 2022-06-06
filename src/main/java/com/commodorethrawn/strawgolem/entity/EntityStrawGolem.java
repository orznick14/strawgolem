package com.commodorethrawn.strawgolem.entity;

import com.commodorethrawn.strawgolem.Registry;
import com.commodorethrawn.strawgolem.Strawgolem;
import com.commodorethrawn.strawgolem.config.ConfigHelper;
import com.commodorethrawn.strawgolem.entity.ai.*;
import com.commodorethrawn.strawgolem.entity.capability.InventoryProvider;
import com.commodorethrawn.strawgolem.entity.capability.lifespan.ILifespan;
import com.commodorethrawn.strawgolem.entity.capability.lifespan.LifespanProvider;
import com.commodorethrawn.strawgolem.entity.capability.memory.IMemory;
import com.commodorethrawn.strawgolem.entity.capability.memory.MemoryProvider;
import com.commodorethrawn.strawgolem.entity.capability.profession.IProfession;
import com.commodorethrawn.strawgolem.entity.capability.profession.ProfessionProvider;
import com.commodorethrawn.strawgolem.network.MessageLifespan;
import com.commodorethrawn.strawgolem.network.PacketHandler;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class EntityStrawGolem extends GolemEntity {
    public static final SoundEvent GOLEM_AMBIENT = new SoundEvent(new ResourceLocation(Strawgolem.MODID, "golem_ambient"));
    public static final SoundEvent GOLEM_STRAINED = new SoundEvent(new ResourceLocation(Strawgolem.MODID, "golem_strained"));
    public static final SoundEvent GOLEM_HURT = new SoundEvent(new ResourceLocation(Strawgolem.MODID, "golem_hurt"));
    public static final SoundEvent GOLEM_DEATH = new SoundEvent(new ResourceLocation(Strawgolem.MODID, "golem_death"));
    public static final SoundEvent GOLEM_HEAL = new SoundEvent(new ResourceLocation(Strawgolem.MODID, "golem_heal"));
    public static final SoundEvent GOLEM_SCARED = new SoundEvent(new ResourceLocation(Strawgolem.MODID, "golem_scared"));
    public static final SoundEvent GOLEM_INTERESTED = new SoundEvent(new ResourceLocation(Strawgolem.MODID, "golem_interested"));
    private static final ResourceLocation LOOT = new ResourceLocation(Strawgolem.MODID, "strawgolem");
    private final ILifespan lifespan;
    private final IMemory memory;
    private final IProfession profession;
    private static final String BAD_CAP = "Can't be empty";
    private BlockPos harvestPos;
    private final IItemHandler inventory;
    public EntityStrawGolem(EntityType<? extends EntityStrawGolem> type, World worldIn) {
        super(type, worldIn);
        inventory = getCapability(InventoryProvider.CROP_SLOT, null).orElseThrow(() -> new IllegalArgumentException(BAD_CAP));
        profession = getCapability(ProfessionProvider.PROFESSION_CAP, null).orElseThrow(() -> new IllegalArgumentException(BAD_CAP));
        lifespan = getCapability(LifespanProvider.LIFESPAN_CAP, null).orElseThrow(() -> new IllegalArgumentException(BAD_CAP));
        memory = getCapability(MemoryProvider.MEMORY_CAP, null).orElseThrow(() -> new IllegalArgumentException(BAD_CAP));
        harvestPos = BlockPos.ZERO;
    }

    @Nonnull
    @Override
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (ConfigHelper.isSoundsEnabled()) {
            if (goalSelector.getRunningGoals().anyMatch(
                    goal -> goal.getGoal() instanceof GolemFleeGoal || goal.getGoal() instanceof GolemTetherGoal))
                return GOLEM_SCARED;
            else if (holdingFullBlock()) return GOLEM_STRAINED;
            return GOLEM_AMBIENT;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(@Nonnull DamageSource damageSourceIn) {
        return ConfigHelper.isSoundsEnabled() ? GOLEM_HURT : null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ConfigHelper.isSoundsEnabled() ? GOLEM_DEATH : null;
    }

    @Override
    public int getTalkInterval() {
        return holdingFullBlock() ? 60 : 120;
    }


    public static AttributeModifierMap.MutableAttribute registerAttributes() {
        return MobEntity.func_233666_p_()
                .func_233815_a_(Attributes.field_233818_a_, 3.0D)
                .func_233815_a_(Attributes.field_233821_d_, 0.25D);
    }

    @Override
    protected void registerGoals() {
        int priority = 0;
        this.goalSelector.addGoal(priority, new SwimGoal(this));
        this.goalSelector.addGoal(++priority, new GolemFleeGoal(this));
        this.goalSelector.addGoal(++priority, new GolemTemptGoal(this));
        this.goalSelector.addGoal(++priority, new GolemHarvestGoal(this, 0.6D));
        this.goalSelector.addGoal(++priority, new GolemDeliverGoal(this, 0.6D));
        if (ConfigHelper.isTetherEnabled()) {
            this.goalSelector.addGoal(++priority, new GolemTetherGoal(this, 0.9D)); // tether is fast
        }
        this.goalSelector.addGoal(++priority, new GolemWanderGoal(this, 0.6D));
        this.goalSelector.addGoal(++priority, new GolemLookAtPlayerGoal(this, 5.0F));
        this.goalSelector.addGoal(++priority, new GolemLookRandomlyGoal(this));
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!world.isRemote) {
            lifespan.update();
            if (holdingFullBlock() && ConfigHelper.isLifespanPenalty("heavy")) lifespan.update();
            if (isInRain()) lifespan.update();
            if (isInWater() && ConfigHelper.isLifespanPenalty("water")) lifespan.update();
            if (rand.nextInt(40) == 0) {
                PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new MessageLifespan(this));
            }
            if (lifespan.isOver()) attackEntityFrom(DamageSource.MAGIC, getMaxHealth() * 100);
        } else if (lifespan.get() * 4 < ConfigHelper.getLifespan() && lifespan.get() >= 0 && rand.nextInt(80) == 0) {
            world.addParticle(Registry.FLY_PARTICLE, lastTickPosX, lastTickPosY, lastTickPosZ,
                    0, 0, 0);
        }
    }

    /**
     * Determines if the golem is in the rain
     * @return true if the golem is in rain, false otherwise
     */
    public boolean isInRain() {
        return world.isRainingAt(getPosition())
                && world.canSeeSky(getPosition())
                && ConfigHelper.isLifespanPenalty("rain");
    }

    /**
     * Determines if the golem is in the cold
     * @return true if the golem is in the cold, false otherwise
     */
    public boolean isInCold() {
        return world.getBiome(getPosition()).getTemperature(getPosition()) < 0.15F;
    }

    /* Handle inventory */

    /**
     * Returns true if the golem is not holding anything, and false otherwise
     *
     * @return whether the hand is empty
     */
    public boolean isHandEmpty() {
        return getHeldItemMainhand().isEmpty();
    }

    @Nonnull
    @Override
    public ItemStack getHeldItem(@Nonnull Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            return inventory.getStackInSlot(0);
        }
        return ItemStack.EMPTY;
    }

    /**
     * Returns true if the golem is holding a block crop, and false otherwise
     *
     * @return whether the golem is holding a gourd block
     */
    public boolean holdingFullBlock() {
        ItemStack item = inventory.getStackInSlot(0);
        if (!(item.getItem() instanceof BlockItem)) return false;
        BlockItem blockItem = (BlockItem) item.getItem();
        return blockItem != Items.AIR
                && blockItem.getBlock().getDefaultState().isSolid()
                && blockItem.getBlock().asItem() == blockItem;
    }

    /* Miscellaneous */

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public boolean canDespawn(double distanceToClosestPlayer) {
        return false;
    }

    //Drops held item
    @Override
    protected void dropSpecialItems(@Nonnull DamageSource source, int looting, boolean recentlyHitIn) {
        super.dropSpecialItems(source, looting, recentlyHitIn);
        if (EffectiveSide.get().isServer()) {
            entityDropItem(inventory.getStackInSlot(0).copy());
            inventory.getStackInSlot(0).setCount(0);
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    public @Nonnull ActionResultType applyPlayerInteraction(PlayerEntity player, Vector3d vec, Hand hand) {
        if (player.getHeldItem(hand).getItem() == Items.WHEAT) {
            if (EffectiveSide.get().isServer()) {
                if (player.isSneaking()) {
                    StringTextComponent message = new StringTextComponent("Ordering: " + getDisplayName().getString());
                    player.sendMessage(message, Util.field_240973_b_);
                    player.getPersistentData().putInt("golemId", getEntityId());
                } else if (isGolemHurt()) {
                    setHealth(getMaxHealth());
                    playSound(GOLEM_HEAL, 1.0F, 1.0F);
                    playSound(SoundEvents.BLOCK_GRASS_STEP, 1.0F, 1.0F);
                    addToLifespan(12000);
                    if (!player.isCreative()) player.getHeldItem(hand).shrink(1);
                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new MessageLifespan(this));
                }
            }
            if (!player.isSneaking() && isGolemHurt()) {
                spawnHealParticles(lastTickPosX, lastTickPosY, lastTickPosZ);
            }
        }
        return ActionResultType.FAIL;
    }

    /**
     * Returns whether the golem is in an imperfect state (i.e. lifespan is below 90% or it has taken damage)
     * @return whether golem is hurt
     */
    private boolean isGolemHurt() {
        return getHealth() != getMaxHealth() || getCurrentLifespan() < ConfigHelper.getLifespan() * 0.9;
    }

    /**
     * Spawns the heal particles based on location x, y, z
     * @param x coordiante
     * @param y coordinate
     * @param z coordinate
     */
    private void spawnHealParticles(double x, double y, double z) {
        world.addParticle(ParticleTypes.HEART, x + rand.nextDouble() - 0.5, y + 0.4D, z + rand.nextDouble() - 0.5, this.getMotion().x, this.getMotion().y, this.getMotion().z);
    }

    public BlockPos getPosition() {
        return new BlockPos(getPositionVec());
    }

    /* Handles being picked up by iron golem */

    @Override
    public void setPosition(double posX, double posY, double posZ) {
        if (isPassenger() && getRidingEntity() instanceof IronGolemEntity) {
            IronGolemEntity ironGolem = (IronGolemEntity) getRidingEntity();
            double lookX = ironGolem.getLookVec().x;
            double lookZ = ironGolem.getLookVec().z;
            double magnitude = Math.sqrt(lookX * lookX + lookZ * lookZ);
            lookX /= magnitude;
            lookZ /= magnitude;
            super.setPosition(posX + 1.9D * lookX, posY - 0.5D, posZ + 1.9D * lookZ);
        } else {
            super.setPosition(posX, posY, posZ);
        }
    }

    @Override
    public void stopRiding() {
        super.stopRiding();
        if (getRidingEntity() instanceof IronGolemEntity) {
            LivingEntity ridingEntity = (LivingEntity) getRidingEntity();
            double lookX = ridingEntity.getLookVec().x;
            double lookZ = ridingEntity.getLookVec().z;
            double magnitude = Math.sqrt(lookX * lookX + lookZ * lookZ);
            lookX /= magnitude;
            lookZ /= magnitude;
            super.setPosition(getPositionVec().x + lookX, getPositionVec().y, getPositionVec().z + lookZ);
        }
    }

    /**
     * Determines whether or not the block at position pos in world worldIn should be harvested
     *
     * @param worldIn the world
     * @param pos     the position
     * @return whether the golem should harvest the block
     */
    public boolean shouldHarvestBlock(IWorldReader worldIn, BlockPos pos) {
        BlockState state = worldIn.getBlockState(pos);
        if (ConfigHelper.blockHarvestAllowed(state.getBlock())) {
            if (state.getBlock() instanceof CropsBlock)
                return ((CropsBlock) state.getBlock()).isMaxAge(state);
            else if (state.getBlock() instanceof StemGrownBlock)
                return true;
            else if (state.getBlock() instanceof NetherWartBlock)
                return state.get(NetherWartBlock.AGE) == 3;
            else if (state.getBlock() instanceof BushBlock && state.getBlock() instanceof IGrowable) {
                if (state.func_235901_b_(BlockStateProperties.AGE_0_2)) {
                    return state.get(BlockStateProperties.AGE_0_2) == 2;
                } else if (state.func_235901_b_(BlockStateProperties.AGE_0_3)) {
                    return state.get(BlockStateProperties.AGE_0_3) == 3;
                } else if (state.func_235901_b_(BlockStateProperties.AGE_0_5)) {
                    return state.get(BlockStateProperties.AGE_0_5) == 5;
                } else if (state.func_235901_b_(BlockStateProperties.AGE_0_7)) {
                    return state.get(BlockStateProperties.AGE_0_7) == 7;
                }
            }
        }
        return false;
    }

    /**
     * Checks if golem has line of sight on the block
     *
     * @param worldIn the world
     * @param pos     the position
     * @return whether the golem has line of sight
     */
    public boolean canSeeBlock(IWorldReader worldIn, BlockPos pos) {
        Vector3d golemPos = getPositionVec().add(0, 0.75, 0);
        if (getPositionVec().y % 1F != 0) golemPos = golemPos.add(0, 0.5, 0);
        Vector3d blockPos = new Vector3d(pos.getX(), pos.getY() + 0.5, pos.getZ());
        RayTraceContext ctx = new RayTraceContext(golemPos, blockPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this);
        return worldIn.rayTraceBlocks(ctx).getPos().withinDistance(blockPos, 2.5D);
    }

    /* Handles capabilities */

    public IItemHandler getInventory() {
        return inventory;
    }

    /**
     * Returnns the memory, capability, used to store and retrieve chest positions
     *
     * @return the golem's memory capability
     */
    public IMemory getMemory() {
        return memory;
    }

    /**
     * Sets the harvest position to pos
     *
     * @param pos new harvest position
     */
    public void setHarvesting(BlockPos pos) {
        harvestPos = pos;
    }

    /**
     * Returns the position to be used to initiate the GolemHarvestGoal
     *
     * @return the harvest position
     */
    public BlockPos getHarvestPos() {
        return harvestPos;
    }

    /**
     * Clears the harvest position
     */
    public void clearHarvestPos() {
        harvestPos = BlockPos.ZERO;
    }

    public int getCurrentLifespan() {
        return lifespan.get();
    }

    /**
     * Adds time to the lifespan
     *
     * @param time additional lifespan
     */
    public void addToLifespan(int time) {
        lifespan.set(lifespan.get() + time);
    }
}
