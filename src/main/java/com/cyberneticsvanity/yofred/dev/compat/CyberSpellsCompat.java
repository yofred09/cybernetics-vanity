package com.cyberneticsvanity.yofred.dev.compat;

import com.cyberneticsvanity.yofred.dev.CyberneticsVanity;
import net.neoforged.fml.ModList;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Soft optional Cyber Spells ({@code cyberspells}) detection.
 * Never hard-depends; mixins targeting Cyber Spells use a separate optional mixin config.
 */
public final class CyberSpellsCompat {
    public static final String MOD_ID = "cyberspells";

    private static final AtomicBoolean LOGGED = new AtomicBoolean(false);
    private static volatile Boolean cached;

    private CyberSpellsCompat() {}

    public static boolean isLoaded() {
        Boolean c = cached;
        if (c != null) {
            return c;
        }
        synchronized (CyberSpellsCompat.class) {
            if (cached != null) {
                return cached;
            }
            boolean found = false;
            try {
                ModList list = ModList.get();
                found = list != null && list.isLoaded(MOD_ID);
            } catch (Throwable t) {
                CyberneticsVanity.LOGGER.warn("Cyber Spells detection failed; treating as absent", t);
                found = false;
            }
            cached = found;
            if (found && LOGGED.compareAndSet(false, true)) {
                CyberneticsVanity.LOGGER.info(
                        "Cyber Spells detected — soft compat active (RuneSkinLayer / FP rune hide)"
                );
            }
            return found;
        }
    }
}
