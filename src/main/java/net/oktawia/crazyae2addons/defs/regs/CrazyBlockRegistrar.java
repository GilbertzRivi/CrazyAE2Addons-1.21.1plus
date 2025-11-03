package net.oktawia.crazyae2addons.defs.regs;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.blocks.*;
import net.oktawia.crazyae2addons.items.CrazyPatternProviderBlockItem;

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

    public static final DeferredBlock<CrazyPatternProviderBlock> CRAZY_PATTERN_PROVIDER_BLOCK =
            BLOCKS.register("crazy_pattern_provider", CrazyPatternProviderBlock::new);

    public static final DeferredItem<BlockItem> CRAZY_PATTERN_PROVIDER_BLOCK_ITEM =
            BLOCK_ITEMS.register("crazy_pattern_provider",
                    () -> new CrazyPatternProviderBlockItem(CRAZY_PATTERN_PROVIDER_BLOCK.get(), new Item.Properties()));

    public static final DeferredBlock<BrokenPatternProviderBlock> BROKEN_PATTERN_PROVIDER_BLOCK = registerBlock(
            "broken_pattern_provider", BrokenPatternProviderBlock::new);

    public static final DeferredBlock<AutoBuilderBlock> AUTO_BUILDER_BLOCK = registerBlock(
            "auto_builder", AutoBuilderBlock::new);

    public static final DeferredBlock<AutoBuilderCreativeSupplyBlock> AUTO_BUILDER_CREATIVE_SUPPLY_BLOCK = registerBlock(
            "auto_builder_creative_supply", AutoBuilderCreativeSupplyBlock::new);

    public static final DeferredBlock<AmpereMeterBlock> AMPERE_METER_BLOCK = registerBlock(
            "ampere_meter", AmpereMeterBlock::new);

    public static final DeferredBlock<DataControllerBlock> DATA_CONTROLLER_BLOCK = registerBlock(
            "data_controller", DataControllerBlock::new);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ITEMS.register(eventBus);
    }
}
