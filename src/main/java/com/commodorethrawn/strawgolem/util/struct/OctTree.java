package com.commodorethrawn.strawgolem.util.struct;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.*;

public class OctTree implements PosTree {

    private static final BlockBox DEFAULT = new BlockBox(-2147483646, -2147483646, -2147483646, 2147483646, 2147483646, 2147483646);
    private static final HashMap<Triplet<Boolean, Boolean, Boolean>, Octant> octants = new HashMap<>();
    static {
        octants.put(new Triplet<>(true, true, true), Octant.FIRST);
        octants.put(new Triplet<>(true, true, false), Octant.SECOND);
        octants.put(new Triplet<>(false, true, true), Octant.THIRD);
        octants.put(new Triplet<>(false, true, false), Octant.FOURTH);
        octants.put(new Triplet<>(true, false, true), Octant.FIFTH);
        octants.put(new Triplet<>(true, false, false), Octant.SIXTH);
        octants.put(new Triplet<>(false, false, true), Octant.SEVENTH);
        octants.put(new Triplet<>(false, false, false), Octant.EIGHTH);  
    }

    
    private final OctTree parent;
    private final BlockBox boundary;
    private final Vec3i center;
    private final HashMap<Octant, OctTree> octTrees = new HashMap<>();
    private BlockPos point;

    private enum Octant {
        FIRST, SECOND, THIRD, FOURTH, FIFTH, SIXTH, SEVENTH, EIGHTH

    }

    OctTree() {
        parent = null;
        boundary = DEFAULT;
        center = BlockPos.ORIGIN;
        buildMaps();
    }

    public OctTree(final OctTree parent, final Octant octant) {
        this.parent = parent;
        switch (octant) {
            case FIRST -> boundary = new BlockBox(parent.boundary.getMinX(), parent.boundary.getMinY(), parent.boundary.getMinZ(), parent.center.getX(), parent.center.getY(), parent.center.getZ());
            case SECOND -> boundary = new BlockBox(parent.boundary.getMinX(), parent.boundary.getMinY(), parent.center.getZ(), parent.center.getX(), parent.center.getY(), parent.boundary.getMaxZ());
            case THIRD -> boundary = new BlockBox(parent.center.getX(), parent.boundary.getMinY(), parent.boundary.getMinZ(), parent.boundary.getMaxX(), parent.center.getY(), parent.center.getZ());
            case FOURTH -> boundary = new BlockBox(parent.center.getX(), parent.boundary.getMinY(), parent.center.getZ(), parent.boundary.getMaxX(), parent.center.getY(), parent.boundary.getMaxZ());
            case FIFTH -> boundary = new BlockBox(parent.boundary.getMinX(), parent.center.getY(), parent.boundary.getMinZ(), parent.center.getX(), parent.boundary.getMaxY(), parent.center.getZ());
            case SIXTH -> boundary = new BlockBox(parent.boundary.getMinX(), parent.center.getY(), parent.center.getZ(), parent.center.getX(), parent.boundary.getMaxY(), parent.boundary.getMaxZ());
            case SEVENTH -> boundary = new BlockBox(parent.center.getX(), parent.center.getY(), parent.boundary.getMinZ(), parent.boundary.getMaxX(), parent.boundary.getMaxY(), parent.center.getZ());
            default -> boundary = new BlockBox(parent.center.getX(), parent.center.getY(), parent.center.getZ(), parent.boundary.getMaxX(), parent.boundary.getMaxY(), parent.boundary.getMaxZ());
        }
        center = boundary.getCenter();
        buildMaps();
    }

    private void buildMaps() {
        octTrees.put(Octant.FIRST, null);
        octTrees.put(Octant.SECOND, null);
        octTrees.put(Octant.THIRD, null);
        octTrees.put(Octant.FOURTH, null);
        octTrees.put(Octant.FIFTH, null);
        octTrees.put(Octant.SIXTH, null);
        octTrees.put(Octant.SEVENTH, null);
        octTrees.put(Octant.EIGHTH, null);
    }

    // ADT methods

    @Override
    public void insert(BlockPos pos) {
        if (pos == null) return;
        OctTree result = search(pos);
        if (pos.equals(result.point)) return;
        if (result.isLeaf()) {
            if (result.point == null) result.point = pos;
            else result.insertToLeaf(pos);
        } else {
            Octant posOctant = result.getOctant(pos);
            OctTree posTree = new OctTree(result, posOctant);
            posTree.point = pos;
            result.setOctTree(posOctant, posTree);
        }
    }

    @Override
    public void delete(BlockPos pos) {
        if (pos == null) return;
        OctTree result = search(pos);
        if (pos.equals(result.point)) {
            if (result.parent != null) {
                Octant octant = result.parent.getOctant(pos);
                result.parent.setOctTree(octant, null);
            } else result.point = null;
        }
    }

    @Override
    public BlockPos findNearest(final BlockPos pos) {
        if (isEmpty()) return null;
        if (pos.equals(point)) return pos;
        OctTree octTree = getOctTree(getOctant(pos));
        if (octTree == null) {
            PriorityQueue<OctTree> closestPQ = new PriorityQueue<>((o1, o2) -> {
                return Float.compare(o1.getManhattanDistance(pos), o2.getManhattanDistance(pos));
            });
            closestPQ.offer(this);
            return findNearest(closestPQ, pos);
        }
        return octTree.findNearest(pos);
    }

    @Override
    public boolean isEmpty() {
        return isLeaf() && point == null;
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return new TreeIterator(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OctTree)) return false;
        OctTree tree = (OctTree) obj;
        return boundary.equals(tree.boundary);
    }

    // Smaller helpers

    private Octant getOctant(final BlockPos query) {
        Triplet<Boolean, Boolean, Boolean> queryValues = new Triplet<>(
                query.getX() < center.getX(),
                query.getY() < center.getY(),
                query.getZ() < center.getZ());
        return octants.get(queryValues);
    }

    private OctTree getOctTree(final Octant query) {
        return octTrees.get(query);
    }

    private OctTree getOctTree(final BlockPos query) {
        return getOctTree(getOctant(query));
    }

    private void setOctTree(Octant key, OctTree value) {
        octTrees.put(key, value);
    }

    private boolean isLeaf() {
        return octTrees.values().stream().allMatch(Objects::isNull);
    }

    // Bigger Helpers

    private OctTree search(final BlockPos pos) {
        if (pos.equals(point)) return this;
        OctTree octTree = getOctTree(pos);
        if (octTree != null) return octTree.search(pos);
        return this;
    }

    private void insertToLeaf(BlockPos pos) {
        Octant posOctant = getOctant(pos);
        Octant pointOctant = getOctant(point);

        OctTree posTree = new OctTree(this, posOctant);
        posTree.point = pos;
        setOctTree(posOctant, posTree);

        if (posOctant == pointOctant) {
            posTree.insertToLeaf(point);
        } else {
            OctTree pointTree = new OctTree(this, pointOctant);
            pointTree.point = point;
            setOctTree(pointOctant, pointTree);
        }
        point = null;
    }

    private static BlockPos findNearest(PriorityQueue<OctTree> closestPQ, final BlockPos pos) {
        OctTree closest = closestPQ.poll();
        if (closest.point != null) return closest.point;
        for (OctTree tree : closest.octTrees.values()) {
            if (tree != null) closestPQ.offer(tree);
        }
        return findNearest(closestPQ, pos);
    }

    private float getManhattanDistance(BlockPos pos) {
        if (point != null) return (float) point.getManhattanDistance(pos);
        float minDistance = Float.MAX_VALUE;
        for (OctTree tree : octTrees.values()) {
            if (tree == null) continue;
            float distance = tree.getManhattanDistance(pos);
            if (distance < minDistance) minDistance = distance;
        }
        return minDistance;
    }

    // Iterator class
    private static class TreeIterator implements Iterator<BlockPos> {

        private final Stack<BlockPos> positions = new Stack<>();

        private TreeIterator(OctTree tree) {
            buildStack(tree);
        }

        private void buildStack(OctTree parent) {
            for (OctTree tree : parent.octTrees.values()) {
                if (tree != null) {
                    if (tree.point != null) positions.push(tree.point);
                    else buildStack(tree);
                }
            }
        }

        @Override
        public boolean hasNext() {
            return !positions.isEmpty();
        }

        @Override
        public BlockPos next() {
            if (!hasNext()) throw new NoSuchElementException();
            return positions.pop();
        }
    }

}
