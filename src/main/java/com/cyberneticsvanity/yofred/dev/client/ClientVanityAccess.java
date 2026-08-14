package com.cyberneticsvanity.yofred.dev.client;

import com.cyberneticsvanity.yofred.dev.VanityImplantAccess;
import net.minecraft.client.Minecraft;

/** Client-only vanity implant helpers. */
public final class ClientVanityAccess {
    private ClientVanityAccess() {}

    public static boolean isInstalledOnLocalPlayer() {
        Minecraft mc = Minecraft.getInstance();
        return VanityImplantAccess.isInstalled(mc.player);
    }
}
