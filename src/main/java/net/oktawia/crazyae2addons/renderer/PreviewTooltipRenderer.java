package net.oktawia.crazyae2addons.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.oktawia.crazyae2addons.CrazyAddons;

@OnlyIn(Dist.CLIENT)
public final class PreviewTooltipRenderer {
    private PreviewTooltipRenderer() {}

    public static String text = null;
    public static Integer forceColor = null;
    public static long expireAtMs = 0L;
    public static final long DEFAULT_TTL_MS = 150L;

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(id("autobuilder_tooltip"), PreviewTooltipRenderer::renderLayer);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(CrazyAddons.MODID, path);
    }

    public static void set(String msg) {
        set(msg, null, DEFAULT_TTL_MS);
    }

    public static void set(String msg, Integer color, long ttlMs) {
        text = msg;
        forceColor = color;
        expireAtMs = System.currentTimeMillis() + Math.max(50L, ttlMs);
    }

    private static void renderLayer(GuiGraphics g, DeltaTracker delta) {
        long now = System.currentTimeMillis();
        if (text == null || now > expireAtMs) return;

        var mc = Minecraft.getInstance();
        Font font = mc.font;
        PoseStack pose = g.pose();

        var win = mc.getWindow();
        float x = win.getGuiScaledWidth() / 2f + 8f;
        float y = (win.getGuiScaledHeight() - font.lineHeight) / 2f + 8f;

        pose.pushPose();
        pose.translate(x, y, 0.0F);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        drawText(g, font, text);

        pose.popPose();
    }

    private static void drawText(GuiGraphics g, Font font, String txt) {
        if (forceColor != null) {
            g.drawString(font, txt, 0, 0, forceColor, true);
            return;
        }
        int dx = 0;
        for (int i = 0; i < txt.length(); i++) {
            double t = (txt.length() > 1) ? (i / (double) (txt.length() - 1)) : 0.0;

            int colorInt = SimpleGradient.blueGradient(t);

            String s = String.valueOf(txt.charAt(i));
            g.drawString(font, s, dx, 0, colorInt, true);
            dx += font.width(s);
        }
    }
}