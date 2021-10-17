package com.commodorethrawn.strawgolem.storage;

import com.commodorethrawn.strawgolem.crop.CropHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class StrawgolemSaveData {

    private static final int TAG_COMPOUND = 10;

    private final File worldDataDir;

    public StrawgolemSaveData(MinecraftServer server) {
        worldDataDir = new File(server.getSavePath(WorldSavePath.ROOT) + "\\strawgolem");
        if (!worldDataDir.exists()) worldDataDir.mkdirs();
    }

    private static final String POS = "pos";

    public void loadData(World world) throws IOException {
        File saveFile = new File(worldDataDir, getFileName(world));
        if (saveFile.exists() && saveFile.isFile()) {
            NbtCompound worldTag = NbtIo.readCompressed(saveFile);
            NbtList positionsTag = worldTag.getList(POS, TAG_COMPOUND);
            positionsTag.forEach(tag -> {
                BlockPos pos = NbtHelper.toBlockPos((NbtCompound) tag);
                CropHandler.INSTANCE.addCrop(world, pos);
            });
        }
    }

    public void saveData(World world) throws IOException {
        NbtCompound worldTag = new NbtCompound();
        NbtList positionsTag = new NbtList();
        Iterator<BlockPos> cropIterator = CropHandler.INSTANCE.getCrops(world);
        while (cropIterator.hasNext()) {
            BlockPos pos = cropIterator.next();
            positionsTag.add(NbtHelper.fromBlockPos(pos));
        }
        worldTag.put(POS, positionsTag);

        File file = new File(worldDataDir, getFileName(world));
        NbtIo.writeCompressed(worldTag, file);
    }

    private String getFileName(World world) {
        Identifier id = world.getRegistryKey().getValue();
        return id.getNamespace() + "-" + id.getPath() + ".dat";
    }
}
