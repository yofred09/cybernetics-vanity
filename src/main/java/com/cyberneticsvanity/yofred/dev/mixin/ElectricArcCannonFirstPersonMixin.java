package com.cyberneticsvanity.yofred.dev.mixin;

import com.cyberneticsvanity.yofred.dev.InstalledVisualImplants;
import com.cyberneticsvanity.yofred.dev.VanityState;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.item.cyberware.arm.ElectricArcCannonItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses first-person Arc Cannon prongs when that implant is vanity-hidden.
 */
@Mixin(ElectricArcCannonItem.ClientFirstPerson.class)
public abstract class ElectricArcCannonFirstPersonMixin {
    @Inject(method = "onRenderArm", at = @At("HEAD"), cancellable = true)
    private static void cyberneticsvanity$skipHiddenProngs(RenderArmEvent event, CallbackInfo ci) {
        if (event == null || event.getPlayer() == null) {
            return;
        }
        if (!VanityState.isVanityActive(event.getPlayer())) {
            return;
        }
        CyberwareSlot slot = event.getArm() == HumanoidArm.LEFT ? CyberwareSlot.LARM : CyberwareSlot.RARM;
        for (InstalledVisualImplants.Entry entry : InstalledVisualImplants.list(event.getPlayer())) {
            if (entry.slot() != slot || !VanityState.isEntryHidden(event.getPlayer(), entry)) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(entry.stack().getItem());
            String path = id.getPath().toLowerCase();
            if (path.contains("arccannon") || path.contains("arc_cannon")) {
                ci.cancel();
                return;
            }
        }
    }
}
