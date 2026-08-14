package com.cyberneticsvanity.yofred.dev.mixin.compat;

import com.cyberneticsvanity.yofred.dev.VanityState;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Soft Cyber Spells compat: suppress first-person rune arm overlay when hidden.
 */
@Mixin(targets = "cyberspells.client.RuneFirstPersonRenderer", remap = false)
public abstract class RuneFirstPersonRendererMixin {
    @Inject(method = "onRenderArm", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cyberneticsvanity$hideRuneFp(RenderArmEvent event, CallbackInfo ci) {
        if (event == null || event.getPlayer() == null) {
            return;
        }
        CyberwareSlot slot = event.getArm() == HumanoidArm.LEFT ? CyberwareSlot.LARM : CyberwareSlot.RARM;
        if (VanityState.isSlotIndexHidden(event.getPlayer(), slot, 0)) {
            ci.cancel();
        }
    }
}
