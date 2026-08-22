package com.cyberneticsvanity.yofred.dev.mixin;

import com.cyberneticsvanity.yofred.dev.CyberneticsIds;
import com.cyberneticsvanity.yofred.dev.VanityState;
import com.perigrine3.createcybernetics.client.skin.SkinModifier;
import com.perigrine3.createcybernetics.client.skin.SkinModifierManager;
import com.perigrine3.createcybernetics.client.skin.SkinModifierState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.EnumSet;
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
            if (!VanityState.shouldHideModifier(player, modifier)
                    && !isMetalPlatingSuppressedByVanity(modifier)) {
                kept.add(withVanitySkinUnderlay(modifier));
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

    /**
     * Cybernetics omits this base layer when its exact Synthskin item is installed.
     * Vanity is Synthskin-compatible, but that upstream check compares item identity,
     * so reproduce the intended result here without suppressing Netherite Plating.
     */
    private static boolean isMetalPlatingSuppressedByVanity(SkinModifier modifier) {
        if (modifier == null) {
            return false;
        }
        return CyberneticsIds.isBaseMetalPlatingTexture(modifier.getTexture(PlayerSkin.Model.WIDE))
                || CyberneticsIds.isBaseMetalPlatingTexture(modifier.getTexture(PlayerSkin.Model.SLIM));
    }

    /**
     * Netherite Plating hides the vanilla body and draws a partially transparent
     * texture over it. Cybernetics normally requests the player-skin underlay only
     * for its exact Synthskin item, so a Synthskin-compatible Vanity Implant can
     * otherwise leave transparent facial pixels (especially the eyes) empty.
     */
    private static SkinModifier withVanitySkinUnderlay(SkinModifier modifier) {
        if (modifier == null || modifier.needsPlayerSkinUnderlay()) {
            return modifier;
        }

        var wide = modifier.getTexture(PlayerSkin.Model.WIDE);
        var slim = modifier.getTexture(PlayerSkin.Model.SLIM);
        if (!CyberneticsIds.isNetheriteSkinTexture(wide)
                && !CyberneticsIds.isNetheriteSkinTexture(slim)) {
            return modifier;
        }

        EnumSet<HumanoidArm> replacedArms = EnumSet.noneOf(HumanoidArm.class);
        for (HumanoidArm arm : HumanoidArm.values()) {
            if (modifier.replacesVanillaArm(arm)) {
                replacedArms.add(arm);
            }
        }

        // Copy every original rendering property and change only the underlay flag.
        return new SkinModifier(
                wide,
                slim,
                modifier.getColor(),
                modifier.hasGlint(),
                modifier.shouldHideVanillaLayers(),
                EnumSet.copyOf(modifier.getHideMask()),
                EnumSet.copyOf(modifier.getOverlayParts()),
                replacedArms,
                true
        );
    }
}
