package net.oktawia.crazyae2addons.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class CrazyBlockTagProvider extends BlockTagsProvider {
    public CrazyBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CrazyAddons.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (var block : CrazyBlockRegistrar.getBlocks()){
            this.tag(BlockTags.NEEDS_IRON_TOOL)
                    .add(block);
            this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .add(block);
        }
    }
}
