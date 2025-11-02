package net.oktawia.crazyae2addons.defs.regs;

import appeng.items.parts.PartItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.items.BuilderPatternItem;
import net.oktawia.crazyae2addons.items.CrazyPatternProviderUpgrade;

import java.util.List;

public class CrazyItemRegistrar {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CrazyAddons.MODID);

    public static List<? extends Item> getItems() {
        return ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .toList();
    }

    public static List<? extends Item> getParts() {
        return ITEMS.getEntries()
                .stream()
                .map(DeferredHolder::get)
                .filter(i -> i instanceof PartItem)
                .toList();
    }

    public static final DeferredItem<Item> CRAZY_PATTERN_PROVIDER_UPGRADE = ITEMS.register("crazy_pattern_provider_upgrade",
            () -> new CrazyPatternProviderUpgrade(new Item.Properties()));

    public static final DeferredItem<Item> BUILDER_PATTERN = ITEMS.register("builder_pattern",
            () -> new BuilderPatternItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
