package com.cyberneticsvanity.yofred.dev.mixin.compat;

import com.cyberneticsvanity.yofred.dev.ClientSyncedServerRules;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Preserves the normal Corpse renderer when CPM appearance preservation is enabled. */
@Pseudo
@Mixin(targets = "de.maxhenkel.corpse.entities.CorpseRenderer", priority = 900)
public abstract class CorpseCpmAppearanceMixin {
    @Dynamic
    @Inject(method = "createcybernetics$applyCorpseVisualSnapshot", at = @At("HEAD"), cancellable = true, require = 0)
    private void cyberneticsvanity$preserveCpmCorpseSnapshot(
            Object entity, float entityYaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffers, int packedLight, CallbackInfo originalRenderCallback, CallbackInfo ci
    ) {
        if (shouldPreserve()) ci.cancel();
    }

    @Dynamic
    @Inject(method = "createcybernetics$renderSkeletonOverlays", at = @At("HEAD"), cancellable = true, require = 0)
    private void cyberneticsvanity$preserveCpmCorpseSkeleton(
            Object entity, float entityYaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffers, int packedLight, CallbackInfo originalRenderCallback, CallbackInfo ci
    ) {
        if (shouldPreserve()) ci.cancel();
    }

    private static boolean shouldPreserve() {
        return ClientSyncedServerRules.preserveCorpseAppearance();
    }
}
