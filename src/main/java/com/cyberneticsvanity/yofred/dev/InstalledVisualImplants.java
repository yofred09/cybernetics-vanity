package com.cyberneticsvanity.yofred.dev;

import com.cyberneticsvanity.yofred.dev.item.ModItems;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.client.skin.SkinModifier;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

/**
 * Discovers installed cyberware that can contribute visuals, and matches
 * SkinModifiers / overlay parts / attachments back to those implant keys.
 * <p>
 * Matching is slot-first (overlay parts / limb family), then strict item↔texture
 * stems. Bare tokens like {@code left}/{@code right} are never used — they
 * incorrectly map {@code basecyberware_rightarm} onto {@code right_cyberleg}.
 */
public final class InstalledVisualImplants {
    public record Entry(String key, CyberwareSlot slot, int index, ItemStack stack) {}

    private enum LimbFamily {
        LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG, HEAD, BODY, UNKNOWN
    }

    private InstalledVisualImplants() {}

    public static List<Entry> list(Player player) {
        List<Entry> out = new ArrayList<>();
        if (player == null) {
            return out;
        }
        PlayerCyberwareData data = PlayerCyberwareData.getForVisual(player, player.registryAccess());
        if (data == null) {
            return out;
        }
        for (CyberwareSlot slot : CyberwareSlot.values()) {
            for (int i = 0; i < slot.size; i++) {
                InstalledCyberware installed = data.get(slot, i);
                if (installed == null) {
                    continue;
                }
                ItemStack stack = installed.getItem();
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                if (stack.is(ModItems.VANITY_IMPLANT.get())) {
                    continue;
                }
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (id.getPath().startsWith("bodypart_")) {
                    continue;
                }
                out.add(new Entry(VanityKeys.implantKey(slot, i, stack), slot, i, stack.copy()));
            }
        }
        return out;
    }

    /**
     * True when this SkinModifier belongs to the given implant row.
     * Requires (1) overlay/limb slot compatibility and (2) strict texture↔item match.
     */
    public static boolean matchesModifier(SkinModifier modifier, Entry entry) {
        if (modifier == null || entry == null) {
            return false;
        }
        if (!modifierFitsSlot(modifier, entry.slot())) {
            return false;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(entry.stack().getItem());
        ResourceLocation wide = modifier.getTexture(PlayerSkin.Model.WIDE);
        ResourceLocation slim = modifier.getTexture(PlayerSkin.Model.SLIM);
        return textureMatchesItem(wide, itemId) || textureMatchesItem(slim, itemId);
    }

    /** Overlay-part region must belong to the implant's cyberware slot. */
    public static boolean matchesOverlayPart(SkinModifier.OverlayPart part, Entry entry) {
        if (part == null || entry == null) {
            return false;
        }
        return switch (entry.slot()) {
            case EYES, BRAIN -> part == SkinModifier.OverlayPart.HEAD || part == SkinModifier.OverlayPart.HAT;
            case LARM -> part == SkinModifier.OverlayPart.LEFT_ARM || part == SkinModifier.OverlayPart.LEFT_SLEEVE;
            case RARM -> part == SkinModifier.OverlayPart.RIGHT_ARM || part == SkinModifier.OverlayPart.RIGHT_SLEEVE;
            case LLEG -> part == SkinModifier.OverlayPart.LEFT_LEG || part == SkinModifier.OverlayPart.LEFT_PANTS;
            case RLEG -> part == SkinModifier.OverlayPart.RIGHT_LEG || part == SkinModifier.OverlayPart.RIGHT_PANTS;
            case SKIN, MUSCLE, BONE, HEART, LUNGS, ORGANS ->
                    part == SkinModifier.OverlayPart.BODY || part == SkinModifier.OverlayPart.JACKET;
        };
    }

    public static boolean matchesAttachmentTexture(ResourceLocation texture, Entry entry) {
        if (texture == null || entry == null) {
            return false;
        }
        if (!textureFitsSlot(texture, entry.slot())) {
            return false;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(entry.stack().getItem());
        return textureMatchesItem(texture, itemId);
    }

    public static boolean isLimbReplacingItem(ResourceLocation itemId) {
        if (itemId == null) {
            return false;
        }
        String path = itemId.getPath().toLowerCase(Locale.ROOT);
        return path.contains("basecyberware_")
                || path.contains("arccannon")
                || path.contains("arc_cannon")
                || path.contains("cyberarm")
                || path.contains("cyberleg")
                || path.contains("rune_arm")
                || path.contains("rune_leg")
                || (path.contains("rune") && (path.contains("arm") || path.contains("leg")));
    }

    public static boolean isSculkHeart(ResourceLocation itemId) {
        return itemId != null
                && itemId.getNamespace().equals("createcybernetics")
                && itemId.getPath().equals("wetware_sculkheart");
    }

    public static boolean isSculkAppearance(ResourceLocation texture) {
        if (texture == null || !texture.getNamespace().equals("createcybernetics")) {
            return false;
        }
        String path = texture.getPath().toLowerCase(Locale.ROOT);
        return path.contains("/sculk_") || path.endsWith("/sculked.png");
    }

    /**
     * Specific visual stems for an item. Never emits bare {@code left}/{@code right}.
     */
    public static List<String> visualTokens(ResourceLocation itemId) {
        List<String> tokens = new ArrayList<>();
        if (itemId == null) {
            return tokens;
        }
        String path = itemId.getPath().toLowerCase(Locale.ROOT);
        tokens.add(path);

        String stripped = path
                .replace("armupgrades_", "")
                .replace("legupgrades_", "")
                .replace("skinupgrades_", "")
                .replace("bodymods_", "")
                .replace("basecyberware_", "")
                .replace("scavenged_", "")
                .replace("chipware_", "")
                .replace("wetware_", "");
        if (!stripped.isEmpty() && !stripped.equals(path)) {
            tokens.add(stripped);
        }

        // Limb baseware: map item ids → Create-Cybernetics entity texture stems.
        if (path.contains("leftarm") || path.contains("left_arm")) {
            tokens.add("leftarm");
            tokens.add("left_cyberarm");
            tokens.add("left_arm");
        } else if (path.contains("rightarm") || path.contains("right_arm")) {
            tokens.add("rightarm");
            tokens.add("right_cyberarm");
            tokens.add("right_arm");
        } else if (path.contains("leftleg") || path.contains("left_leg")) {
            tokens.add("leftleg");
            tokens.add("left_cyberleg");
            tokens.add("left_leg");
        } else if (path.contains("rightleg") || path.contains("right_leg")) {
            tokens.add("rightleg");
            tokens.add("right_cyberleg");
            tokens.add("right_leg");
        }

        if (path.contains("arccannon") || stripped.contains("arccannon") || path.contains("arc_cannon")) {
            tokens.add("arc_cannon");
            tokens.add("arccannon");
            tokens.add("armcannon");
        }
        if (path.contains("firestarter") || stripped.contains("firestarter")) {
            tokens.add("firestarter");
        }
        if (path.contains("flywheel") || stripped.contains("flywheel")) {
            tokens.add("flywheel");
        }
        if (path.contains("crafthands") || stripped.contains("crafthands") || path.contains("crafting_hands")) {
            tokens.add("crafthands");
            tokens.add("craft_hands");
            tokens.add("knuckles");
        }
        if (path.contains("cybereye")) {
            tokens.add("cybereye");
            tokens.add("cybereyes");
        }
        if (path.contains("metalplating") || path.contains("metal_plating")) {
            tokens.add("metal_plating");
            tokens.add("metalplating");
        }
        if (path.contains("synthskin")) {
            tokens.add("synth");
        }
        if (path.contains("netherplated") || path.contains("nether_plated")) {
            tokens.add("netherplated");
            tokens.add("nether_plated");
        }
        if (path.contains("dragonskin")) {
            tokens.add("dragonskin");
        }
        if (itemId.getNamespace().equals("cyberspells") || path.contains("rune_")) {
            tokens.add("rune");
        }
        return tokens;
    }

    /**
     * Strict item↔texture match: stem must appear in the texture file name,
     * and limb families must not conflict (arm vs leg, left vs right).
     */
    public static boolean textureMatchesItem(ResourceLocation texture, ResourceLocation itemId) {
        if (texture == null || itemId == null) {
            return false;
        }
        String texPath = texture.getPath().toLowerCase(Locale.ROOT);
        String file = texPath;
        int slash = texPath.lastIndexOf('/');
        if (slash >= 0 && slash + 1 < texPath.length()) {
            file = texPath.substring(slash + 1);
        }
        String compactFile = file.replace("_", "").replace("-", "");
        String itemPath = itemId.getPath().toLowerCase(Locale.ROOT);

        LimbFamily itemLimb = limbFamilyFromPath(itemPath);
        LimbFamily texLimb = limbFamilyFromPath(file);
        if (itemLimb != LimbFamily.UNKNOWN && texLimb != LimbFamily.UNKNOWN && itemLimb != texLimb) {
            return false;
        }

        boolean matched = false;
        for (String token : visualTokens(itemId)) {
            if (token == null || token.isBlank()) {
                continue;
            }
            String t = token.toLowerCase(Locale.ROOT);
            // Reject ultra-short / ambiguous tokens
            if (t.length() < 4 || t.equals("left") || t.equals("right") || t.equals("arm") || t.equals("leg")) {
                continue;
            }
            if (file.contains(t) || texPath.contains(t)) {
                matched = true;
                break;
            }
            String compactToken = t.replace("_", "").replace("-", "");
            if (compactToken.length() >= 4 && compactFile.contains(compactToken)) {
                matched = true;
                break;
            }
        }

        if (!matched && itemPath.contains("tattoo") && (file.contains("tattoo") || texPath.contains("tattoo"))) {
            matched = true;
        }
        return matched;
    }

    /** Whether a SkinModifier's overlay mask belongs to this cyberware slot. */
    public static boolean modifierFitsSlot(SkinModifier modifier, CyberwareSlot slot) {
        if (modifier == null || slot == null) {
            return false;
        }
        EnumSet<SkinModifier.OverlayPart> parts = modifier.getOverlayParts();
        if (parts == null || parts.isEmpty()) {
            // Fall back to texture limb family when overlay set is missing.
            ResourceLocation wide = modifier.getTexture(PlayerSkin.Model.WIDE);
            ResourceLocation slim = modifier.getTexture(PlayerSkin.Model.SLIM);
            return textureFitsSlot(wide, slot) || textureFitsSlot(slim, slot);
        }

        boolean leftArm = parts.contains(SkinModifier.OverlayPart.LEFT_ARM)
                || parts.contains(SkinModifier.OverlayPart.LEFT_SLEEVE)
                || modifier.replacesVanillaArm(HumanoidArm.LEFT);
        boolean rightArm = parts.contains(SkinModifier.OverlayPart.RIGHT_ARM)
                || parts.contains(SkinModifier.OverlayPart.RIGHT_SLEEVE)
                || modifier.replacesVanillaArm(HumanoidArm.RIGHT);
        boolean leftLeg = parts.contains(SkinModifier.OverlayPart.LEFT_LEG)
                || parts.contains(SkinModifier.OverlayPart.LEFT_PANTS);
        boolean rightLeg = parts.contains(SkinModifier.OverlayPart.RIGHT_LEG)
                || parts.contains(SkinModifier.OverlayPart.RIGHT_PANTS);
        boolean head = parts.contains(SkinModifier.OverlayPart.HEAD)
                || parts.contains(SkinModifier.OverlayPart.HAT);
        boolean body = parts.contains(SkinModifier.OverlayPart.BODY)
                || parts.contains(SkinModifier.OverlayPart.JACKET);

        return switch (slot) {
            case LARM -> leftArm && !rightArm && !leftLeg && !rightLeg;
            case RARM -> rightArm && !leftArm && !leftLeg && !rightLeg;
            case LLEG -> leftLeg && !rightLeg && !leftArm && !rightArm;
            case RLEG -> rightLeg && !leftLeg && !leftArm && !rightArm;
            case EYES, BRAIN -> head && !leftArm && !rightArm && !leftLeg && !rightLeg;
            case SKIN, MUSCLE, BONE, HEART, LUNGS, ORGANS ->
                    // Skin/body mods may paint many regions; reject exclusive single-limb mods.
                    (body || head || (leftArm && rightArm) || (leftLeg && rightLeg))
                            && !(exclusiveLimb(leftArm, rightArm, leftLeg, rightLeg));
        };
    }

    private static boolean exclusiveLimb(boolean la, boolean ra, boolean ll, boolean rl) {
        int n = (la ? 1 : 0) + (ra ? 1 : 0) + (ll ? 1 : 0) + (rl ? 1 : 0);
        return n == 1;
    }

    public static boolean textureFitsSlot(ResourceLocation texture, CyberwareSlot slot) {
        if (texture == null || slot == null) {
            return false;
        }
        LimbFamily family = limbFamilyFromPath(texture.getPath().toLowerCase(Locale.ROOT));
        return switch (slot) {
            case LARM -> family == LimbFamily.LEFT_ARM || family == LimbFamily.UNKNOWN;
            case RARM -> family == LimbFamily.RIGHT_ARM || family == LimbFamily.UNKNOWN;
            case LLEG -> family == LimbFamily.LEFT_LEG || family == LimbFamily.UNKNOWN;
            case RLEG -> family == LimbFamily.RIGHT_LEG || family == LimbFamily.UNKNOWN;
            case EYES, BRAIN -> family == LimbFamily.HEAD || family == LimbFamily.UNKNOWN;
            case SKIN, MUSCLE, BONE, HEART, LUNGS, ORGANS ->
                    family == LimbFamily.BODY || family == LimbFamily.UNKNOWN || family == LimbFamily.HEAD;
        };
    }

    /**
     * Detect limb family from item or texture paths.
     * Prefer longer / more specific markers (leftarm before left, cyberarm with side, etc.).
     */
    static LimbFamily limbFamilyFromPath(String path) {
        if (path == null || path.isBlank()) {
            return LimbFamily.UNKNOWN;
        }
        String p = path.toLowerCase(Locale.ROOT);

        // Explicit Create-Cybernetics stems first.
        if (p.contains("left_cyberarm") || p.contains("leftarm") || p.contains("left_arm")
                || p.contains("firestarter_larm") || p.contains("flywheel_larm")
                || p.contains("knuckles_larm") || p.contains("armcannon_larm")
                || p.contains("arc_cannon_left") || p.contains("arccannon_left")
                || p.contains("sculk_leftarm") || p.contains("sculkleftarm")) {
            return LimbFamily.LEFT_ARM;
        }
        if (p.contains("right_cyberarm") || p.contains("rightarm") || p.contains("right_arm")
                || p.contains("firestarter_rarm") || p.contains("flywheel_rarm")
                || p.contains("knuckles_rarm") || p.contains("armcannon_rarm")
                || p.contains("arc_cannon_right") || p.contains("arccannon_right")
                || p.contains("sculk_rightarm") || p.contains("sculkrightarm")) {
            return LimbFamily.RIGHT_ARM;
        }
        if (p.contains("left_cyberleg") || p.contains("leftleg") || p.contains("left_leg")
                || p.contains("sculk_leftleg") || p.contains("sculkleftleg")) {
            return LimbFamily.LEFT_LEG;
        }
        if (p.contains("right_cyberleg") || p.contains("rightleg") || p.contains("right_leg")
                || p.contains("sculk_rightleg") || p.contains("sculkrightleg")) {
            return LimbFamily.RIGHT_LEG;
        }

        // Segment-aware _larm / _rarm (never bare "rarm" inside "cyberarm").
        if (hasSegment(p, "larm") || p.contains("_larm") || p.endsWith("larm")) {
            return LimbFamily.LEFT_ARM;
        }
        if (hasSegment(p, "rarm") || p.contains("_rarm") || p.endsWith("rarm")) {
            return LimbFamily.RIGHT_ARM;
        }

        if (p.contains("cybereye") || p.contains("guardian_eye") || p.contains("/eyes")
                || p.contains("visor") || p.contains("_eyes_") || p.contains("_eye_")) {
            return LimbFamily.HEAD;
        }
        if (p.contains("cyberleg") || (p.contains("leg") && !p.contains("legacy"))) {
            // Side unknown — treat as unknown so slot gate can still use UNKNOWN→limb allow carefully.
            // Prefer not guessing side from bare "leg".
            return LimbFamily.UNKNOWN;
        }
        if (p.contains("cyberarm") || (p.contains("arm") && !p.contains("harm") && !p.contains("armor"))) {
            return LimbFamily.UNKNOWN;
        }
        return LimbFamily.UNKNOWN;
    }

    private static boolean hasSegment(String path, String segment) {
        // Match _segment_ / _segment. / start/end boundaries; avoids "cyberarm".contains("rarm").
        String needle = "_" + segment;
        if (path.contains(needle + "_") || path.contains(needle + ".") || path.endsWith(needle)
                || path.contains("/" + segment + "_") || path.contains("/" + segment + ".")) {
            return true;
        }
        return path.startsWith(segment + "_") || path.equals(segment);
    }
}
