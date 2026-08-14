"""Generate crisp 16x16 vanity_implant.png pixel art."""
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]

T = (0, 0, 0, 0)
G0 = (18, 22, 28, 255)
G1 = (36, 42, 52, 255)
G2 = (72, 82, 96, 255)
G3 = (140, 150, 165, 255)
G4 = (200, 210, 222, 255)
G5 = (232, 240, 248, 255)
C0 = (0, 90, 110, 255)
C1 = (0, 170, 195, 255)
C2 = (80, 230, 255, 255)
C3 = (180, 250, 255, 255)
K = (10, 12, 16, 255)

# Chrome skin-chip with cyan neural circuit (Minecraft item style).
rows = [
    [T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T],
    [T, T, G1, G2, G3, G3, G3, G3, G3, G3, G3, G2, G1, T, T, T],
    [T, G1, G3, G5, G4, G4, G4, G4, G4, G4, G4, G5, G3, G1, T, T],
    [T, G2, G4, G3, K, K, K, K, K, K, K, G3, G4, G2, G0, T],
    [T, G3, G4, K, K, C0, C1, C2, C1, C0, K, K, G4, G3, G0, T],
    [T, G3, G4, K, C0, C1, C2, C3, C2, C1, C0, K, G4, G3, G1, T],
    [T, G3, G4, K, C1, C2, C3, G5, C3, C2, C1, K, G4, G3, G1, T],
    [T, G3, G4, K, C0, C1, C2, C3, C2, C1, C0, K, G4, G3, G1, T],
    [T, G3, G4, K, K, C0, C1, C2, C1, C0, K, K, G4, G3, G1, T],
    [T, G3, G4, G3, K, K, K, C0, K, K, K, G3, G4, G3, G1, T],
    [T, G2, G4, G5, G4, G3, G3, G3, G3, G3, G4, G5, G4, G2, G0, T],
    [T, G1, G3, G4, G3, G2, C1, C2, C1, G2, G3, G4, G3, G1, T, T],
    [T, T, G1, G2, G3, G3, C0, C1, C0, G3, G3, G2, G1, T, T, T],
    [T, T, T, G0, G1, G2, G2, G2, G2, G2, G1, G0, T, T, T, T],
    [T, T, T, T, T, G0, G1, G1, G1, G0, T, T, T, T, T, T],
    [T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T],
]

img = Image.new("RGBA", (16, 16))
px = img.load()
for y, row in enumerate(rows):
    for x, c in enumerate(row):
        px[x, y] = c

item_path = ROOT / "src/main/resources/assets/cyberneticsvanity/textures/item/vanity_implant.png"
item_path.parent.mkdir(parents=True, exist_ok=True)
img.save(item_path, "PNG")
print("wrote", item_path, img.size)

export_dir = ROOT / "export/curseforge"
export_dir.mkdir(parents=True, exist_ok=True)
preview = img.resize((256, 256), Image.NEAREST)
preview_path = export_dir / "vanity_implant_preview.png"
preview.save(preview_path, "PNG")
print("wrote", preview_path)
