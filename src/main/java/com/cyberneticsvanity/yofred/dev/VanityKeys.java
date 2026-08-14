package com.cyberneticsvanity.yofred.dev;

import com.perigrine3.createcybernetics.api.CyberwareSlot;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Stable vanity keys for persistence + network sync.
 */
public final class VanityKeys {
    public static final String HIGHLIGHTS = "__meta:highlights";
    public static final String LIMB_HIDING = "__meta:limb_hiding";

    private VanityKeys() {}

    public static String implantKey(CyberwareSlot slot, int index, ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return slot.name() + ":" + index + ":" + id;
    }

    public static boolean isMeta(String key) {
        return key != null && key.startsWith("__meta:");
    }

    public static CyberwareSlot parseSlot(String key) {
        if (key == null || isMeta(key)) {
            return null;
        }
        int colon = key.indexOf(':');
        if (colon <= 0) {
            return null;
        }
        try {
            return CyberwareSlot.valueOf(key.substring(0, colon));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static int parseIndex(String key) {
        if (key == null || isMeta(key)) {
            return -1;
        }
        String[] parts = key.split(":", 3);
        if (parts.length < 2) {
            return -1;
        }
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public static ResourceLocation parseItemId(String key) {
        if (key == null || isMeta(key)) {
            return null;
        }
        String[] parts = key.split(":", 3);
        if (parts.length < 3) {
            return null;
        }
        return ResourceLocation.tryParse(parts[2]);
    }
}
