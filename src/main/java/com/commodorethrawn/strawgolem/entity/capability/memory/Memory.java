package com.commodorethrawn.strawgolem.entity.capability.memory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.HashSet;
import java.util.Set;

public class Memory implements IMemory {

    private final Set<Pair<IWorld, BlockPos>> posList;
    private BlockPos priority;

    public Memory() {
        posList = new HashSet<>();
        priority = BlockPos.ZERO;
    }

    @Override
    public Set<Pair<IWorld, BlockPos>> getPositions() {
        return posList;
    }

    @Override
    public BlockPos getDeliveryChest(IWorld world, BlockPos pos) {
        if (!priority.equals(BlockPos.ZERO)) {
            return priority;
        }
        BlockPos closest = BlockPos.ZERO;
        for (Pair<IWorld, BlockPos> chest : posList) {
            IWorld chestWorld = chest.getFirst();
            if (!chestWorld.equals(world)) continue;
            BlockPos chestPos = chest.getSecond();
            if (pos.distanceSq(closest) >= pos.distanceSq(chestPos)) {
                closest = chestPos;
            }
        }
        return closest;
    }

    @Override
    public void addPosition(IWorld world, BlockPos pos) {
        posList.add(Pair.of(world, pos));
    }

    @Override
    public void removePosition(IWorld world, BlockPos pos) {
        if (priority.equals(pos)) priority = BlockPos.ZERO;
        posList.remove(Pair.of(world, pos));
    }

    @Override
    public BlockPos getPriorityChest() {
        return priority;
    }


    @Override
    public void setPriorityChest(BlockPos pos) {
        priority = pos;
    }

}
