package com.cyberneticsvanity.yofred.dev.mixin;

import com.cyberneticsvanity.yofred.dev.VanityState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.perigrine3.createcybernetics.client.skin.SkinModifier;
import com.perigrine3.createcybernetics.client.skin.SkinOverlayModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * First-person arm overlay path (always local player).
 * Third-person filtering is handled in {@link SkinModifierManagerMixin}.
 */
@Mixin(SkinOverlayModelRenderer.class)
public abstract class SkinOverlayModelRendererMixin {
    @Inject(method = "renderFirstPersonArmModifier", at = @At("HEAD"), cancellable = true)
    private static void cyberneticsvanity$skipHiddenFpModifier(
            PlayerModel model,
            SkinModifier modifier,
            boolean rightArm,
            PoseStack poseStack,
            VertexConsumer vc,
            int light,
            int overlay,
            int color,
            CallbackInfo ci
    ) {
        if (model == null || modifier == null) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        HumanoidArm arm = rightArm ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        if (VanityState.shouldHideArm(player, arm) || VanityState.shouldHideModifier(player, modifier)) {
            ci.cancel();
        }
    }
}
