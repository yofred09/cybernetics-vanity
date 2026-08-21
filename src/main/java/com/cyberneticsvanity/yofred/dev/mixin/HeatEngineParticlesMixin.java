package com.cyberneticsvanity.yofred.dev.mixin;

import com.cyberneticsvanity.yofred.dev.ServerVisualRules;
import com.cyberneticsvanity.yofred.dev.CyberneticsIds;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.item.cyberware.organs.HeatEngineItem;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels the Heat Engine's cosmetic server particles when Vanity hides it.
 *
 * <p>The injection targets the particle helper instead of {@code onTick}, so
 * the implant continues generating energy and only its appearance changes.</p>
 */
@Mixin(HeatEngineItem.class)
public abstract class HeatEngineParticlesMixin {
    @Inject(method = "spawnHeatEngineParticles", at = @At("HEAD"), cancellable = true)
    private static void cyberneticsvanity$hideParticles(ServerPlayer player, CallbackInfo ci) {
        if (ServerVisualRules.isInstalledItemHidden(
                player,
                CyberwareSlot.ORGANS,
                CyberneticsIds::isHeatEngine
        )) {
            ci.cancel();
        }
    }
}
