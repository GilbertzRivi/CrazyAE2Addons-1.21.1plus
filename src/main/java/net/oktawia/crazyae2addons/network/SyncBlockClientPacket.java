package net.oktawia.crazyae2addons.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.entities.CrazyPatternProviderBE;

public record SyncBlockClientPacket(BlockPos pos, int added) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncBlockClientPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CrazyAddons.MODID, "sync_block_client"));

    public static final StreamCodec<FriendlyByteBuf, SyncBlockClientPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            SyncBlockClientPacket::pos,
            ByteBufCodecs.INT,
            SyncBlockClientPacket::added,
            SyncBlockClientPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncBlockClientPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            BlockEntity be = mc.level.getBlockEntity(packet.pos());
            if (be instanceof CrazyPatternProviderBE myBe) {
                myBe.setAdded(packet.added());
            }
        });
    }
}