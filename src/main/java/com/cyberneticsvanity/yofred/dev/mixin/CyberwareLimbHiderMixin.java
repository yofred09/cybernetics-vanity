package com.cyberneticsvanity.yofred.dev.mixin;

import com.cyberneticsvanity.yofred.dev.VanityState;
import com.cyberneticsvanity.yofred.dev.ClientSyncedServerRules;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.client.render.CyberwareLimbHider;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CyberwareLimbHider.class)
public abstract class CyberwareLimbHiderMixin {
    @ModifyVariable(method = "onRenderLivingPre", at = @At("STORE"), index = 5)
    private static boolean cyberneticsvanity$keepLeftArmWhenMutilationDisabled(boolean visible) {
        return visible || !ClientSyncedServerRules.hideMissingLimbs();
    }

    @ModifyVariable(method = "onRenderLivingPre", at = @At("STORE"), index = 6)
    private static boolean cyberneticsvanity$keepRightArmWhenMutilationDisabled(boolean visible) {
        return visible || !ClientSyncedServerRules.hideMissingLimbs();
    }

    @ModifyVariable(method = "onRenderLivingPre", at = @At("STORE"), index = 7)
    private static boolean cyberneticsvanity$keepLeftLegWhenMutilationDisabled(boolean visible) {
        return visible || !ClientSyncedServerRules.hideMissingLimbs();
    }

    @ModifyVariable(method = "onRenderLivingPre", at = @At("STORE"), index = 8)
    private static boolean cyberneticsvanity$keepRightLegWhenMutilationDisabled(boolean visible) {
        return visible || !ClientSyncedServerRules.hideMissingLimbs();
    }

    @Inject(method = "onRenderLivingPre", at = @At("HEAD"), cancellable = true)
    private static void cyberneticsvanity$skipWhenHidden(RenderLivingEvent.Pre event, CallbackInfo ci) {
        if (event == null) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }
        if (VanityState.shouldSkipLimbHiding(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "shouldRenderLeftArm", at = @At("RETURN"), cancellable = true)
    private static void cyberneticsvanity$forceVanillaLeft(
            AbstractClientPlayer player,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!ClientSyncedServerRules.hideMissingLimbs() && !Boolean.TRUE.equals(cir.getReturnValue())) {
            cir.setReturnValue(true);
            return;
        }
        if (Boolean.TRUE.equals(cir.getReturnValue())
                && VanityState.shouldForceVanillaLimb(player, CyberwareSlot.LARM)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldRenderRightArm", at = @At("RETURN"), cancellable = true)
    private static void cyberneticsvanity$forceVanillaRight(
            AbstractClientPlayer player,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!ClientSyncedServerRules.hideMissingLimbs() && !Boolean.TRUE.equals(cir.getReturnValue())) {
            cir.setReturnValue(true);
            return;
        }
        if (Boolean.TRUE.equals(cir.getReturnValue())
                && VanityState.shouldForceVanillaLimb(player, CyberwareSlot.RARM)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldRenderLeftLeg", at = @At("RETURN"), cancellable = true)
    private static void cyberneticsvanity$forceVanillaLeftLeg(
            AbstractClientPlayer player,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!ClientSyncedServerRules.hideMissingLimbs() && !Boolean.TRUE.equals(cir.getReturnValue())) {
            cir.setReturnValue(true);
            return;
        }
        if (Boolean.TRUE.equals(cir.getReturnValue())
                && VanityState.shouldForceVanillaLimb(player, CyberwareSlot.LLEG)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldRenderRightLeg", at = @At("RETURN"), cancellable = true)
    private static void cyberneticsvanity$forceVanillaRightLeg(
            AbstractClientPlayer player,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!ClientSyncedServerRules.hideMissingLimbs() && !Boolean.TRUE.equals(cir.getReturnValue())) {
            cir.setReturnValue(true);
            return;
        }
        if (Boolean.TRUE.equals(cir.getReturnValue())
                && VanityState.shouldForceVanillaLimb(player, CyberwareSlot.RLEG)) {
            cir.setReturnValue(false);
        }
    }
}
