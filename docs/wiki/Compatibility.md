# Compatibility

## Custom Player Models

Cybernetics can render the vanilla player model repeatedly for skin modifiers. CPM may also intercept those calls, producing duplicate custom cubes and visible flicker. Vanity suppresses the conflicting duplicate skin passes for custom CPM profiles while preserving compatible 3D attachments.

Sandevistan mirages can render the CPM player model. Long trails use adaptive density to reduce the number of expensive full-model copies.

## Pehkui

Player width and height scaling are preserved in Holoprojector projections and Sandevistan mirages, including when CPM is also installed.

## Corpse

The optional `preserveCorpseAppearance` server rule prevents Cybernetics mutilation and skeleton overlays from replacing the saved corpse appearance.

## Cyber Spells

Compatible rune skin and first-person limb overlays follow Vanity visibility choices.

## Create and Cyberchems

Both are optional from Vanity's perspective. Cybernetics itself remains required.
