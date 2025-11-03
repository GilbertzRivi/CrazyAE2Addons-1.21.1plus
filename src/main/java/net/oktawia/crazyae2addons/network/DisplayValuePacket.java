package net.oktawia.crazyae2addons.network;

import appeng.api.parts.IPartHost;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.parts.DisplayPart;

import java.util.HashMap;

public record DisplayValuePacket(
        BlockPos pos,
        String textValue,
        Direction direction,
        byte spin,
        String variables,
        int fontSize,
        boolean mode,
        boolean margin,
        boolean center
) implements CustomPacketPayload {

    public DisplayValuePacket {
        if (variables == null) {
            variables = "";
        }
    }

    public static final CustomPacketPayload.Type<DisplayValuePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CrazyAddons.MODID, "display_value"));

    public static final StreamCodec<FriendlyByteBuf, DisplayValuePacket> STREAM_CODEC =
            StreamCodec.of(DisplayValuePacket::encode, DisplayValuePacket::decode);

    private static void encode(FriendlyByteBuf buf, DisplayValuePacket packet) {
        buf.writeBlockPos(packet.pos());
        buf.writeUtf(packet.direction().getSerializedName());
        buf.writeUtf(packet.textValue());
        buf.writeByte(packet.spin());
        buf.writeUtf(packet.variables());
        buf.writeInt(packet.fontSize());
        buf.writeBoolean(packet.mode());
        buf.writeBoolean(packet.margin());
        buf.writeBoolean(packet.center());
    }

    private static DisplayValuePacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String dirName = buf.readUtf();
        String textValue = buf.readUtf();
        byte spin = buf.readByte();
        String variables = buf.readUtf();
        int fontSize = buf.readInt();
        boolean mode = buf.readBoolean();
        boolean margin = buf.readBoolean();
        boolean center = buf.readBoolean();

        Direction dir = Direction.byName(dirName);
        if (dir == null) dir = Direction.SOUTH;

        return new DisplayValuePacket(pos, textValue, dir, spin, variables, fontSize, mode, margin, center);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DisplayValuePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                BlockEntity te = level.getBlockEntity(packet.pos());
                if (te instanceof IPartHost host) {
                    if (host.getPart(packet.direction()) instanceof DisplayPart displayPart) {

                        displayPart.textValue = packet.textValue();
                        displayPart.spin = packet.spin();
                        displayPart.mode = packet.mode();
                        displayPart.fontSize = packet.fontSize();
                        displayPart.margin = packet.margin();
                        displayPart.center = packet.center();

                        if (packet.variables() != null && !packet.variables().isEmpty()) {
                            HashMap<String, String> variablesMap = new HashMap<>();
                            for (String s : packet.variables().split("\\|")) {
                                if (s.isEmpty()) continue;
                                String[] arr = s.split("=", 2);
                                if (arr.length == 2 && !arr[0].isEmpty()) {
                                    variablesMap.put(arr[0], arr[1]);
                                }
                            }
                            if (!variablesMap.isEmpty()) {
                                displayPart.variables = variablesMap;
                            }
                        } else {
                            displayPart.variables.clear();
                        }

                        host.markForUpdate();
                    }
                }
            }
        });
    }
}