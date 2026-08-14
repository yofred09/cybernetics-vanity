package com.cyberneticsvanity.yofred.dev.mixin;

import com.cyberneticsvanity.yofred.dev.ClientVanityConfig;
import com.cyberneticsvanity.yofred.dev.compat.CpmCompat;
import com.cyberneticsvanity.yofred.dev.compat.CpmSoftBridge;
import com.cyberneticsvanity.yofred.dev.compat.PehkuiScaleBridge;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.perigrine3.createcybernetics.client.render.SandevistanMirageTrail;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Deque;

/** Preserves the player's CPM model and Pehkui scale in Sandevistan mirages. */
@Mixin(SandevistanMirageTrail.class)
public abstract class SandevistanMirageTrailCpmMixin {
    private static final ThreadLocal<Integer> cyberneticsvanity$mirageIndex =
            ThreadLocal.withInitial(() -> 0);

    @Inject(method = "renderTrailForPlayer", at = @At("HEAD"))
    private static void cyberneticsvanity$beginAdaptiveTrail(
            PoseStack trailPose, MultiBufferSource buffers, PlayerRenderer renderer,
            AbstractClientPlayer player, Deque<?> snapshots, Vec3 cameraPosition,
            float partialTick, CallbackInfo ci
    ) {
        cyberneticsvanity$mirageIndex.set(0);
    }

    @Redirect(
            method = "renderTrailForPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/PlayerModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
                    ordinal = 0
            ),
            require = 1
    )
    private static void cyberneticsvanity$renderCpmMirage(
            PlayerModel<?> model,
            PoseStack renderPose,
            VertexConsumer consumer,
            int packedLight,
            int packedOverlay,
            int color,
            PoseStack trailPose,
            MultiBufferSource buffers,
            PlayerRenderer renderer,
            AbstractClientPlayer player,
            Deque<?> snapshots,
            Vec3 cameraPosition,
            float partialTick
    ) {
        int index = cyberneticsvanity$mirageIndex.get();
        cyberneticsvanity$mirageIndex.set(index + 1);
        boolean cpmEnabled = ClientVanityConfig.sandevistanCpmModels() && CpmCompat.isLoaded();
        // A short trail keeps every CPM copy. Longer trails progressively use
        // fewer full custom-model renders; at the maximum normal lifetime this
        // caps the expensive CPM work at roughly 27 copies per player/frame.
        int stride = snapshots.size() > 48 ? 3 : snapshots.size() > 24 ? 2 : 1;
        if (cpmEnabled && index % stride != 0) return;
        cyberneticsvanity$renderScaled(model, renderPose, consumer, packedLight, packedOverlay, color,
                buffers, player, partialTick, cpmEnabled);
    }

    @Redirect(
            method = "renderTrailForPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/PlayerModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
                    ordinal = 1
            ),
            require = 1
    )
    private static void cyberneticsvanity$renderScaledMirageOverlay(
            PlayerModel<?> model, PoseStack renderPose, VertexConsumer consumer,
            int packedLight, int packedOverlay, int color, PoseStack trailPose,
            MultiBufferSource buffers, PlayerRenderer renderer, AbstractClientPlayer player,
            Deque<?> snapshots, Vec3 cameraPosition, float partialTick
    ) {
        // CPM already draws its own textured cubes in the base pass. Repeating
        // them for Cybernetics' vanilla skin overlay causes the flashing seen
        // on custom models.
        if (ClientVanityConfig.sandevistanCpmModels() && CpmCompat.isLoaded()) return;
        cyberneticsvanity$renderScaled(model, renderPose, consumer, packedLight, packedOverlay, color,
                buffers, player, partialTick, false);
    }

    private static void cyberneticsvanity$renderScaled(
            PlayerModel<?> model, PoseStack renderPose, VertexConsumer consumer,
            int packedLight, int packedOverlay, int color, MultiBufferSource buffers,
            AbstractClientPlayer player, float partialTick, boolean useCpm
    ) {
        float widthScale = PehkuiScaleBridge.modelWidth(player, partialTick);
        float heightScale = PehkuiScaleBridge.modelHeight(player, partialTick);
        renderPose.pushPose();
        try {
            renderPose.scale(widthScale, heightScale, widthScale);
            if (useCpm && CpmCompat.isLoaded() && CpmSoftBridge.renderPlayerModelSafe(
                    player,
                    model,
                    renderPose,
                    consumer,
                    buffers,
                    packedLight,
                    packedOverlay,
                    color
            )) {
                return;
            }
            model.renderToBuffer(renderPose, consumer, packedLight, packedOverlay, color);
        } finally {
            renderPose.popPose();
        }
    }
}
