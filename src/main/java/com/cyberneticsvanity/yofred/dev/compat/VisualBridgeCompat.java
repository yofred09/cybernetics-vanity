package com.cyberneticsvanity.yofred.dev.compat;

import net.neoforged.fml.ModList;

/** Optional integration marker; CPM rendering itself belongs to CPM Visual Bridge. */
public final class VisualBridgeCompat {
    public static final String MOD_ID = "cpmvisualbridge";

    private VisualBridgeCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }
}
