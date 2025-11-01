package net.oktawia.crazyae2addons;

import appeng.api.AECapabilities;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import appeng.api.parts.RegisterPartCapabilitiesEventInternal;
import appeng.core.definitions.AEBlockEntities;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.oktawia.crazyae2addons.defs.Screens;
import net.oktawia.crazyae2addons.defs.regs.*;
import net.oktawia.crazyae2addons.network.SyncBlockClientPacket; // Zaimportuj swoje pakiety
// Zaimportuj tutaj wszystkie swoje klasy pakietów
// import net.oktawia.crazyae2addons.network.*;

import net.oktawia.crazyae2addons.network.UpdatePatternsPacket;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent; // <-- NOWY IMPORT
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent; // <-- NOWY IMPORT
import net.neoforged.neoforge.network.registration.PayloadRegistrar; // <-- NOWY IMPORT


@Mod(CrazyAddons.MODID)
public class CrazyAddons {
    public static final String MODID = "crazyae2addons";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CrazyAddons(IEventBus modEventBus, ModContainer modContainer) {
        CrazyCreativeTabRegistrar.register(modEventBus);
        CrazyItemRegistrar.register(modEventBus);
        CrazyBlockRegistrar.register(modEventBus);
        CrazyBlockEntityRegistrar.register(modEventBus);
        CrazyMenuRegistrar.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(Screens::register);
        modEventBus.addListener(CrazyAddons::initCapabilities);

        modEventBus.addListener(this::registerPayloadHandlers);

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, CrazyConfig.COMMON_SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("CrazyAE2Addons loading...");
        event.enqueueWork(CrazyBlockEntityRegistrar::runBlockEntitySetup);
    }

    private void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID).versioned("1");

        registrar.playToClient(
                SyncBlockClientPacket.TYPE,
                SyncBlockClientPacket.STREAM_CODEC,
                SyncBlockClientPacket::handle
        );
        registrar.playToClient(
                UpdatePatternsPacket.TYPE,
                UpdatePatternsPacket.STREAM_CODEC,
                UpdatePatternsPacket::handle
        );
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("CrazyAE2Addons loading on server...");
    }

    private static void initCapabilities(RegisterCapabilitiesEvent event) {
        for (var type : CrazyBlockEntityRegistrar.BLOCK_ENTITIES.getEntries()) {
            event.registerBlockEntity(
                    AECapabilities.IN_WORLD_GRID_NODE_HOST, type.get(),
                    (be, context) -> (IInWorldGridNodeHost) be);
        }

//        var partEvent = new RegisterPartCapabilitiesEvent();
//        partEvent.addHostType(AEBlockEntities.CABLE_BUS.get());
//        partEvent.register(
//                Capabilities.ItemHandler.BLOCK,
//                (part, context) -> part.getExposedApi(),
//                RRItemP2PTunnel.class);
//        ModLoader.postEvent(partEvent);
//        RegisterPartCapabilitiesEventInternal.register(partEvent, event);
    }
}