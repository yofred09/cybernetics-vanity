# Cybernetics: Vanity

Cosmetic control and compatibility addon for [Cybernetics](https://www.curseforge.com/minecraft/mc-mods/cybernetics) on NeoForge 1.21.1.

Cybernetics: Vanity lets players decide which installed cyberware remains visible without uninstalling it or losing its effects. Install the Vanity Implant, assign the menu key, and control each visual implant from a synchronized Robosurgeon-inspired interface.

## Download

Official builds are distributed exclusively through:

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/cybernetics-vanity)
- [Modrinth](https://modrinth.com/mod/cybernetics-vanity)

GitHub is used for source code, documentation, and issue tracking only. Compiled releases are not distributed here.

## Features

- Hide or show each installed visual implant independently.
- Master vanity switch plus **Hide all** and **Show all** actions.
- Rotating live player preview.
- First- and third-person visual control.
- Multiplayer synchronization and persistent player choices.
- Vanity Implant implemented as a Synthskin replacement, including tattoo support.
- Configurable villager drop, Looting bonus, implant requirement, and permission level.
- Optional missing-limb rendering for Steve and Alex skins.
- Optional preservation of player appearance on Corpse mod corpses.
- English and Brazilian Portuguese translations.

## Compatibility

- **[Customizable Player Models (CPM)](https://www.curseforge.com/minecraft/mc-mods/custom-player-models):** avoids duplicate skin passes and flickering on custom models.
- **[Pehkui](https://www.curseforge.com/minecraft/mc-mods/pehkui):** preserves player scale in Holoprojector projections and Sandevistan mirages.
- **Sandevistan + CPM:** mirages use the player's CPM model with adaptive render density to protect FPS; this can be disabled in the client settings.
- **[Cyber Spells](https://www.curseforge.com/minecraft/mc-mods/cyber-spells):** vanity controls compatible rune overlays.
- **[Corpse](https://www.curseforge.com/minecraft/mc-mods/corpse):** optional appearance-preservation rule.
- **[Create](https://www.curseforge.com/minecraft/mc-mods/create):** optional. [Cybernetics](https://www.curseforge.com/minecraft/mc-mods/cybernetics) itself remains required.

All optional integrations are soft dependencies: the addon continues to load when they are absent.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.244 or newer
- [Cybernetics](https://www.curseforge.com/minecraft/mc-mods/cybernetics) 0.5.2 or newer for the current development branch

## Getting started

1. Install Cybernetics: Vanity on the server and every connecting client.
2. Obtain a Vanity Implant from a villager drop, unless the server disables the implant requirement.
3. Surgically install it as a skin replacement.
4. Assign **Open Cyber Vanity Menu** under Controls → Cybernetics: Vanity.
5. Open the menu and choose which installed visuals should remain visible.

See the [wiki](https://github.com/yofred09/cybernetics-vanity/wiki) for configuration, compatibility details, and troubleshooting.

## Building

```bash
./gradlew build
```

Local development builds are written to `build/libs/`. Java 21 is required. Do not redistribute development builds; official downloads are available only from CurseForge and Modrinth.

## Support

Report reproducible problems through [GitHub Issues](https://github.com/yofred09/cybernetics-vanity/issues). Include the Minecraft, NeoForge, Cybernetics, CPM, and Pehkui versions involved, plus whether the problem occurs in first person, third person, or a projection.

Join the [official Discord community](https://discord.gg/R5VnN7Rn5H) for help, discussion, previews and development updates.

## Credits

- [Cybernetics](https://www.curseforge.com/minecraft/mc-mods/cybernetics) by Perigrine33 — required parent mod and cyberware framework.
- Cybernetics: Vanity by Yo_Fred.
- Original addon idea by OkarinY.

This project is not affiliated with Mojang or Microsoft.
