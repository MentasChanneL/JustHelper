package com.prikolz.justhelper.mixin;

import static com.prikolz.justhelper.events.BracketsHighlight.HIGHLIGHTED_BLOCKS;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    MinecraftClient client;

    @Inject(
        method = "shouldRenderBlockOutline",
        at = @At("HEAD"),
        cancellable = true
    )
    private void shouldRenderOutline(CallbackInfoReturnable<Boolean> ci) {
        HitResult hitResult = this.client.crosshairTarget;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
            if (HIGHLIGHTED_BLOCKS.contains(blockPos)) {
                ci.setReturnValue(false);
            }
        }
    }
}
