package com.t2pellet.strawgolem.crop;

import com.t2pellet.strawgolem.StrawgolemCommon;
import com.t2pellet.strawgolem.util.struct.PosTree;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;

class WorldCropsImpl extends SavedData implements WorldCrops {

    private static final String TAG_VERSION = "version";
    private static final String TAG_POS = "pos";
    private static final int VERSION = 1;

    private final Level level;
    private PosTree tree;

    WorldCropsImpl(Level level) {
        tree = PosTree.create();
        this.level = level;
    }

    static WorldCropsImpl load(Level level, CompoundTag tag) {
        StrawgolemCommon.LOG.info("Loading strawgolem save data");

        WorldCropsImpl crops = new WorldCropsImpl(level);
        if (!tag.contains(TAG_VERSION) || tag.getInt(TAG_VERSION) != VERSION) return crops;

        ListTag positions = tag.getList(TAG_POS, Tag.TAG_COMPOUND);
        for (Tag position : positions) {
            BlockPos pos = NbtUtils.readBlockPos((CompoundTag) position);
            if (CropRegistry.INSTANCE.isGrownCrop(level, pos)) {
                crops.addCrop(pos);
            }
        }
        return crops;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        StrawgolemCommon.LOG.debug("Saving strawgolem save data");

        ListTag positionsTag = new ListTag();
        Iterator<BlockPos> cropIterator = getCrops();
        while (cropIterator.hasNext()) {
            BlockPos pos = cropIterator.next();
            if (level.hasChunk(pos.getX(), pos.getZ()) && CropRegistry.INSTANCE.isGrownCrop(level, pos)) {
                positionsTag.add(NbtUtils.writeBlockPos(pos));
            }
        }
        tag.put(TAG_POS, positionsTag);
        tag.putInt(TAG_VERSION, VERSION);

        return tag;
    }

    @Override
    public void reset() {
        tree = PosTree.create();
        setDirty();
    }

    @Override
    public void addCrop(BlockPos pos) {
        tree.insert(pos);
        setDirty();
    }

    @Override
    public void removeCrop(BlockPos pos) {
        tree.delete(pos);
        setDirty();
    }

    @Override
    public BlockPos getNearestCrop(BlockPos pos, int maxRange) {
        Vec3i result = tree.findNearest(pos, maxRange);
        return result == null ? null : new BlockPos(result);
    }

    @Override
    public Iterator<BlockPos> getCrops() {
        Iterator<Vec3i> iterator = tree.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public BlockPos next() {
                return new BlockPos(iterator.next());
            }
        };
    }
}
