package net.oktawia.crazyae2addons.defs.regs;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.items.BuilderPatternItem;
import net.oktawia.crazyae2addons.items.CrazyPatternProviderUpgrade;
import net.oktawia.crazyae2addons.items.Nokia3310;
import net.oktawia.crazyae2addons.parts.DisplayPart;
import net.oktawia.crazyae2addons.parts.WormholeP2PTunnelPart;

import java.util.List;
import java.util.function.Function;

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

    public static final ItemDefinition<CrazyPatternProviderUpgrade> CRAZY_PATTERN_PROVIDER_UPGRADE = item(
            "Crazy Pattern Provider Upgrade",
            "crazy_pattern_provider_upgrade",
            CrazyPatternProviderUpgrade::new
    );

    public static final ItemDefinition<BuilderPatternItem> BUILDER_PATTERN = item(
            "Builder Pattern",
            "builder_pattern",
            BuilderPatternItem::new
    );

    public static final ItemDefinition<Nokia3310> NOKIA_3310 = item(
            "Nokia 3310",
            "nokia_3310",
            Nokia3310::new
        );

    public static final ItemDefinition<PartItem<WormholeP2PTunnelPart>> WORMHOLE_P2P_TUNNEL = part(
            "Wormhole P2P Tunnel",
            "wormhole_p2p_tunnel",
            WormholeP2PTunnelPart.class,
            WormholeP2PTunnelPart::new
    );

    public static final ItemDefinition<PartItem<DisplayPart>> DISPLAY_PART = part(
            "Display",
            "display",
            DisplayPart.class,
            DisplayPart::new
    );

    public static <T extends IPart> ItemDefinition<PartItem<T>> part(
            String englishName, String id, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return item(englishName, id, p -> new PartItem<>(p, partClass, factory));
    }

    private static <T extends Item> ItemDefinition<T> item(
            String englishName, String id, Function<Item.Properties, T> factory) {
        return new ItemDefinition<>(englishName, ITEMS.registerItem(id, factory));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
