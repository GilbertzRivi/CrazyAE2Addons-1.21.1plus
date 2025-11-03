package net.oktawia.crazyae2addons.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.oktawia.crazyae2addons.misc.WormholeAnchor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Player.class, remap = false)
public abstract class PlayerCanInteractMixin {

    @Inject(method = "canInteractWithBlock(Lnet/minecraft/core/BlockPos;D)Z",
            at = @At("HEAD"), cancellable = true)
    private void anchorInteract(BlockPos pos, double distance, CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player) (Object) this;
        BlockPos anchor = WormholeAnchor.get(self);
        if (anchor == null) return;

        if (anchor.equals(pos)) {
            cir.setReturnValue(true);
        }
    }
}
