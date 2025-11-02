package net.oktawia.crazyae2addons.renderer.preview;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.oktawia.crazyae2addons.entities.AutoBuilderBE;
import net.oktawia.crazyae2addons.renderer.BuilderPreviewRenderer;

import java.util.ArrayList;

public class AutoBuilderPreviewRenderer {

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        Frustum frustum = event.getFrustum();

        for (AutoBuilderBE be : AutoBuilderBE.CLIENT_INSTANCES) {
            if (be == null || be.getLevel() == null || !be.getLevel().isClientSide) continue;

            BlockPos origin = be.getBlockPos();
            if (origin.distSqr(mc.player.blockPosition()) > 64 * 64) continue;

            BlockPos cursorP = be.getGhostRenderPos();
            if (cursorP != null) {
                if (cursorP.distSqr(mc.player.blockPosition()) <= BuilderPreviewRenderer.MAX_DIST_SQ && frustum.isVisible(new AABB(cursorP))) {
                    BlockState cursorState = Blocks.WHITE_STAINED_GLASS.defaultBlockState();
                    BakedModel cursorModel = dispatcher.getBlockModel(cursorState);

                    ArrayList<PreviewInfo.BlockInfo> cursorList = new ArrayList<>();
                    cursorList.add(new PreviewInfo.BlockInfo(cursorP, cursorState, cursorModel));
                    PreviewInfo cursorPreview = new PreviewInfo(cursorList);

                    float oldAlpha = BuilderPreviewRenderer.BUILDER_ALPHA;
                    BuilderPreviewRenderer.BUILDER_ALPHA = 0.7f;
                    BuilderPreviewRenderer.render(cursorPreview, event, false);
                    BuilderPreviewRenderer.BUILDER_ALPHA = oldAlpha;
                }
            }

            if (!be.isPreviewEnabled()) {
                continue;
            }

            PreviewInfo currentPreview = rebuildPreview(be, dispatcher);

            BuilderPreviewRenderer.render(currentPreview, event, true);
        }
    }

    private static PreviewInfo rebuildPreview(AutoBuilderBE be, BlockRenderDispatcher dispatcher) {
        var positions = be.getPreviewPositions();
        var palette   = be.getPreviewPalette();
        var indices   = be.getPreviewIndices();

        var list = new ArrayList<PreviewInfo.BlockInfo>();
        for (int i = 0; i < positions.size() && i < indices.length; i++) {
            int palIndex = indices[i];
            if (palIndex < 0 || palIndex >= palette.size()) continue;

            BlockState state = AutoBuilderPreviewStateCache.parseBlockState(palette.get(palIndex));
            if (state == null) continue;

            BakedModel model = dispatcher.getBlockModel(state);
            list.add(new PreviewInfo.BlockInfo(positions.get(i), state, model));
        }

        return new PreviewInfo(list);
    }
}