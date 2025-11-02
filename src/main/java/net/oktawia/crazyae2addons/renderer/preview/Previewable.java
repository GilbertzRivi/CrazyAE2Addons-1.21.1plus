package net.oktawia.crazyae2addons.renderer.preview;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface Previewable {
    @OnlyIn(Dist.CLIENT)
    PreviewInfo getPreviewInfo();

    @OnlyIn(Dist.CLIENT)
    void setPreviewInfo(PreviewInfo previewInfo);
}