package net.oktawia.crazyae2addons.defs.regs;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.blocks.BrokenPatternProviderBlock;

import java.util.List;
import java.util.function.Supplier;

public class CrazyBlockRegistrar {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CrazyAddons.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS =
            DeferredRegister.createItems(CrazyAddons.MODID);

    public static List<? extends Block> getBlocks() {
        return BLOCKS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .toList();
    }
    public static List<? extends Item> getBlockItems() {
        return BLOCK_ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .toList();
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        BLOCK_ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static final DeferredBlock<BrokenPatternProviderBlock> BROKEN_PATTERN_PROVIDER_BLOCK = registerBlock(
            "broken_pattern_provider", BrokenPatternProviderBlock::new);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ITEMS.register(eventBus);
    }
}
