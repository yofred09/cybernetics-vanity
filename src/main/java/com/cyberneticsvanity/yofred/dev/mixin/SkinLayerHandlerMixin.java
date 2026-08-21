package com.cyberneticsvanity.yofred.dev.mixin;

import com.cyberneticsvanity.yofred.dev.VanityState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.perigrine3.createcybernetics.client.skin.SkinLayerHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkinLayerHandler.class)
public abstract class SkinLayerHandlerMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cyberneticsvanity$skipWhenFullyHidden(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (player == null) {
            return;
        }
        if (VanityState.shouldSkipAllOverlays(player)) {
            ci.cancel();
        }
    }
}
