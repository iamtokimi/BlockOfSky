package net.cerulan.blockofsky;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class ConnectedBlockHelper {
    private static final int MAX_BLOCKS = 1000;

    public static int propagateDimension(Level level, BlockPos startPos, ResourceLocation dimensionId) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(startPos);
        visited.add(startPos);
        int count = 0;

        while (!queue.isEmpty() && count < MAX_BLOCKS) {
            BlockPos current = queue.poll();
            var be = level.getBlockEntity(current);

            if (be instanceof SkyBlockEntity skyBe) {
                skyBe.setDimensionId(dimensionId);
                count++;

                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        var neighborBe = level.getBlockEntity(neighbor);
                        if (neighborBe instanceof SkyBlockEntity) {
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
        return count;
    }
}
