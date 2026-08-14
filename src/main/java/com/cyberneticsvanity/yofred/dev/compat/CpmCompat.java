package com.cyberneticsvanity.yofred.dev.compat;

import com.cyberneticsvanity.yofred.dev.ClientSyncedServerRules;

import com.cyberneticsvanity.yofred.dev.CyberneticsVanity;
import com.cyberneticsvanity.yofred.dev.ClientVanityConfig;
import net.neoforged.fml.ModList;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Soft optional Custom Player Models (CPM) compatibility facade.
 * <p>
 * Never hard-depends on CPM. Detection uses {@link ModList} only; any CPM class
 * loading happens inside {@link CpmSoftBridge}, which is reflective-loaded only
 * when a known CPM mod id is present. Failures log once and disable API features —
 * they never propagate as crashes.
 */
public final class CpmCompat {
    /** NeoForge / modern CPM mod id. */
    public static final String MOD_ID_CPM = "cpm";
    /** Legacy / alternate CPM mod id (IMC target on older builds). */
    public static final String MOD_ID_LEGACY = "customplayermodels";

    private static final String[] MOD_IDS = {MOD_ID_CPM, MOD_ID_LEGACY};

    private static final AtomicBoolean LOADED_LOGGED = new AtomicBoolean(false);
    private static final AtomicBoolean BRIDGE_FAIL_LOGGED = new AtomicBoolean(false);
    private static final AtomicBoolean DETECT_FAIL_LOGGED = new AtomicBoolean(false);

    private static volatile Boolean cachedLoaded;
    private static volatile boolean bridgeAttempted;
    private static volatile boolean bridgeReady;
    private static volatile boolean apiFeaturesDisabled;

    private CpmCompat() {}

    /**
     * True when CPM (either known mod id) is on the active mod list.
     * Safe before/after loader init; never throws.
     */
    public static boolean isLoaded() {
        Boolean cached = cachedLoaded;
        if (cached != null) {
            return cached;
        }
        synchronized (CpmCompat.class) {
            if (cachedLoaded != null) {
                return cachedLoaded;
            }
            boolean found = false;
            try {
                ModList list = ModList.get();
                if (list != null) {
                    for (String id : MOD_IDS) {
                        try {
                            if (list.isLoaded(id)) {
                                found = true;
                                break;
                            }
                        } catch (Throwable t) {
                            logDetectFailureOnce("ModList.isLoaded(" + id + ")", t);
                        }
                    }
                }
            } catch (Throwable t) {
                logDetectFailureOnce("ModList.get()", t);
                found = false;
            }
            cachedLoaded = found;
            if (found && LOADED_LOGGED.compareAndSet(false, true)) {
                CyberneticsVanity.LOGGER.info(
                        "Custom Player Models detected — safe rendering active (limb edits skipped; duplicate skin passes suppressed for custom profiles)"
                );
            }
            return found;
        }
    }

    /**
     * Reflective bootstrap of {@link CpmSoftBridge}. Call from client setup only.
     * No-op when CPM is absent. Never throws.
     */
    public static void bootstrap() {
        try {
            if (!isLoaded()) {
                return;
            }
            ensureBridge();
        } catch (Throwable t) {
            disableApiFeatures("bootstrap", t);
        }
    }

    /**
     * When CPM is present, Create-Cybernetics limb hider / FP arm cancellation must not
     * mutate vanilla {@code PlayerModel} limbs (that fights CPM geometry).
     */
    public static boolean shouldSkipLimbGeometryEdits() {
        try {
            return isLoaded() && !ClientSyncedServerRules.hideMissingLimbs();
        } catch (Throwable t) {
            disableApiFeatures("shouldSkipLimbGeometryEdits", t);
            // Fail closed toward skipping limb edits if something is wrong while CPM might be present.
            return true;
        }
    }

    /**
     * Optional: prefer vanilla limb visibility when CPM reports a custom model.
     * Falls back to "CPM present" if the API probe is unavailable. Never throws.
     */
    public static boolean shouldPreferVanillaLimbVisibility() {
        try {
            if (!isLoaded()) {
                return false;
            }
            if (apiFeaturesDisabled) {
                return true;
            }
            ensureBridge();
            if (!bridgeReady) {
                return true;
            }
            Boolean custom = CpmSoftBridge.hasCustomModelSafe();
            return custom == null || custom;
        } catch (Throwable t) {
            disableApiFeatures("shouldPreferVanillaLimbVisibility", t);
            return true;
        }
    }

    /**
     * Cybernetics renders skin modifiers by re-rendering PlayerModel. CPM also
     * intercepts that render call, which can draw custom cubes repeatedly and
     * cause visible flicker/z-fighting. Suppress only those 2D modifier passes
     * for profiles using a CPM model; keep Cybernetics 3D attachments intact.
     */
    public static boolean shouldSuppressCyberneticsSkinPasses(AbstractClientPlayer player) {
        try {
            if (!isLoaded()) return false;
            ensureBridge();
            if (!bridgeReady || apiFeaturesDisabled) {
                // Safe fallback: CPM is present but cannot tell us which profile
                // is custom, so avoid the known duplicate-render path.
                return true;
            }
            Boolean custom = CpmSoftBridge.hasCustomModelSafe(player);
            return custom == null || custom;
        } catch (Throwable t) {
            disableApiFeatures("CPM skin-pass detection", t);
            return true;
        }
    }

    public static boolean isApiAvailable() {
        try {
            if (!isLoaded() || apiFeaturesDisabled) {
                return false;
            }
            ensureBridge();
            return bridgeReady && CpmSoftBridge.isApiAvailable();
        } catch (Throwable t) {
            disableApiFeatures("isApiAvailable", t);
            return false;
        }
    }

    public static boolean areApiFeaturesDisabled() {
        return apiFeaturesDisabled;
    }

    private static void ensureBridge() {
        if (bridgeAttempted || apiFeaturesDisabled) {
            return;
        }
        synchronized (CpmCompat.class) {
            if (bridgeAttempted || apiFeaturesDisabled) {
                return;
            }
            bridgeAttempted = true;
            try {
                // Our bridge class only; CPM types are Class.forName'd inside CpmSoftBridge.
                CpmSoftBridge.init();
                bridgeReady = !apiFeaturesDisabled;
            } catch (Throwable t) {
                bridgeReady = false;
                disableApiFeatures("load CpmSoftBridge", t);
            }
        }
    }

    static void disableApiFeatures(String where, Throwable t) {
        apiFeaturesDisabled = true;
        bridgeReady = false;
        if (BRIDGE_FAIL_LOGGED.compareAndSet(false, true)) {
            CyberneticsVanity.LOGGER.warn(
                    "CPM soft-compat API disabled after failure at {} — limb-skip still uses ModList only; game continues",
                    where,
                    t
            );
        }
    }

    private static void logDetectFailureOnce(String where, Throwable t) {
        if (DETECT_FAIL_LOGGED.compareAndSet(false, true)) {
            CyberneticsVanity.LOGGER.warn("CPM detection issue at {}; treating CPM as absent", where, t);
        }
    }
}
