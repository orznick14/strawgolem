package com.commodorethrawn.strawgolem.entity;

import com.commodorethrawn.strawgolem.Strawgolem;
import com.commodorethrawn.strawgolem.config.StrawgolemConfig;
import com.commodorethrawn.strawgolem.entity.ai.*;
import com.commodorethrawn.strawgolem.entity.capability.CapabilityHandler;
import com.commodorethrawn.strawgolem.entity.capability.hunger.Hunger;
import com.commodorethrawn.strawgolem.entity.capability.hunger.IHasHunger;
import com.commodorethrawn.strawgolem.entity.capability.lifespan.Lifespan;
import com.commodorethrawn.strawgolem.entity.capability.memory.Memory;
import com.commodorethrawn.strawgolem.entity.capability.tether.IHasTether;
import com.commodorethrawn.strawgolem.entity.capability.tether.Tether;
import com.commodorethrawn.strawgolem.events.GolemChestHandler;
import com.commodorethrawn.strawgolem.events.GolemCreationHandler;
import com.commodorethrawn.strawgolem.network.HealthPacket;
import com.commodorethrawn.strawgolem.network.PacketHandler;
import com.commodorethrawn.strawgolem.registry.StrawgolemSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import static com.commodorethrawn.strawgolem.registry.ParticleRegistry.FLY_PARTICLE;

public class EntityStrawGolem extends GolemEntity implements IHasHunger, IHasTether {
    public static final SoundEvent GOLEM_AMBIENT = new SoundEvent(StrawgolemSounds.GOLEM_AMBIENT_ID);
    public static final SoundEvent GOLEM_STRAINED = new SoundEvent(StrawgolemSounds.GOLEM_STRAINED_ID);
    public static final SoundEvent GOLEM_HURT = new SoundEvent(StrawgolemSounds.GOLEM_HURT_ID);
    public static final SoundEvent GOLEM_DEATH = new SoundEvent(StrawgolemSounds.GOLEM_DEATH_ID);
    public static final SoundEvent GOLEM_HEAL = new SoundEvent(StrawgolemSounds.GOLEM_HEAL_ID);
    public static final SoundEvent GOLEM_SCARED = new SoundEvent(StrawgolemSounds.GOLEM_SCARED_ID);
    public static final SoundEvent GOLEM_INTERESTED = new SoundEvent(StrawgolemSounds.GOLEM_INTERESTED_ID);

    private static final Identifier IDENTIFIER = new Identifier(Strawgolem.MODID, "strawgolem");
    private static final int maxLifespan = StrawgolemConfig.Health.getLifespan() + 12000;
    private static final int maxHunger = StrawgolemConfig.Health.getHunger() + 6000;

    private final Lifespan lifespan;
    private final Memory memory;
    private final SimpleInventory inventory;
    private final Tether tether;
    private final Hunger hunger;
    private boolean harvesting;
    private boolean tempted;

    public static DefaultAttributeContainer.Builder createMob() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 4.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2D);
    }

    public EntityStrawGolem(EntityType<? extends EntityStrawGolem> type, World worldIn) {
        super(type, worldIn);
        lifespan = CapabilityHandler.INSTANCE.get(Lifespan.class).orElseThrow(() -> new InstantiationError("Failed to create lifespan cap"));
        memory = CapabilityHandler.INSTANCE.get(Memory.class).orElseThrow(() -> new InstantiationError("Failed to create memory cap"));
        tether = CapabilityHandler.INSTANCE.get(Tether.class).orElseThrow(() -> new InstantiationError("Failed to create tether cap"));
        hunger = CapabilityHandler.INSTANCE.get(Hunger.class).orElseThrow(() -> new InstantiationError("Failed to create new hunger cap"));
        inventory = new SimpleInventory(1);
        tempted = false;
        // Set default tether value
    }

    @Override
    protected void initGoals() {
        int priority = 0;
        this.goalSelector.add(++priority, new GolemPoutGoal(this));
        this.goalSelector.add(++priority, new GolemFleeGoal(this));
        this.goalSelector.add(++priority, new GolemTemptGoal(this));
        this.goalSelector.add(++priority, new GolemHarvestGoal(this));
        this.goalSelector.add(++priority, new GolemDeliverGoal(this));
        if (StrawgolemConfig.Tether.isTetherEnabled()) {
            this.goalSelector.add(++priority, new GolemTetherGoal<>(this, 0.8D));
        }
        this.goalSelector.add(++priority, new GolemWanderGoal(this));
        this.goalSelector.add(++priority, new GolemLookAtPlayerGoal(this, 4.0F));
        this.goalSelector.add(++priority, new GolemLookRandomlyGoal(this));
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!world.isClient) {
            lifespan.update();
            hunger.update();
            float healthCap = 4.0F * Math.round((float) lifespan.get() / StrawgolemConfig.Health.getLifespan());
            getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(healthCap);
            if (getHealth() > healthCap) setHealth(healthCap);
            if (holdingFullBlock() && StrawgolemConfig.Health.isHeavyPenalty()) {
                lifespan.update();
                hunger.update();
            }
            if (isInRain() && StrawgolemConfig.Health.isRainPenalty()) {
                lifespan.update();
            }
            if (isWet() && StrawgolemConfig.Health.isWaterPenalty()) {
                lifespan.update();
            }
            if (random.nextInt(40) == 0) {
                PacketHandler.INSTANCE.sendInRange(new HealthPacket(this), this, 25.0F);
            }
            if (lifespan.isOver()) {
                damage(DamageSource.MAGIC, getMaxHealth() * 100);
            }
            if (hunger.get() * 4 < StrawgolemConfig.Health.getHunger() && hunger.get() > 0 && random.nextInt(120) == 0) playSound(GOLEM_STRAINED, 1.0F, 1.0F);
        } else if (lifespan.get() * 4 < StrawgolemConfig.Health.getLifespan() && lifespan.get() >= 0 && random.nextInt(80) == 0) {
            world.addParticle(FLY_PARTICLE, prevX, prevY, prevZ,
                    0, 0, 0);
        }
    }

    /**
     * Determines if the golem is in the rain
     * @return true if the golem is in rain, false otherwise
     */
    public boolean isInRain() {
        return world.hasRain(getBlockPos())
                && world.isSkyVisible(getBlockPos());
    }

    /**
     * Determines if the golem is in the cold
     * @return true if the golem is in the cold, false otherwise
     */
    public boolean isInCold() {
        return world.getBiome(getBlockPos()).getTemperature(getBlockPos()) < 0.15F;
    }

    /* Interaction */

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        Item heldItem = player.getStackInHand(hand).getItem();

        if (heldItem == Items.WHEAT) {
            // Check condition
            int newLifespan = lifespan.get() + 12000;
            if (newLifespan > maxLifespan) return ActionResult.FAIL;
            // Compute
            if (!world.isClient()) {
                if (getHealth() < getMaxHealth()) setHealth(getMaxHealth());
                lifespan.set(newLifespan);
                if (!player.isCreative()) player.getStackInHand(hand).decrement(1);
                PacketHandler.INSTANCE.sendInRange(new HealthPacket(this), this, 25.0F);
            // Feedback
                playSound(GOLEM_HEAL, 1.0F, 1.0F);
                playSound(SoundEvents.BLOCK_GRASS_STEP, 1.0F, 1.0F);
            }
            spawnHealParticles(getX(), getY(), getZ());
            // Result
            return ActionResult.CONSUME;
        } else if (heldItem == Items.APPLE) {
            // Check condition
            int newHunger = hunger.get() + 6000;
            if (newHunger > maxHunger) return ActionResult.FAIL;
            // Compute
            if (!world.isClient()) {
                hunger.set(newHunger);
                if (!player.isCreative()) player.getStackInHand(hand).decrement(1);
                PacketHandler.INSTANCE.sendInRange(new HealthPacket(this), this,25.0F);
            // Feedback
                playSound(GOLEM_HEAL, 1.0F, 1.0F);
            }
            spawnHappyParticles(getX(), getY(), getZ());
            // Result
            return ActionResult.CONSUME;
        } else if (heldItem == Items.AIR) {
            // Condition
            if (hand == Hand.OFF_HAND || !player.isSneaking()) return ActionResult.FAIL;
            // Compute
            if (!world.isClient()) {
                GolemChestHandler.addMapping(player.getUuid(), getId());
            // Feedback
                Text message = new TranslatableText("strawgolem.order", getDisplayName().getString());
                player.sendMessage(message, true);
            }
            // Result
            return ActionResult.SUCCESS;
        }

        return ActionResult.FAIL;
    }

    /**
     * Returns whether the golem is in an imperfect state (i.e. lifespan is below 90% or it has taken damage)
     * @return whether golem is hurt
     */
    private boolean isGolemHurt() {
        return lifespan.get() + 6000 < StrawgolemConfig.Health.getLifespan() * 2 || getHealth() < getMaxHealth();
    }

    private void spawnHealParticles(double x, double y, double z) {
        world.addParticle(
                ParticleTypes.HEART,
                x + random.nextDouble() - 0.5, y + 0.4D, z + random.nextDouble() - 0.5,
                this.getVelocity().x, this.getVelocity().y, this.getVelocity().z);
    }

    private void spawnHappyParticles(double x, double y, double z) {
        world.addParticle(
                ParticleTypes.HAPPY_VILLAGER,
                x + random.nextDouble() - 0.5, y + 0.4D, z + random.nextDouble() - 0.5,
                this.getVelocity().x, this.getVelocity().y, this.getVelocity().z);
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        if (random.nextInt(10) == 0) GolemCreationHandler.spawnStrawngGolem(world, this);
    }

    /* Death & Despawning */

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source == DamageSource.SWEET_BERRY_BUSH) return false;
        return super.damage(source, amount);
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);
        if (!world.isClient) {
            dropStack(inventory.getStack(0).copy());
            inventory.getStack(0).setCount(0);
        }
    }

    /* Harvesting */

    /**
     * Checks if golem has line of sight on the block
     * @param worldIn the world
     * @param pos     the position
     * @return whether the golem has line of sight
     */
    public boolean canSeeBlock(WorldView worldIn, BlockPos pos) {
        Vec3d golemPos = getPos().add(0, 0.75, 0);
        if (getPos().y % 1F != 0) golemPos = golemPos.add(0, 0.5, 0);
        Vec3d blockPos = new Vec3d(pos.getX(), pos.getY() + 0.5, pos.getZ());
        RaycastContext ctx = new RaycastContext(golemPos, blockPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this);
        return worldIn.raycast(ctx).getPos().isInRange(blockPos, 1D);
    }

    public void setHarvesting(boolean isHarvesting) {
        harvesting = isHarvesting;
    }

    public boolean isHarvesting() {
        return harvesting;
    }

    /* Handle inventory */

    public SimpleInventory getInventory() {
        return inventory;
    }

    /**
     * Returns true if the golem is not holding anything, and false otherwise
     * @return whether the hand is empty
     */
    public boolean isHandEmpty() {
        return getMainHandStack().isEmpty();
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return inventory.getStack(0);
        }
        return ItemStack.EMPTY;
    }

    /**
     * Returns true if the golem is holding a block crop, and false otherwise
     * @return whether the golem is holding a gourd block
     */
    public boolean holdingFullBlock() {
        ItemStack item = getMainHandStack();
        if (!(item.getItem() instanceof BlockItem blockItem)) return false;
        return blockItem != Items.AIR
                && blockItem.getBlock().getDefaultState().isOpaque()
                && blockItem.getBlock().asItem() == blockItem;
    }
//
//    /* Handles capabilities */
//
    public Lifespan getLifespan() {
        return lifespan;
    }

    /**
     * Returns the memory, capability, used to store and retrieve chest positions
     * @return the golem's memory capability
     */
    public Memory getMemory() {
        return memory;
    }

    /**
     * Returns the tether capability, used to prevent the golem from wandering too far
     * @return the golem's tether capability
     */
    @Override
    public Tether getTether() {
        return tether;
    }

    /**
     * Returns the hunger capability, used to remember if the golem needs to eat!
     */
    @Override
    public Hunger getHunger() {
        return hunger;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void setTempted(boolean tempted) {
        this.tempted = tempted;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean isTempted() {
        return tempted;
    }

    /* Golem Pickup */

    @Override
    public void setPosition(double posX, double posY, double posZ) {
        if (hasVehicle() && (getVehicle() instanceof IronGolemEntity || getVehicle() instanceof EntityStrawngGolem)) {
            GolemEntity golemEntity = (GolemEntity) getVehicle();
            double lookX = golemEntity.getRotationVector().getX();
            double lookZ = golemEntity.getRotationVector().getZ();
            double magnitude = Math.sqrt(lookX * lookX + lookZ * lookZ);
            lookX /= magnitude;
            lookZ /= magnitude;
            super.setPosition(posX + 1.71D * lookX, posY - 0.55D, posZ + 1.71D * lookZ);
        } else {
            super.setPosition(posX, posY, posZ);
        }
    }

    @Override
    public void stopRiding() {
        super.stopRiding();
        if (getVehicle() instanceof IronGolemEntity || getVehicle() instanceof EntityStrawngGolem) {
            LivingEntity ridingEntity = (LivingEntity) getVehicle();
            double lookX = ridingEntity.getYaw();
            double lookZ = ridingEntity.getPitch();
            double magnitude = Math.sqrt(lookX * lookX + lookZ * lookZ);
            lookX /= magnitude;
            lookZ /= magnitude;
            super.setPosition(getPos().x + lookX, getPos().y, getPos().z + lookZ);
        }
    }

    /* Storage */

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.put("lifespan", lifespan.writeTag());
        tag.put("hunger", hunger.writeTag());
        tag.put("memory", memory.writeTag());
        tag.put("inventory", inventory.toNbtList());
        tag.put("tether", tether.writeTag());
        return super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        if (tag.contains("lifespan")) lifespan.readTag(tag.get("lifespan"));
        if (tag.contains("hunger")) hunger.readTag(tag.get("hunger"));
        if (tag.contains("memory")) memory.readTag(tag.get("memory"));
        if (tag.contains("inventory")) inventory.readNbtList((NbtList) tag.get("inventory"));
        if (tag.contains("tether")) tether.readTag(tag.get("tether"));
        super.readNbt(tag);
    }


    /* Sounds */

    @Override
    protected SoundEvent getAmbientSound() {
        if (StrawgolemConfig.Miscellaneous.isSoundsEnabled()) {
            if (goalSelector.getRunningGoals().anyMatch(
                    goal -> goal.getGoal() instanceof GolemFleeGoal || goal.getGoal() instanceof GolemTetherGoal))
                return GOLEM_SCARED;
            else if (holdingFullBlock()) return GOLEM_STRAINED;
            return GOLEM_AMBIENT;
        }
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return StrawgolemConfig.Miscellaneous.isSoundsEnabled() ? GOLEM_HURT : null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return StrawgolemConfig.Miscellaneous.isSoundsEnabled() ? GOLEM_DEATH : null;
    }

    @Override
    public int getMinAmbientSoundDelay() {
        return holdingFullBlock() ? 60 : 120;
    }

    @Override
    protected Identifier getLootTableId() {
        return IDENTIFIER;
    }

}
