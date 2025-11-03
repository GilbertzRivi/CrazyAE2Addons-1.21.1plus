package net.oktawia.crazyae2addons;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.oktawia.crazyae2addons.renderer.GadgetCostPreviewClient;
import net.oktawia.crazyae2addons.renderer.PreviewTooltipRenderer;
import net.oktawia.crazyae2addons.renderer.preview.AutoBuilderPreviewRenderer;
import net.oktawia.crazyae2addons.renderer.preview.HeldStructureGadgetPreviewRenderer;

@Mod(value = CrazyAddons.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CrazyAddons.MODID, value = Dist.CLIENT)
public class CrazyAddonsClient {
    public CrazyAddonsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        CrazyAddons.LOGGER.info("CrazyAE2Addons loading on client...");
        NeoForge.EVENT_BUS.register(AutoBuilderPreviewRenderer.class);
        NeoForge.EVENT_BUS.register(HeldStructureGadgetPreviewRenderer.class);
        NeoForge.EVENT_BUS.register(GadgetCostPreviewClient.class);
    }
}
