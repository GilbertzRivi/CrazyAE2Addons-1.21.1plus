package net.oktawia.crazyae2addons.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.screens.BuilderPatternScreen;
import net.oktawia.crazyae2addons.screens.Nokia3310Screen;

import java.nio.charset.StandardCharsets;

public record SendLongStringToClientPacket(String data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SendLongStringToClientPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CrazyAddons.MODID, "send_long_string_to_client"));

    public static final StreamCodec<FriendlyByteBuf, SendLongStringToClientPacket> STREAM_CODEC =
            StreamCodec.of(SendLongStringToClientPacket::encode, SendLongStringToClientPacket::decode);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    private static void encode(FriendlyByteBuf buf, SendLongStringToClientPacket packet) {
        byte[] bytes = packet.data().getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeByteArray(bytes);
    }

    private static SendLongStringToClientPacket decode(FriendlyByteBuf buf) {
        int length = buf.readInt();
        byte[] bytes = buf.readByteArray(length);
        return new SendLongStringToClientPacket(new String(bytes, StandardCharsets.UTF_8));
    }

    public static void handle(SendLongStringToClientPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof BuilderPatternScreen<?> screen) {
                screen.setProgram(packet.data());
            } else if (mc.screen instanceof Nokia3310Screen<?> screen) {
                screen.setProgram(packet.data());
            }
        });
    }
}