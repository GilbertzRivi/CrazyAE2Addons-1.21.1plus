package net.oktawia.crazyae2addons.defs;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;

public class UpgradeCards {
    public UpgradeCards(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Upgrades.add(AEItems.SPEED_CARD, CrazyBlockRegistrar.AUTO_BUILDER_BLOCK.get(), 6, "group.auto_builder.name");
            Upgrades.add(AEItems.CRAFTING_CARD, CrazyBlockRegistrar.AUTO_BUILDER_BLOCK.get(), 1, "group.auto_builder.name");
        });
    }
}