package com.cyberneticsvanity.yofred.dev.mixin;

import com.cyberneticsvanity.yofred.dev.VanityState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.perigrine3.createcybernetics.client.render.FirstPersonOverlayArmRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonOverlayArmRenderer.class)
public abstract class FirstPersonOverlayArmRendererMixin {
    @Inject(method = "renderOverlayArmAndSleeve", at = @At("HEAD"), cancellable = true)
    private static void cyberneticsvanity$skipWhenArmsHidden(
            AbstractClientPlayer player,
            HumanoidArm arm,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            ResourceLocation overlayTexture,
            CallbackInfo ci
    ) {
        if (player == null || arm == null) {
            return;
        }
        if (VanityState.shouldHideArm(player, arm)) {
            ci.cancel();
        }
    }
}
