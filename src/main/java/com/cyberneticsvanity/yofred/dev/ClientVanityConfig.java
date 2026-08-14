package com.cyberneticsvanity.yofred.dev;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Client-local vanity preferences (persisted). Synced to server when changed online.
 */
public final class ClientVanityConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue CYBER_VANITY_ENABLED = BUILDER
            .comment(
                    "Master Cyber Vanity switch. When true, per-implant hide keys below apply.",
                    "When false, all cyberware visuals render normally."
            )
            .define("cyberVanityEnabled", false);

    public static final ModConfigSpec.BooleanValue SANDEVISTAN_CPM_MODELS = BUILDER
            .comment(
                    "Render CPM models in Sandevistan mirages.",
                    "When enabled, mirage density is reduced automatically for long trails to protect FPS.",
                    "When disabled, the vanilla player model is used while Pehkui scale is still preserved."
            )
            .define("sandevistanCpmModels", true);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> HIDDEN_IMPLANTS = BUILDER
            .comment(
                    "Stable implant keys to hide while Cyber Vanity is enabled.",
                    "Format: SLOT:index:namespace:path  (e.g. LARM:0:createcybernetics:basecyberware_leftarm)",
                    "Meta keys: __meta:highlights , __meta:limb_hiding"
            )
            .defineListAllowEmpty("hiddenImplants", ArrayList::new, () -> "", o -> o instanceof String);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ClientVanityConfig() {}

    public static boolean sandevistanCpmModels() {
        return SANDEVISTAN_CPM_MODELS.get();
    }

    public static VanitySnapshot snapshot() {
        Set<String> hidden = new LinkedHashSet<>();
        for (String key : HIDDEN_IMPLANTS.get()) {
            if (key != null && !key.isBlank()) {
                hidden.add(key);
            }
        }
        return new VanitySnapshot(CYBER_VANITY_ENABLED.get(), hidden);
    }

    public static void apply(VanitySnapshot snapshot) {
        CYBER_VANITY_ENABLED.set(snapshot.enabled());
        HIDDEN_IMPLANTS.set(new ArrayList<>(snapshot.hiddenKeyList()));
        SPEC.save();
    }
}
