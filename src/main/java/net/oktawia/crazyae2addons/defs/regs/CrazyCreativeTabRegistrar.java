package net.oktawia.crazyae2addons.defs.regs;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oktawia.crazyae2addons.CrazyAddons;

import java.util.function.Supplier;

public class CrazyCreativeTabRegistrar {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CrazyAddons.MODID);

    public static final Supplier<CreativeModeTab> TAB = CREATIVE_MODE_TAB.register("crazy_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(Items.STICK.asItem()))
                    .title(Component.translatable("creativetab.title"))
                    .displayItems((itemDisplayParameters, output) -> {
                        for (var item : CrazyItemRegistrar.getItems()) {
                            output.accept(item);
                        }
                        for (var item : CrazyBlockRegistrar.getBlockItems()){
                            output.accept(item);
                        }
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);

    }
}
