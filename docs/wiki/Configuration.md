# Configuration

## Client

The NeoForge Mods screen contains the client settings.

- `cyberVanityEnabled`: master local vanity state.
- `hiddenImplants`: persisted implant visibility keys.

## Server

Server settings are stored in `config/cyberneticsvanity-server.toml`.

- `enableVillagerDrop`: enables Vanity Implant villager drops.
- `villagerDropChance`: base drop chance; default `0.125`.
- `lootingBonusPerLevel`: additional chance per Looting level; default `0.025`.
- `maxDropChance`: maximum final drop chance; default `0.30`.
- `requireVanityImplant`: requires surgical installation before the menu can be used.
- `allowedPermissionLevel`: minimum vanilla permission level, from `0` to `4`.
- `hideMissingLimbs`: visually removes missing arms and legs from supported player skins.

Server visual rules are synchronized to modded clients.

CPM, Pehkui, projection, and external-renderer settings are configured by [CPM Visual Bridge](https://github.com/yofred09/cpm-visual-bridge), not Cybernetics: Vanity.
