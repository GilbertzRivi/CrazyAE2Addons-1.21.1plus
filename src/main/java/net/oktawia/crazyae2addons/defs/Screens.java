package net.oktawia.crazyae2addons.defs;

import appeng.init.client.InitScreens;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.menus.BrokenPatternProviderMenu;
import net.oktawia.crazyae2addons.menus.CrazyPatternProviderMenu;
import net.oktawia.crazyae2addons.screens.BrokenPatternProviderScreen;
import net.oktawia.crazyae2addons.screens.CrazyPatternProviderScreen;

public final class Screens {

    public static void register(RegisterMenuScreensEvent event) {

        InitScreens.register(
                event,
                CrazyMenuRegistrar.BROKEN_PATTERN_PROVIDER_MENU.get(),
                BrokenPatternProviderScreen<BrokenPatternProviderMenu>::new,
                "/screens/broken_pattern_provider.json"
        );
        InitScreens.register(
                event,
                CrazyMenuRegistrar.CRAZY_PATTERN_PROVIDER_MENU.get(),
                CrazyPatternProviderScreen<CrazyPatternProviderMenu>::new,
                "/screens/crazy_pattern_provider.json"
        );
    }
}