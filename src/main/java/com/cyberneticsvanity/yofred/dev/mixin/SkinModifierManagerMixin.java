package com.cyberneticsvanity.yofred.dev.mixin;

import com.cyberneticsvanity.yofred.dev.VanityState;
import com.perigrine3.createcybernetics.client.skin.SkinModifier;
import com.perigrine3.createcybernetics.client.skin.SkinModifierManager;
import com.perigrine3.createcybernetics.client.skin.SkinModifierState;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(SkinModifierManager.class)
public abstract class SkinModifierManagerMixin {
    @Inject(method = "getPlayerSkinState", at = @At("RETURN"))
    private static void cyberneticsvanity$filterHiddenImplants(
            AbstractClientPlayer player,
            CallbackInfoReturnable<SkinModifierState> cir
    ) {
        SkinModifierState state = cir.getReturnValue();
        if (state == null || player == null) {
            return;
        }

        if (!VanityState.isVanityActive(player)) {
            return;
        }

        List<SkinModifier> kept = new ArrayList<>();
        for (SkinModifier modifier : state.getModifiers()) {
            if (!VanityState.shouldHideModifier(player, modifier)) {
                kept.add(modifier);
            }
        }
        state.clearModifiers();
        for (SkinModifier modifier : kept) {
            state.addModifier(modifier);
        }

        if (VanityState.shouldHideHighlights(player)) {
            state.clearHighlights();
        }
    }
}
