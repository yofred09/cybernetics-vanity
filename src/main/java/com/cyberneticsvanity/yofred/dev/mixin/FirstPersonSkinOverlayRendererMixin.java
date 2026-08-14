package com.cyberneticsvanity.yofred.dev.mixin;

import com.cyberneticsvanity.yofred.dev.VanityState;
import com.cyberneticsvanity.yofred.dev.compat.CpmCompat;
import com.perigrine3.createcybernetics.client.skin.SkinLayerRender;
import net.minecraft.client.player.AbstractClientPlayer;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkinLayerRender.FirstPersonSkinOverlayRenderer.class)
public abstract class FirstPersonSkinOverlayRendererMixin {
    @Inject(method = "onRenderArmCancel", at = @At("HEAD"), cancellable = true)
    private static void cyberneticsvanity$skipCancelWhenHidden(RenderArmEvent event, CallbackInfo ci) {
        if (event == null) {
            return;
        }
        AbstractClientPlayer player = event.getPlayer();
        if (player == null) {
            return;
        }
        // CPM: do not cancel vanilla FP arm rendering (limb geometry fight).
        // Overlay hide still applies below via shouldHideArm when vanity HIDE is set.
        if (CpmCompat.shouldSkipLimbGeometryEdits()
                || VanityState.shouldSuppressFirstPersonArmAlteration(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "onRenderArm", at = @At("HEAD"), cancellable = true)
    private static void cyberneticsvanity$skipOverlayWhenHidden(RenderArmEvent event, CallbackInfo ci) {
        if (event == null) {
            return;
        }
        AbstractClientPlayer player = event.getPlayer();
        if (player == null) {
            return;
        }
        // Only cancel FP chrome when that arm's implant is vanity-hidden (SHOW still renders with CPM).
        if (VanityState.shouldHideArm(player, event.getArm())) {
            ci.cancel();
        }
    }
}
