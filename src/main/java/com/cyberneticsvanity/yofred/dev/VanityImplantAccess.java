package com.cyberneticsvanity.yofred.dev;

import com.cyberneticsvanity.yofred.dev.item.ModItems;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import net.minecraft.world.entity.player.Player;

/**
 * Shared implant presence checks — safe on dedicated server and client.
 * Local-player helpers live in {@link com.cyberneticsvanity.yofred.dev.client.ClientVanityAccess}.
 */
public final class VanityImplantAccess {
    private VanityImplantAccess() {}

    public static boolean isInstalled(Player player) {
        if (player == null) {
            return false;
        }
        PlayerCyberwareData data = PlayerCyberwareData.getForVisual(player, player.registryAccess());
        if (data == null) {
            return false;
        }
        return data.hasSpecificItem(ModItems.VANITY_IMPLANT.get(), CyberwareSlot.SKIN);
    }
}
