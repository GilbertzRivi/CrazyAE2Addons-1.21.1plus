package net.oktawia.crazyae2addons.network;

import net.minecraft.client.Minecraft;
// 1. ZMIANA IMPORTU: Używamy RegistryFriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.screens.CrazyPatternProviderScreen;

import java.util.List;

public record UpdatePatternsPacket(int startIndex, List<ItemStack> patterns) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdatePatternsPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CrazyAddons.MODID, "update_patterns"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdatePatternsPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UpdatePatternsPacket::startIndex,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC,
            UpdatePatternsPacket::patterns,
            UpdatePatternsPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdatePatternsPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof CrazyPatternProviderScreen<?> screen) {
                screen.updatePatternsFromServer(packet.startIndex(), packet.patterns());
            } 
//            else if (mc.screen instanceof CrazyPatternModifierScreenPP<?> screen) {
//                screen.updatePatternsFromServer(packet.startIndex(), packet.patterns());
//            }
        });
    }
}