package com.cyberneticsvanity.yofred.dev;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

/**
 * Names and path checks owned by Create Cybernetics.
 *
 * <p>Keeping them here avoids scattering string literals through render and
 * server code. It also gives us one place to update if Cybernetics renames an
 * item or texture in a future release.</p>
 */
public final class CyberneticsIds {
    private static final String NAMESPACE = "createcybernetics";

    private CyberneticsIds() {}

    public static boolean isHeatEngine(ResourceLocation itemId) {
        return hasPath(itemId, "organsupgrades_heatengine")
                || hasPath(itemId, "scavenged_heatengine");
    }

    public static boolean isSculkHeart(ResourceLocation itemId) {
        return hasPath(itemId, "wetware_sculkheart");
    }

    public static boolean isSculkAppearance(ResourceLocation textureId) {
        if (!isFromCybernetics(textureId)) {
            return false;
        }
        String path = textureId.getPath().toLowerCase(Locale.ROOT);
        return path.contains("/sculk_") || path.endsWith("/sculked.png");
    }

    public static boolean isNetheriteSkinTexture(ResourceLocation textureId) {
        if (!isFromCybernetics(textureId)) {
            return false;
        }
        String path = textureId.getPath();
        return path.endsWith("/isothermal_skin_wide.png")
                || path.endsWith("/isothermal_skin_slim.png");
    }

    public static boolean isBaseMetalPlatingTexture(ResourceLocation textureId) {
        if (!isFromCybernetics(textureId)) {
            return false;
        }
        String path = textureId.getPath();
        return path.endsWith("/metal_plating_wide.png")
                || path.endsWith("/metal_plating_slim.png")
                || path.endsWith("/metal_plating_wide_dyed.png")
                || path.endsWith("/metal_plating_slim_dyed.png");
    }

    private static boolean hasPath(ResourceLocation id, String expectedPath) {
        return isFromCybernetics(id) && id.getPath().equals(expectedPath);
    }

    private static boolean isFromCybernetics(ResourceLocation id) {
        return id != null && id.getNamespace().equals(NAMESPACE);
    }
}
