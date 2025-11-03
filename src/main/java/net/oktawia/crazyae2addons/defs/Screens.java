package net.oktawia.crazyae2addons.defs;

import appeng.init.client.InitScreens;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.menus.*;
import net.oktawia.crazyae2addons.screens.*;

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
        InitScreens.register(
                event,
                CrazyMenuRegistrar.AUTO_BUILDER_MENU.get(),
                AutoBuilderScreen<AutoBuilderMenu>::new,
                "/screens/auto_builder.json"
        );
        InitScreens.register(
                event,
                CrazyMenuRegistrar.BUILDER_PATTERN_MENU.get(),
                BuilderPatternScreen<BuilderPatternMenu>::new,
                "/screens/builder_pattern.json"
        );
        InitScreens.register(
                event,
                CrazyMenuRegistrar.NOKIA3310_MENU.get(),
                Nokia3310Screen<Nokia3310Menu>::new,
                "/screens/nokia3310.json"
        );
        InitScreens.register(
                event,
                CrazyMenuRegistrar.AMPERE_METER_MENU.get(),
                AmpereMeterScreen<AmpereMeterMenu>::new,
                "/screens/ampere_meter.json"
        );
    }
}