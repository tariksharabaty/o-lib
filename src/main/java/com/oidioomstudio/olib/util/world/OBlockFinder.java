package com.oidioomstudio.olib.util.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.BiPredicate;

/**
 * High-speed BFS vein and block scanner for Fast Miner, Fast Tree, Torchmanator, BoneBone, etc.
 */
public final class OBlockFinder {
    private OBlockFinder() {}

    /**
     * Scans and finds connected blocks matching the given predicate using breadth-first search.
     *
     * @param level          The world level
     * @param startPos       Starting origin block position
     * @param maxBlocks      Maximum number of blocks to match
     * @param matcher        Condition matching valid blocks
     * @param checkDiagonals If true, checks all 26 adjacent neighbors; if false, checks 6 cardinal faces
     * @return List of matched BlockPos in BFS order
     */
    private static final Direction[] DIRECTIONS = Direction.values();

    public static List<BlockPos> findConnected(Level level, BlockPos startPos, int maxBlocks,
                                               BiPredicate<BlockPos, BlockState> matcher,
                                               boolean checkDiagonals) {
        if (level == null || startPos == null || maxBlocks <= 0) {
            return Collections.emptyList();
        }

        List<BlockPos> results = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

        BlockState startState = level.getBlockState(startPos);
        if (!matcher.test(startPos, startState)) {
            return Collections.emptyList();
        }

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty() && results.size() < maxBlocks) {
            BlockPos current = queue.poll();
            results.add(current);

            int cx = current.getX();
            int cy = current.getY();
            int cz = current.getZ();

            if (checkDiagonals) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            mut.set(cx + dx, cy + dy, cz + dz);
                            if (!visited.contains(mut)) {
                                BlockPos neighbor = mut.immutable();
                                visited.add(neighbor);
                                if (matcher.test(neighbor, level.getBlockState(mut))) {
                                    queue.add(neighbor);
                                    if (results.size() + queue.size() >= maxBlocks) break;
                                }
                            }
                        }
                    }
                }
            } else {
                for (Direction dir : DIRECTIONS) {
                    mut.setWithOffset(current, dir);
                    if (!visited.contains(mut)) {
                        BlockPos neighbor = mut.immutable();
                        visited.add(neighbor);
                        if (matcher.test(neighbor, level.getBlockState(mut))) {
                            queue.add(neighbor);
                            if (results.size() + queue.size() >= maxBlocks) break;
                        }
                    }
                }
            }
        }

        return results;
    }
}
