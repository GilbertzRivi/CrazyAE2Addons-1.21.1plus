package net.oktawia.crazyae2addons.recipes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.Map;

public record StructureSnapshot(int sizeX, int sizeY, int sizeZ, Map<BlockPos, BlockState> blocks) {
    public StructureSnapshot(int sizeX, int sizeY, int sizeZ, Map<BlockPos, BlockState> blocks) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.blocks = Map.copyOf(blocks);
    }

    public BlockState get(int x, int y, int z) {
        return blocks.getOrDefault(new BlockPos(x, y, z), null);
    }

    @Override
    public Map<BlockPos, BlockState> blocks() {
        return Collections.unmodifiableMap(blocks);
    }
}