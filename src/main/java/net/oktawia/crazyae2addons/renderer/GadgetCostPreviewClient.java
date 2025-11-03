package net.oktawia.crazyae2addons.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents; // <-- NOWY IMPORT
import net.minecraft.nbt.CompoundTag; // <-- NOWY IMPORT
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData; // <-- NOWY IMPORT
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.oktawia.crazyae2addons.items.Nokia3310; // <-- ZMIENIONA NAZWA

public class GadgetCostPreviewClient {

    private static final int RED = 0xFF4040;
    private static final long TOOLTIP_TTL_MS = 175L;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post e) {

        var mc = Minecraft.getInstance();
        var player = mc.player;
        var level = mc.level;
        if (player == null || level == null) return;

        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(held.getItem() instanceof Nokia3310)) {
            held = player.getItemInHand(InteractionHand.OFF_HAND);
            if (!(held.getItem() instanceof Nokia3310)) return;
        }

        HitResult hr = mc.hitResult;
        if (!(hr instanceof BlockHitResult bhr) || bhr.getType() != HitResult.Type.BLOCK) return;

        BlockPos lookAt = bhr.getBlockPos();
        Direction face = bhr.getDirection();

        int energy = readEnergyFromNBT(held);

        CustomData customData = held.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = (customData != null) ? customData.copyTag() : null;
        
        if (tag != null && tag.contains("selA")) {
            int[] a = tag.getIntArray("selA");
            if (a.length == 3) {
                BlockPos cornerA = new BlockPos(a[0], a[1], a[2]);

                int cost = Nokia3310.computeCutCostFE(level, cornerA, lookAt, lookAt);
                showCostWithTTL("Cut: %,d FE", cost, energy);
                return;
            }
        }

        if (tag != null && tag.getBoolean("code")) {
            BlockPos originWorld = lookAt.relative(face);
            Direction pasteFacing = player.getDirection();
            Nokia3310.Basis basis = Nokia3310.Basis.forFacing(pasteFacing);

            int[] posArr = tag.getIntArray("preview_positions");
            double req = 0.0;
            for (int i = 0; i + 2 < posArr.length; i += 3) {
                BlockPos local = new BlockPos(posArr[i], posArr[i + 1], posArr[i + 2]);
                BlockPos world = Nokia3310.localToWorld(local, originWorld, basis);
                req += Nokia3310.calcStepCostFE(originWorld, world);
            }
            int cost = (int) Math.ceil(req);
            showCostWithTTL("Paste: %,d FE", cost, energy);
        }
    }

    private static void showCostWithTTL(String fmt, int cost, int energy) {
        String msg = String.format(fmt, cost);
        Integer color = (cost > energy) ? RED : null;
        PreviewTooltipRenderer.set(msg, color, TOOLTIP_TTL_MS);
    }

    private static int readEnergyFromNBT(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return 0;
        return customData.copyTag().getInt("energy");
    }
}