package net.oktawia.crazyae2addons.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;

import java.util.Set;

public class CrazyLootTableProvider extends BlockLootSubProvider {
    protected CrazyLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        for (var block : getKnownBlocks()) {
            this.dropSelf(block);
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return CrazyBlockRegistrar.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
