package com.cyberneticsvanity.yofred.dev.mixin;

import com.cyberneticsvanity.yofred.dev.VanityState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.perigrine3.createcybernetics.compat.playeranimator.PlayerAnimatorFirstPersonOverlayCompat;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerAnimatorFirstPersonOverlayCompat.class)
public abstract class PlayerAnimatorFirstPersonOverlayCompatMixin {
    @Inject(method = "renderVanillaFirstPersonArmOverlays", at = @At("HEAD"), cancellable = true)
    private static void cyberneticsvanity$skipFpArmOverlays(
            AbstractClientPlayer player,
            HumanoidArm arm,
            PlayerModel model,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        if (player == null || arm == null) {
            return;
        }
        if (VanityState.shouldHideArm(player, arm)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFirstPersonPlayerOverlays", at = @At("HEAD"), cancellable = true)
    private static void cyberneticsvanity$skipFpPlayerOverlays(
            AbstractClientPlayer player,
            PlayerModel model,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
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
