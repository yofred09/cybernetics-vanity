package com.cyberneticsvanity.yofred.dev.mixin.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.perigrine3.createcybernetics.block.entity.HoloprojectorBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import virtuoel.pehkui.util.ScaleUtils;

/** Applies the projected player's Pehkui model scale to Create-Cybernetics' synthetic player. */
@Mixin(HoloprojectorBlockEntityRenderer.class)
public abstract class HoloprojectorPehkuiMixin {
    @Redirect(
            method = "renderPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            )
    )
    private void cyberneticsvanity$renderAtProjectedPlayerScale(
            EntityRenderDispatcher dispatcher, Entity hologram,
            double x, double y, double z, float yaw, float partialTick,
            PoseStack poseStack, MultiBufferSource buffers, int packedLight
    ) {
        Player source = Minecraft.getInstance().level == null
                ? null
                : Minecraft.getInstance().level.getPlayerByUUID(hologram.getUUID());
        if (source == null || source == hologram) {
            dispatcher.render(hologram, x, y, z, yaw, partialTick, poseStack, buffers, packedLight);
            return;
        }

        float width = ScaleUtils.getModelWidthScale(source, partialTick);
        float height = ScaleUtils.getModelHeightScale(source, partialTick);
        poseStack.pushPose();
        try {
            poseStack.scale(width, height, width);
            dispatcher.render(hologram, x, y, z, yaw, partialTick, poseStack, buffers, packedLight);
        } finally {
            poseStack.popPose();
        }
    }
}
