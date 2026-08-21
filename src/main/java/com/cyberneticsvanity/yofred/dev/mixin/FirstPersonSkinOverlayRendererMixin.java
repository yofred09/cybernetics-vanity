package com.cyberneticsvanity.yofred.dev.mixin;

import com.cyberneticsvanity.yofred.dev.VanityState;
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
        if (VanityState.shouldSuppressFirstPersonArmAlteration(player)) {
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
        // Only cancel the chrome belonging to a vanity-hidden arm.
        if (VanityState.shouldHideArm(player, event.getArm())) {
            ci.cancel();
        }
    }
}
