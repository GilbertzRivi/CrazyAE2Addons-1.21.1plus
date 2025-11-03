package net.oktawia.crazyae2addons.defs.regs;

import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.entities.*;
import net.oktawia.crazyae2addons.logic.BuilderPatternHost;
import net.oktawia.crazyae2addons.logic.Nokia3310Host;
import net.oktawia.crazyae2addons.menus.*;
import net.oktawia.crazyae2addons.parts.DisplayPart;


public class CrazyMenuRegistrar {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, CrazyAddons.MODID);

    private static <C extends AEBaseMenu, I> DeferredHolder<MenuType<?>, MenuType<C>> reg(
            String id, MenuTypeBuilder.MenuFactory<C, I> factory, Class<I> host) {

        return MENU_TYPES.register(id,
                () -> MenuTypeBuilder.create(factory, host).build(id));
    }

    private static String id(String s) { return s; }

    public static final DeferredHolder<MenuType<?>, MenuType<BrokenPatternProviderMenu>> BROKEN_PATTERN_PROVIDER_MENU =
            reg(id("broken_pattern_provider"), BrokenPatternProviderMenu::new, BrokenPatternProviderBE.class);

    public static final DeferredHolder<MenuType<?>, MenuType<CrazyPatternProviderMenu>> CRAZY_PATTERN_PROVIDER_MENU =
            reg(id("crazy_pattern_provider"), CrazyPatternProviderMenu::new, CrazyPatternProviderBE.class);

    public static final DeferredHolder<MenuType<?>, MenuType<AutoBuilderMenu>> AUTO_BUILDER_MENU =
            reg(id("auto_builder"), AutoBuilderMenu::new, AutoBuilderBE.class);

    public static final DeferredHolder<MenuType<?>, MenuType<BuilderPatternMenu>> BUILDER_PATTERN_MENU =
            reg(id("builder_pattern"), BuilderPatternMenu::new, BuilderPatternHost.class);

    public static final DeferredHolder<MenuType<?>, MenuType<Nokia3310Menu>> NOKIA3310_MENU =
            reg(id("nokia3310"), Nokia3310Menu::new, Nokia3310Host.class);

    public static final DeferredHolder<MenuType<?>, MenuType<AmpereMeterMenu>> AMPERE_METER_MENU =
            reg(id("ampere_meter"), AmpereMeterMenu::new, AmpereMeterBE.class);

    public static final DeferredHolder<MenuType<?>, MenuType<DisplayMenu>> DISPLAY_MENU =
            reg(id("display"), DisplayMenu::new, DisplayPart.class);

    public static final DeferredHolder<MenuType<?>, MenuType<MEDataControllerMenu>> ME_DATA_CONTROLLER_MENU =
            reg(id("data_controller"), MEDataControllerMenu::new, DataControllerBE.class);

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}