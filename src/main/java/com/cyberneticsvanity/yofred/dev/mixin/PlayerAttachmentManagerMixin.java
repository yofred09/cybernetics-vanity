package com.cyberneticsvanity.yofred.dev.mixin;

import com.cyberneticsvanity.yofred.dev.VanityState;
import com.perigrine3.createcybernetics.client.model.PlayerAttachment;
import com.perigrine3.createcybernetics.client.model.PlayerAttachmentManager;
import com.perigrine3.createcybernetics.client.model.PlayerAttachmentState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Filters 3D attachments (Arc Cannon prongs, claws, drills, …) when the owning implant is vanity-hidden.
 */
@Mixin(PlayerAttachmentManager.class)
public abstract class PlayerAttachmentManagerMixin {
    @Inject(method = "getState", at = @At("RETURN"))
    private static void cyberneticsvanity$filterHiddenAttachments(
            AbstractClientPlayer player,
            CallbackInfoReturnable<PlayerAttachmentState> cir
    ) {
        PlayerAttachmentState state = cir.getReturnValue();
        if (state == null || player == null) {
            return;
        }
        List<PlayerAttachment> all = state.all();
        if (all == null || all.isEmpty()) {
            return;
        }
        if (!VanityState.isVanityActive(player)) {
            return;
        }
        List<PlayerAttachment> kept = new ArrayList<>(all.size());
        for (PlayerAttachment attachment : all) {
            if (attachment == null) {
                continue;
            }
            if (VanityState.shouldHideAttachment(
                    player,
                    attachment.anchor(),
                    attachment.texture(PlayerSkin.Model.WIDE)
            )) {
                continue;
            }
            kept.add(attachment);
        }
        if (kept.size() == all.size()) {
            return;
        }
        state.clear();
        for (PlayerAttachment attachment : kept) {
            state.add(attachment);
        }
    }
}
