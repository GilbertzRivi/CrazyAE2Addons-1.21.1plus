package net.oktawia.crazyae2addons.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.menus.BuilderPatternMenu;

import java.nio.charset.StandardCharsets;

public record SendLongStringToServerPacket(String data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SendLongStringToServerPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CrazyAddons.MODID, "send_long_string_to_server"));

    public static final StreamCodec<FriendlyByteBuf, SendLongStringToServerPacket> STREAM_CODEC =
            StreamCodec.of(SendLongStringToServerPacket::encode, SendLongStringToServerPacket::decode);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void encode(FriendlyByteBuf buf, SendLongStringToServerPacket packet) {
        byte[] bytes = packet.data().getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeByteArray(bytes);
    }

    private static SendLongStringToServerPacket decode(FriendlyByteBuf buf) {
        int length = buf.readInt();
        byte[] bytes = buf.readByteArray(length);
        return new SendLongStringToServerPacket(new String(bytes, StandardCharsets.UTF_8));
    }

    public static void handle(SendLongStringToServerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer sender = (ServerPlayer) context.player();
            if (sender.containerMenu instanceof BuilderPatternMenu menu) {
                menu.updateData(packet.data());
            }
//            else if (sender != null && sender.containerMenu instanceof GadgetMenu menu) {
//                menu.updateData(packet.data());
//            }
        });
    }
}