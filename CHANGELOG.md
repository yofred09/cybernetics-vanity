# Changelog

## 0.5.2 — Development

### Fixed

- Hiding the Sculk Heart now also hides sculk appearance added later by its permanent effect.
- Installing the Vanity Implant no longer makes the player's face or eyes appear blank.
- Fixed transparent eye pixels when the Vanity Implant is combined with Netherite Plating.
- Hiding the Heat Engine now suppresses its flame and smoke particles without disabling energy generation.

### Changed

- Updated compatibility and development runtime to Cybernetics 0.5.2 HOTFIX on NeoForge 1.21.1.
- Moved all CPM and Pehkui rendering responsibility to the optional CPM Visual Bridge addon.
- Removed the old CPM, Pehkui, Sandevistan and CPM Corpse compatibility hooks from Vanity.

## 0.5.1 — Development

### Added

- Support for Cybernetics 0.5.1 HOTFIX on NeoForge 1.21.1.
- Vanity Implant as a Synthskin replacement with tattoo support.
- CPM models in Sandevistan mirage trails.
- Pehkui scale support for Sandevistan mirages and Holoprojector projections.
- Adaptive CPM mirage density and a client option to disable CPM Sandevistan models.
- Server options for Corpse appearance preservation and missing-limb rendering.
- In-game settings screen accessible from the NeoForge Mods menu.

### Fixed

- CPM parts flickering when Cybernetics rendered duplicate skin passes.
- Sandevistan mirages ignoring CPM models and Pehkui player size.
- Vanity Implant not replacing Synthskin or Dermal Tissue during surgery.
- Unsafe CPM auxiliary rendering when animation synchronization was not initialized.
- Compatibility fallbacks that could disable CPM support after a render failure.

### Changed

- Minimum NeoForge version is now 21.1.244.
- Long CPM Sandevistan trails render fewer full custom-model copies to protect client FPS.

## 0.5.0

- Added support for Cybernetics 0.5.0 on NeoForge 1.21.1.
- Added the Cyber Vanity menu, per-implant visibility, bulk actions, preview, synchronization, and persistence.
- Added first-person visual controls and optional CPM/Cyber Spells compatibility.
