# Configuration

## Client

The NeoForge Mods screen contains the client settings.

- `cyberVanityEnabled`: master local vanity state.
- `hiddenImplants`: persisted implant visibility keys.
- `sandevistanCpmModels`: use CPM models in Sandevistan mirages. Enabled by default and automatically reduces mirage density for long trails.

Disabling CPM Sandevistan models keeps Pehkui scaling but uses the vanilla player model for the trail.

## Server

Server settings are stored in `config/cyberneticsvanity-server.toml`.

- `enableVillagerDrop`: enables Vanity Implant villager drops.
- `villagerDropChance`: base drop chance; default `0.125`.
- `lootingBonusPerLevel`: additional chance per Looting level; default `0.025`.
- `maxDropChance`: maximum final drop chance; default `0.30`.
- `requireVanityImplant`: requires surgical installation before the menu can be used.
- `allowedPermissionLevel`: minimum vanilla permission level, from `0` to `4`.
- `preserveCorpseAppearance`: prevents Cybernetics corpse mutilation and skeleton overlays from replacing the preserved appearance.
- `hideMissingLimbs`: visually removes missing arms and legs from supported player skins.

Server visual rules are synchronized to modded clients.
