package com.cyberneticsvanity.yofred.dev;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Dedicated NeoForge SERVER config for staff/admins.
 * <p>
 * File (dedicated server / integrated host): {@code config/cyberneticsvanity-server.toml}
 */
public final class ServerVanityConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_VILLAGER_DROP = BUILDER
            .comment(
                    "Allow vanity implant drops from villagers.",
                    "When false, villagers never drop the implant regardless of chance."
            )
            .define("enableVillagerDrop", true);

    public static final ModConfigSpec.DoubleValue VILLAGER_DROP_CHANCE = BUILDER
            .comment(
                    "Base chance (0.0-1.0) that a villager drops a vanity implant on death.",
                    "Default 0.125 = 12.5% (1/8)."
            )
            .defineInRange("villagerDropChance", 0.125d, 0.0d, 1.0d);

    public static final ModConfigSpec.DoubleValue LOOTING_BONUS_PER_LEVEL = BUILDER
            .comment("Extra drop chance added per Looting enchantment level on the killer's weapon.")
            .defineInRange("lootingBonusPerLevel", 0.025d, 0.0d, 1.0d);

    public static final ModConfigSpec.DoubleValue MAX_DROP_CHANCE = BUILDER
            .comment("Hard cap on villager drop chance after Looting bonuses (0.0-1.0).")
            .defineInRange("maxDropChance", 0.30d, 0.0d, 1.0d);

    public static final ModConfigSpec.BooleanValue REQUIRE_VANITY_IMPLANT = BUILDER
            .comment(
                    "When true (default), players must surgically install the vanity implant to open",
                    "the Cyber Vanity menu and apply hide toggles.",
                    "Set false on staff servers that want free vanity for everyone."
            )
            .define("requireVanityImplant", true);

    public static final ModConfigSpec.IntValue ALLOWED_PERMISSION_LEVEL = BUILDER
            .comment(
                    "Minimum vanilla permission level required to use Cyber Vanity (menu + applying hides).",
                    "0 = everyone, 1 = moderators, 2 = gamemasters, 3 = admins, 4 = owners.",
                    "Combined with requireVanityImplant: player must meet BOTH gates when implant is required."
            )
            .defineInRange("allowedPermissionLevel", 0, 0, 4);

    public static final ModConfigSpec.BooleanValue PRESERVE_CORPSE_APPEARANCE = BUILDER
            .comment(
                    "Preserve the player's normal skin/model on Corpse mod corpses.",
                    "When enabled, Create-Cybernetics corpse mutilation and skeleton overlays are suppressed.",
                    "This server rule is synchronized so every modded client sees the same corpse appearance."
            )
            .define("preserveCorpseAppearance", false);

    public static final ModConfigSpec.BooleanValue HIDE_MISSING_LIMBS = BUILDER
            .comment(
                    "Visually remove missing arms and legs from player skins.",
                    "Supports vanilla Steve/wide and Alex/slim skins and is synchronized for all modded clients.",
                    "CPM models use their source skin/model mapping where CPM exposes it."
            )
            .define("hideMissingLimbs", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ServerVanityConfig() {}

    public static boolean enableVillagerDrop() {
        return ENABLE_VILLAGER_DROP.get();
    }

    public static double villagerDropChance() {
        return VILLAGER_DROP_CHANCE.get();
    }

    public static double lootingBonusPerLevel() {
        return LOOTING_BONUS_PER_LEVEL.get();
    }

    public static double maxDropChance() {
        return MAX_DROP_CHANCE.get();
    }

    public static boolean requireVanityImplant() {
        return REQUIRE_VANITY_IMPLANT.get();
    }

    public static int allowedPermissionLevel() {
        return ALLOWED_PERMISSION_LEVEL.get();
    }

    public static boolean preserveCorpseAppearance() {
        return PRESERVE_CORPSE_APPEARANCE.get();
    }

    public static boolean hideMissingLimbs() {
        return HIDE_MISSING_LIMBS.get();
    }
}
