package com.cyberneticsvanity.yofred.dev.mixin.compat;

import com.cyberneticsvanity.yofred.dev.VanityState;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Soft Cyber Spells compat: skip rune limb draw when that slot index is vanity-hidden.
 * Loaded only via optional mixin config when {@code cyberspells} is present.
 */
@Mixin(targets = "cyberspells.client.RuneSkinLayer", remap = false)
public abstract class RuneSkinLayerMixin {
    @Inject(method = "renderLimb", at = @At("HEAD"), cancellable = true, remap = false)
    private void cyberneticsvanity$hideRuneLimb(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            PlayerCyberwareData data,
            CyberwareSlot slot,
            String side,
            CallbackInfo ci
    ) {
        if (player != null && VanityState.isSlotIndexHidden(player, slot, 0)) {
            ci.cancel();
        }
    }
}
