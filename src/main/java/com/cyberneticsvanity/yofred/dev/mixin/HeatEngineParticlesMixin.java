package com.cyberneticsvanity.yofred.dev.mixin;

import com.cyberneticsvanity.yofred.dev.ServerVisualRules;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.item.cyberware.organs.HeatEngineItem;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides only the Heat Engine's particles; energy generation remains untouched. */
@Mixin(HeatEngineItem.class)
public abstract class HeatEngineParticlesMixin {
    @Inject(method = "spawnHeatEngineParticles", at = @At("HEAD"), cancellable = true)
    private static void cyberneticsvanity$hideParticles(ServerPlayer player, CallbackInfo ci) {
        if (ServerVisualRules.isInstalledItemHidden(
                player,
                CyberwareSlot.ORGANS,
                id -> id != null && id.getNamespace().equals("createcybernetics")
                        && id.getPath().endsWith("heatengine")
        )) {
            ci.cancel();
        }
    }
}
