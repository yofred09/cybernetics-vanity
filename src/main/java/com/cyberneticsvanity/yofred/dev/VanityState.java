package com.cyberneticsvanity.yofred.dev;

import com.cyberneticsvanity.yofred.dev.compat.CpmCompat;
import com.cyberneticsvanity.yofred.dev.network.VanitySync;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.client.model.AttachmentAnchor;
import com.perigrine3.createcybernetics.client.skin.SkinModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Runtime vanity queries. Mixins must pass the rendered player so multiplayer sync applies.
 */
public final class VanityState {
    private VanityState() {}

    public static boolean hasImplant(Player player) {
        return VanityImplantAccess.isInstalled(player);
    }

    public static boolean hasImplant() {
        return com.cyberneticsvanity.yofred.dev.client.ClientVanityAccess.isInstalledOnLocalPlayer();
    }

    /**
     * Whether the local player may open the vanity menu / use vanity features,
     * honouring synced server rules (implant gate + permission level).
     */
    public static boolean canUseVanityLocally() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        if (ClientSyncedServerRules.isClientOnlyMode()) {
            return hasImplant(mc.player);
        }
        int need = ClientSyncedServerRules.allowedPermissionLevel();
        if (need > 0 && !mc.player.hasPermissions(need)) {
            return false;
        }
        if (!ClientSyncedServerRules.requireVanityImplant()) {
            return true;
        }
        return hasImplant(mc.player);
    }

    /** Server-side gate for applying vanity updates. Prefer calling {@link ServerVanityGate} from server packets. */
    public static boolean canUseVanityOnServer(Player player) {
        return ServerVanityGate.canUse(player);
    }

    public static VanitySnapshot snapshotFor(Player player) {
        if (player == null) {
            return VanitySnapshot.DISABLED;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && player.getUUID().equals(mc.player.getUUID())) {
            return ClientVanityConfig.snapshot();
        }
        return VanitySync.clientSnapshot(player.getUUID());
    }

    public static boolean isVanityEnabled(Player player) {
        return snapshotFor(player).enabled();
    }

    public static boolean isVanityEnabled() {
        return ClientVanityConfig.CYBER_VANITY_ENABLED.get();
    }

    /**
     * Vanity master switch is on AND player is allowed to use vanity
     * (implant / staff config). Used by mixins to suppress visuals.
     */
    public static boolean isVanityActive(Player player) {
        if (player == null || !isVanityEnabled(player)) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && player.getUUID().equals(mc.player.getUUID())) {
            if (ClientSyncedServerRules.isClientOnlyMode()) {
                return true;
            }
            if (!ClientSyncedServerRules.requireVanityImplant()) {
                return true;
            }
            return hasImplant(player);
        }
        // Remote players: if their synced snapshot is enabled, apply hides.
        // Implant presence is enforced when they push updates; remote view uses sync only.
        return true;
    }

    public static boolean isVanityActive() {
        Minecraft mc = Minecraft.getInstance();
        return isVanityActive(mc.player);
    }

    public static void setVanityEnabled(boolean enabled) {
        VanitySnapshot next = ClientVanityConfig.snapshot().withEnabled(enabled);
        ClientVanityConfig.apply(next);
        VanitySync.sendLocalToServer(next);
    }

    public static boolean toggleVanity() {
        boolean next = !isVanityEnabled();
        setVanityEnabled(next);
        return next;
    }

    public static boolean isImplantHidden(Player player, String key) {
        return snapshotFor(player).isHidden(key);
    }

    public static void setImplantHidden(String key, boolean hidden) {
        VanitySnapshot next = ClientVanityConfig.snapshot().withHidden(key, hidden);
        ClientVanityConfig.apply(next);
        VanitySync.sendLocalToServer(next);
    }

    public static boolean toggleImplant(String key) {
        boolean next = !ClientVanityConfig.snapshot().hiddenKeys().contains(key);
        setImplantHidden(key, next);
        return next;
    }

    public static boolean isEntryHidden(Player player, InstalledVisualImplants.Entry entry) {
        return entry != null && isImplantHidden(player, entry.key());
    }

    public static boolean isSlotIndexHidden(Player player, CyberwareSlot slot, int index) {
        if (!isVanityActive(player) || slot == null) {
            return false;
        }
        for (InstalledVisualImplants.Entry entry : InstalledVisualImplants.list(player)) {
            if (entry.slot() == slot && entry.index() == index && isEntryHidden(player, entry)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isItemHiddenInSlot(Player player, ResourceLocation itemId, CyberwareSlot slot) {
        if (!isVanityActive(player) || itemId == null || slot == null) {
            return false;
        }
        for (InstalledVisualImplants.Entry entry : InstalledVisualImplants.list(player)) {
            if (entry.slot() != slot || !isEntryHidden(player, entry)) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(entry.stack().getItem());
            if (itemId.equals(id)) {
                return true;
            }
            // Same-slot scavenged ↔ craftable alias via strict texture stem match only.
            if (InstalledVisualImplants.textureMatchesItem(
                    ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "item/" + itemId.getPath()),
                    id
            )) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldHideHighlights(Player player) {
        return isVanityActive(player) && snapshotFor(player).hiddenKeys().contains(VanityKeys.HIGHLIGHTS);
    }

    public static boolean shouldHideHighlights() {
        Minecraft mc = Minecraft.getInstance();
        return shouldHideHighlights(mc.player);
    }

    /**
     * Skip CyberwareLimbHider model mutation when CPM is loaded, or when the player
     * explicitly enables the limb-hiding meta skip.
     * Overlay-only upgrades (Firestarter) must NOT cancel the entire limb hider.
     */
    public static boolean shouldSkipLimbHiding(Player player) {
        if (CpmCompat.shouldSkipLimbGeometryEdits()) {
            return true;
        }
        if (!isVanityActive(player)) {
            return false;
        }
        return snapshotFor(player).hiddenKeys().contains(VanityKeys.LIMB_HIDING);
    }

    /** Hide every installed visual implant key (keeps meta keys). Requires vanity active path. */
    public static void hideAllVisualImplants() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        LinkedHashSet<String> next = new LinkedHashSet<>(ClientVanityConfig.snapshot().hiddenKeys());
        for (InstalledVisualImplants.Entry entry : InstalledVisualImplants.list(mc.player)) {
            next.add(entry.key());
        }
        VanitySnapshot snap = new VanitySnapshot(true, next);
        ClientVanityConfig.apply(snap);
        VanitySync.sendLocalToServer(snap);
    }

    /** Clear hide flags for installed visual implants (keeps meta keys). */
    public static void showAllVisualImplants() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        LinkedHashSet<String> next = new LinkedHashSet<>();
        for (String key : ClientVanityConfig.snapshot().hiddenKeys()) {
            if (VanityKeys.isMeta(key)) {
                next.add(key);
            }
        }
        VanitySnapshot snap = new VanitySnapshot(ClientVanityConfig.snapshot().enabled(), next);
        ClientVanityConfig.apply(snap);
        VanitySync.sendLocalToServer(snap);
    }

    public static boolean shouldSkipLimbHiding() {
        Minecraft mc = Minecraft.getInstance();
        return shouldSkipLimbHiding(mc.player);
    }

    /**
     * When true, {@code CyberwareLimbHider.shouldRender*} should treat the limb as
     * vanilla (return false) because all limb-replacing visuals in that slot are hidden.
     */
    public static boolean shouldForceVanillaLimb(Player player, CyberwareSlot slot) {
        if (!isVanityActive(player) || slot == null) {
            return false;
        }
        boolean sawReplacer = false;
        for (InstalledVisualImplants.Entry entry : InstalledVisualImplants.list(player)) {
            if (entry.slot() != slot) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(entry.stack().getItem());
            if (!InstalledVisualImplants.isLimbReplacingItem(id)) {
                continue;
            }
            sawReplacer = true;
            if (!isEntryHidden(player, entry)) {
                return false;
            }
        }
        return sawReplacer;
    }

    public static boolean shouldHideAttachment(Player player, AttachmentAnchor anchor, ResourceLocation texture) {
        if (player == null || anchor == null) {
            return false;
        }
        if (!isVanityActive(player)) {
            return false;
        }
        CyberwareSlot anchorSlot = anchorToSlot(anchor);
        VanitySnapshot snap = snapshotFor(player);
        for (InstalledVisualImplants.Entry entry : InstalledVisualImplants.list(player)) {
            if (!snap.hiddenKeys().contains(entry.key())) {
                continue;
            }
            // Limb anchors: only the matching limb slot may hide attachments there.
            // Head/body anchors: only eyes/brain (head) or torso slots (body) — never arm/leg rows.
            if (anchorSlot != null && entry.slot() != anchorSlot) {
                if (anchor == AttachmentAnchor.HEAD) {
                    if (entry.slot() != CyberwareSlot.EYES && entry.slot() != CyberwareSlot.BRAIN) {
                        continue;
                    }
                } else if (anchor == AttachmentAnchor.BODY) {
                    if (entry.slot() != CyberwareSlot.SKIN && entry.slot() != CyberwareSlot.MUSCLE
                            && entry.slot() != CyberwareSlot.BONE && entry.slot() != CyberwareSlot.HEART
                            && entry.slot() != CyberwareSlot.LUNGS && entry.slot() != CyberwareSlot.ORGANS) {
                        continue;
                    }
                } else {
                    continue;
                }
            }
            if (InstalledVisualImplants.matchesAttachmentTexture(texture, entry)) {
                return true;
            }
            // Same-slot limb replacer: hide attachments on that limb even if texture alias misses.
            if (anchorSlot != null && entry.slot() == anchorSlot) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(entry.stack().getItem());
                if (InstalledVisualImplants.isLimbReplacingItem(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static CyberwareSlot anchorToSlot(AttachmentAnchor anchor) {
        if (anchor == null) {
            return null;
        }
        return switch (anchor) {
            case LEFT_ARM -> CyberwareSlot.LARM;
            case RIGHT_ARM -> CyberwareSlot.RARM;
            case LEFT_LEG -> CyberwareSlot.LLEG;
            case RIGHT_LEG -> CyberwareSlot.RLEG;
            case HEAD -> CyberwareSlot.EYES;
            case BODY -> CyberwareSlot.SKIN;
        };
    }

    public static boolean shouldHideOverlayPart(Player player, SkinModifier.OverlayPart part) {
        if (!isVanityActive(player) || part == null) {
            return false;
        }
        List<InstalledVisualImplants.Entry> installed = InstalledVisualImplants.list(player);
        VanitySnapshot snap = snapshotFor(player);
        for (InstalledVisualImplants.Entry entry : installed) {
            if (!snap.hiddenKeys().contains(entry.key())) {
                continue;
            }
            // Only suppress exclusive limb/eye regions via part gate; modifiers use texture match.
            CyberwareSlot slot = entry.slot();
            if ((slot == CyberwareSlot.LARM || slot == CyberwareSlot.RARM
                    || slot == CyberwareSlot.LLEG || slot == CyberwareSlot.RLEG
                    || slot == CyberwareSlot.EYES)
                    && InstalledVisualImplants.matchesOverlayPart(part, entry)
                    && InstalledVisualImplants.isLimbReplacingItem(
                    BuiltInRegistries.ITEM.getKey(entry.stack().getItem()))) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldHideOverlayPart(SkinModifier.OverlayPart part) {
        Minecraft mc = Minecraft.getInstance();
        return shouldHideOverlayPart(mc.player, part);
    }

    public static boolean shouldHideModifier(Player player, SkinModifier modifier) {
        if (player == null || modifier == null) {
            return false;
        }
        if (!isVanityActive(player)) {
            return false;
        }
        VanitySnapshot snap = snapshotFor(player);
        List<InstalledVisualImplants.Entry> installed = InstalledVisualImplants.list(player);
        for (InstalledVisualImplants.Entry entry : installed) {
            if (!snap.hiddenKeys().contains(entry.key())) {
                continue;
            }
            if (InstalledVisualImplants.matchesModifier(modifier, entry)) {
                return true;
            }
        }

        ResourceLocation wide = modifier.getTexture(PlayerSkin.Model.WIDE);
        ResourceLocation slim = modifier.getTexture(PlayerSkin.Model.SLIM);
        if (isCybereyeTexture(wide) || isCybereyeTexture(slim)) {
            for (InstalledVisualImplants.Entry entry : installed) {
                if (entry.slot() == CyberwareSlot.EYES && snap.hiddenKeys().contains(entry.key())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean shouldHideModifier(SkinModifier modifier) {
        Minecraft mc = Minecraft.getInstance();
        return shouldHideModifier(mc.player, modifier);
    }

    public static boolean shouldHideArm(Player player, HumanoidArm arm) {
        if (player == null) {
            return false;
        }
        if (!isVanityActive(player)) {
            return false;
        }
        CyberwareSlot slot = arm == HumanoidArm.LEFT ? CyberwareSlot.LARM : CyberwareSlot.RARM;
        VanitySnapshot snap = snapshotFor(player);
        for (InstalledVisualImplants.Entry entry : InstalledVisualImplants.list(player)) {
            if (entry.slot() == slot && snap.hiddenKeys().contains(entry.key())) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(entry.stack().getItem());
                if (InstalledVisualImplants.isLimbReplacingItem(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean shouldHideArms(Player player) {
        return shouldHideArm(player, HumanoidArm.LEFT) || shouldHideArm(player, HumanoidArm.RIGHT);
    }

    public static boolean shouldHideArms() {
        Minecraft mc = Minecraft.getInstance();
        return shouldHideArms(mc.player);
    }

    public static boolean shouldSuppressFirstPersonArmAlteration(Player player) {
        return shouldSkipLimbHiding(player) || shouldHideArms(player);
    }

    public static boolean shouldSuppressFirstPersonArmAlteration() {
        Minecraft mc = Minecraft.getInstance();
        return shouldSuppressFirstPersonArmAlteration(mc.player);
    }

    public static boolean shouldSkipAllOverlays(Player player) {
        if (player == null) {
            return false;
        }
        if (!isVanityActive(player)) {
            return false;
        }
        List<InstalledVisualImplants.Entry> installed = InstalledVisualImplants.list(player);
        if (installed.isEmpty()) {
            return false;
        }
        VanitySnapshot snap = snapshotFor(player);
        for (InstalledVisualImplants.Entry entry : installed) {
            if (!snap.hiddenKeys().contains(entry.key())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isCybereyeTexture(ResourceLocation texture) {
        if (texture == null) {
            return false;
        }
        String path = texture.getPath();
        return path.contains("cybereye") || path.contains("cybereyes");
    }
}
