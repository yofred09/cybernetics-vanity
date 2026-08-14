package com.cyberneticsvanity.yofred.dev;

import net.minecraft.world.entity.player.Player;

/**
 * Server-safe vanity permission gate.
 * <p>
 * Must NOT reference client-only classes ({@code Minecraft}, {@code LocalPlayer}, …).
 * Dedicated servers load this from {@code UpdateVanityC2SPayload}; touching
 * {@link VanityState} there triggers DistCleaner and silently drops all sync.
 */
public final class ServerVanityGate {
    private ServerVanityGate() {}

    public static boolean canUse(Player player) {
        if (player == null) {
            return false;
        }
        int need = ServerVanityConfig.allowedPermissionLevel();
        if (need > 0 && !player.hasPermissions(need)) {
            return false;
        }
        if (!ServerVanityConfig.requireVanityImplant()) {
            return true;
        }
        return VanityImplantAccess.isInstalled(player);
    }
}
