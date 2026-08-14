package com.cyberneticsvanity.yofred.dev.client;

import com.cyberneticsvanity.yofred.dev.ClientVanityConfig;
import com.cyberneticsvanity.yofred.dev.InstalledVisualImplants;
import com.cyberneticsvanity.yofred.dev.VanityKeys;
import com.cyberneticsvanity.yofred.dev.VanityState;
import com.cyberneticsvanity.yofred.dev.compat.CpmCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Robosurgeon-inspired Cyber Vanity menu: rotating player preview (no inventory grid),
 * master toggle, per-implant hide/show, hide-all / show-all, and Done.
 *
 * Row columns (L→R): icon | label (ellipsis) | status | toggle — never overlapping.
 */
public class VanityConfigScreen extends Screen {
    private static final ResourceLocation GUI_BG =
            ResourceLocation.fromNamespaceAndPath("createcybernetics", "textures/gui/robosurgeon/robosurgeon_gui.png");
    private static final ResourceLocation ON_TOGGLE =
            ResourceLocation.fromNamespaceAndPath("createcybernetics", "textures/gui/on_toggle.png");
    private static final ResourceLocation OFF_TOGGLE =
            ResourceLocation.fromNamespaceAndPath("createcybernetics", "textures/gui/off_toggle.png");
    private static final ResourceLocation SLOT =
            ResourceLocation.fromNamespaceAndPath("createcybernetics", "textures/gui/robosurgeon/robosurgeon_interface_slot.png");
    private static final ResourceLocation SLOT_HOVER =
            ResourceLocation.fromNamespaceAndPath("createcybernetics", "textures/gui/robosurgeon/robosurgeon_interface_slothover.png");
    private static final ResourceLocation BACK_ICON =
            ResourceLocation.fromNamespaceAndPath("createcybernetics", "textures/gui/robosurgeon/robosurgeon_interface_backbutton.png");

    private static final int TOGGLE_SIZE = 12;
    /** Preview chrome width (robosurgeon native); height stops above inventory slot bezel. */
    private static final int PREVIEW_W = 176;
    /** Texture y≈117 is back-button row; inventory chrome begins below — keep height ≤116. */
    private static final int PREVIEW_H = 116;
    /** Source texture is 176×222; blit only the upper preview band (no 3×9 chest). */
    private static final int TEX_W = 176;
    private static final int TEX_H = 222;
    private static final int PREVIEW_INNER_BG = 0xFF0A0C10;
    private static final int PANEL_W = 416;
    private static final int PANEL_H = 268;
    private static final int LIST_W = 210;
    private static final int ROW_H = 22;
    private static final int ICON_SIZE = 18;
    private static final int STATUS_COL_W = 42;
    private static final int PREVIEW_SCALE = 48;
    private static final int ACCENT = 0xFF00C8E0;
    private static final int PANEL_BG = 0xE0101418;
    private static final int PANEL_EDGE = 0xFF1A3A44;
    private static final int DIM = 0xC0101010;
    private static final int ICON_WELL = 0xFF3A6A78;
    private static final int DONE_W = 56;
    private static final int DONE_H = 16;

    private final Screen parent;
    private final List<Row> rows = new ArrayList<>();
    private int panelX;
    private int panelY;
    private int scroll;
    private int maxScroll;
    private int listTop;
    private int listBottom;
    private int listLeft;
    private int listRight;
    private int chromeX;
    private int chromeY;
    private int previewX;
    private int previewY;
    private int implantCount;
    private MasterToggle masterToggle;
    private float previewRotation = 180f;
    private boolean draggingPreview;
    private double lastDragX;
    private int doneX;
    private int doneY;
    private int footerNoteY;

    public VanityConfigScreen(Screen parent) {
        super(Component.translatable("gui.cyberneticsvanity.menu.title"));
        this.parent = parent;
    }

    public static void open() {
        Minecraft mc = Minecraft.getInstance();
        if (!VanityState.canUseVanityLocally()) {
            if (mc.player != null) {
                mc.player.displayClientMessage(
                        Component.translatable("message.cyberneticsvanity.need_implant"),
                        true
                );
            }
            return;
        }
        mc.setScreen(new VanityConfigScreen(mc.screen));
    }

    @Override
    protected void init() {
        clearWidgets();
        rows.clear();
        panelX = (this.width - PANEL_W) / 2;
        panelY = (this.height - PANEL_H) / 2;

        listLeft = panelX + 10;
        listRight = panelX + 10 + LIST_W;
        // Header: title + subtitle + master + bulk → list; footer reserved below
        listTop = panelY + 70;
        listBottom = panelY + PANEL_H - 42;

        chromeX = panelX + PANEL_W - PREVIEW_W - 10;
        chromeY = panelY + 44;
        previewX = chromeX + PREVIEW_W / 2;
        // Feet sit near bottom of cropped chrome (matches robosurgeon ~topPos+105 in 116px band).
        previewY = chromeY + 98;

        int masterW = 180;
        masterToggle = new MasterToggle(panelX + (PANEL_W - masterW) / 2, panelY + 28, masterW);
        addRenderableWidget(masterToggle);

        int bulkY = panelY + 48;
        int bulkW = (LIST_W - 6) / 2;
        addRenderableWidget(new BulkButton(
                listLeft,
                bulkY,
                bulkW,
                16,
                Component.translatable("gui.cyberneticsvanity.menu.hide_all"),
                () -> {
                    VanityState.hideAllVisualImplants();
                    refreshRowActive();
                }
        ));
        addRenderableWidget(new BulkButton(
                listLeft + bulkW + 6,
                bulkY,
                bulkW,
                16,
                Component.translatable("gui.cyberneticsvanity.menu.show_all"),
                () -> {
                    VanityState.showAllVisualImplants();
                    refreshRowActive();
                }
        ));

        List<InstalledVisualImplants.Entry> implants = InstalledVisualImplants.list(Minecraft.getInstance().player);
        implantCount = implants.size();

        addRow(new Row(VanityKeys.HIGHLIGHTS, ItemStack.EMPTY,
                Component.translatable("gui.cyberneticsvanity.meta.highlights"), true));
        addRow(new Row(VanityKeys.LIMB_HIDING, ItemStack.EMPTY,
                Component.translatable("gui.cyberneticsvanity.meta.limb_hiding"), true));
        for (InstalledVisualImplants.Entry entry : implants) {
            addRow(new Row(entry.key(), entry.stack(), entry.stack().getHoverName(), false));
        }

        int contentH = rows.size() * ROW_H;
        int viewH = listBottom - listTop;
        maxScroll = Math.max(0, contentH - viewH);
        scroll = Mth.clamp(scroll, 0, maxScroll);
        relayoutRows();

        footerNoteY = panelY + PANEL_H - 26;
        doneX = panelX + PANEL_W - DONE_W - 12;
        doneY = panelY + PANEL_H - DONE_H - 12;
        addRenderableWidget(new DoneButton(doneX, doneY, DONE_W, DONE_H));
    }

    private void addRow(Row row) {
        rows.add(row);
        addRenderableWidget(row);
    }

    private void relayoutRows() {
        int y = listTop - scroll;
        boolean vanityOn = VanityState.isVanityEnabled();
        for (Row row : rows) {
            row.setX(listLeft);
            row.setY(y);
            row.setWidth(listRight - listLeft);
            boolean inView = y + row.getHeight() > listTop && y < listBottom;
            row.visible = inView;
            row.active = vanityOn && inView;
            y += ROW_H;
        }
    }

    private void refreshRowActive() {
        relayoutRows();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0 && mouseX >= listLeft && mouseX <= listRight
                && mouseY >= listTop && mouseY <= listBottom) {
            scroll = Mth.clamp(scroll - (int) (scrollY * ROW_H), 0, maxScroll);
            relayoutRows();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isOverPreview(mouseX, mouseY)) {
            draggingPreview = true;
            lastDragX = mouseX;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            draggingPreview = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingPreview && button == 0) {
            previewRotation += (float) ((mouseX - lastDragX) * 1.2);
            lastDragX = mouseX;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private boolean isOverPreview(double mouseX, double mouseY) {
        return mouseX >= chromeX + 8 && mouseX <= chromeX + PREVIEW_W - 8
                && mouseY >= chromeY + 8 && mouseY <= chromeY + PREVIEW_H - 8;
    }

    @Override
    public void tick() {
        super.tick();
        if (!draggingPreview) {
            previewRotation += 0.35f;
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, DIM);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(panelX - 2, panelY - 2, panelX + PANEL_W + 2, panelY + PANEL_H + 2, PANEL_EDGE);
        graphics.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, PANEL_BG);
        graphics.fill(panelX, panelY, panelX + PANEL_W, panelY + 3, ACCENT);
        graphics.fill(panelX, panelY + PANEL_H - 3, panelX + PANEL_W, panelY + PANEL_H, ACCENT);

        // Preview-only robosurgeon chrome (upper UV band — inventory slot bezel never drawn)
        graphics.fill(chromeX, chromeY, chromeX + PREVIEW_W, chromeY + PREVIEW_H, PREVIEW_INNER_BG);
        graphics.blit(GUI_BG, chromeX, chromeY, 0, 0, PREVIEW_W, PREVIEW_H, TEX_W, TEX_H);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelY + 6, 0xE0FFFF);
        graphics.drawCenteredString(
                this.font,
                Component.translatable("gui.cyberneticsvanity.menu.subtitle"),
                this.width / 2,
                panelY + 16,
                0x7AA8B0
        );

        renderPlayerPreview(graphics, mouseX, mouseY);

        if (CpmCompat.isLoaded()) {
            Component note = Component.translatable("gui.cyberneticsvanity.menu.cpm_note");
            int maxNoteW = doneX - listLeft - 8;
            String clipped = ellipsize(this.font, note.getString(), Math.max(40, maxNoteW));
            graphics.drawString(this.font, clipped, listLeft, footerNoteY, 0x889999, false);
        }

        graphics.enableScissor(listLeft - 1, listTop - 1, listRight + 1, listBottom + 1);
        for (Row row : rows) {
            if (row.visible) {
                row.render(graphics, mouseX, mouseY, partialTick);
            }
        }
        graphics.disableScissor();

        for (var child : this.children()) {
            if (child instanceof MasterToggle || child instanceof BulkButton || child instanceof DoneButton) {
                ((AbstractWidget) child).render(graphics, mouseX, mouseY, partialTick);
            }
        }

        if (implantCount == 0) {
            graphics.drawCenteredString(
                    this.font,
                    Component.translatable("gui.cyberneticsvanity.menu.empty"),
                    (listLeft + listRight) / 2,
                    (listTop + listBottom) / 2,
                    0x889999
            );
        }
    }

    private void renderPlayerPreview(GuiGraphics graphics, int mouseX, int mouseY) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        // Match Create Cybernetics ModelViewer: sync body + head + O-values so the head
        // does not stay locked to world look while the body uses inventory yaw.
        float bodyYaw = player.yBodyRot;
        float bodyYawO = player.yBodyRotO;
        float headYaw = player.yHeadRot;
        float headYawO = player.yHeadRotO;
        float yaw = player.getYRot();
        float yawO = player.yRotO;
        float pitch = player.getXRot();
        float pitchO = player.xRotO;

        player.yBodyRot = 180f;
        player.yBodyRotO = 180f;
        player.yHeadRot = 180f;
        player.yHeadRotO = 180f;
        player.setYRot(180f);
        player.yRotO = 180f;
        player.setXRot(0f);
        player.xRotO = 0f;

        // Same spin quaternion as ModelViewer (rotateX 180°, then yaw), not vanilla Z-flip.
        Quaternionf rotation = new Quaternionf()
                .rotateX((float) Math.PI)
                .rotateY(previewRotation * ((float) Math.PI / 180f));

        int scissorL = chromeX + 8;
        int scissorT = chromeY + 8;
        int scissorR = chromeX + PREVIEW_W - 8;
        int scissorB = chromeY + PREVIEW_H - 8;
        graphics.enableScissor(scissorL, scissorT, scissorR, scissorB);
        try {
            InventoryScreen.renderEntityInInventory(
                    graphics,
                    previewX,
                    previewY,
                    PREVIEW_SCALE,
                    new Vector3f(0f, 0f, 0f),
                    rotation,
                    null,
                    player
            );
        } finally {
            graphics.disableScissor();
            player.yBodyRot = bodyYaw;
            player.yBodyRotO = bodyYawO;
            player.yHeadRot = headYaw;
            player.yHeadRotO = headYawO;
            player.setYRot(yaw);
            player.yRotO = yawO;
            player.setXRot(pitch);
            player.xRotO = pitchO;
        }

        int frameColor = isOverPreview(mouseX, mouseY) ? ACCENT : PANEL_EDGE;
        drawBorder(graphics, chromeX, chromeY, PREVIEW_W, PREVIEW_H, frameColor);
    }

    private static void drawBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public void onClose() {
        ClientVanityConfig.SPEC.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private static void blitToggle(GuiGraphics graphics, boolean on, int x, int y) {
        ResourceLocation tex = on ? ON_TOGGLE : OFF_TOGGLE;
        graphics.blit(tex, x, y, 0, 0, TOGGLE_SIZE, TOGGLE_SIZE, TOGGLE_SIZE, TOGGLE_SIZE);
    }

    private static void playClick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1f, 1f);
        }
    }

    private static String ellipsize(net.minecraft.client.gui.Font font, String text, int maxWidth) {
        if (maxWidth <= 0) {
            return "";
        }
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "…";
        int ellipsisW = font.width(ellipsis);
        if (maxWidth <= ellipsisW) {
            return font.plainSubstrByWidth(text, maxWidth);
        }
        return font.plainSubstrByWidth(text, maxWidth - ellipsisW) + ellipsis;
    }

    private final class MasterToggle extends AbstractWidget {
        private MasterToggle(int x, int y, int width) {
            super(x, y, width, 16, masterLabel());
            this.setTooltip(Tooltip.create(Component.translatable("gui.cyberneticsvanity.vanity_tooltip")));
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            VanityState.toggleVanity();
            setMessage(masterLabel());
            refreshRowActive();
            playClick();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean on = VanityState.isVanityEnabled();
            int labelW = Minecraft.getInstance().font.width(getMessage());
            int total = TOGGLE_SIZE + 6 + labelW;
            int startX = getX() + (getWidth() - total) / 2;
            int ty = getY() + (getHeight() - TOGGLE_SIZE) / 2;
            blitToggle(graphics, on, startX, ty);
            graphics.drawString(
                    Minecraft.getInstance().font,
                    getMessage(),
                    startX + TOGGLE_SIZE + 6,
                    getY() + (getHeight() - 8) / 2,
                    on ? 0x66FFAA : 0xFF8866,
                    false
            );
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
            defaultButtonNarrationText(narration);
        }
    }

    private static Component masterLabel() {
        return Component.translatable(
                VanityState.isVanityEnabled()
                        ? "gui.cyberneticsvanity.vanity_on"
                        : "gui.cyberneticsvanity.vanity_off"
        );
    }

    private final class BulkButton extends AbstractWidget {
        private final Runnable action;

        private BulkButton(int x, int y, int w, int h, Component label, Runnable action) {
            super(x, y, w, h, label);
            this.action = action;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (!VanityState.isVanityEnabled()) {
                return;
            }
            action.run();
            playClick();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean armed = VanityState.isVanityEnabled();
            boolean hover = this.isHoveredOrFocused() && armed;
            int bg = hover ? 0xFF1E4450 : 0xFF141C22;
            int edge = armed ? ACCENT : 0xFF3A5058;
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
            drawBorder(graphics, getX(), getY(), getWidth(), getHeight(), edge);
            int color = armed ? (hover ? 0xE0FFFF : 0xB8DCE4) : 0x55666A;
            graphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    getMessage(),
                    getX() + getWidth() / 2,
                    getY() + (getHeight() - 8) / 2,
                    color
            );
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
            defaultButtonNarrationText(narration);
        }
    }

    private final class DoneButton extends AbstractWidget {
        private DoneButton(int x, int y, int w, int h) {
            super(x, y, w, h, Component.translatable("gui.cyberneticsvanity.menu.done"));
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            playClick();
            onClose();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hover = this.isHoveredOrFocused();
            int bg = hover ? 0xFF1E4450 : 0xFF141C22;
            int edge = hover ? ACCENT : PANEL_EDGE;
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
            drawBorder(graphics, getX(), getY(), getWidth(), getHeight(), edge);

            float alpha = hover ? 1.0f : 0.85f;
            graphics.setColor(1f, 1f, 1f, alpha);
            int iconX = getX() + 4;
            int iconY = getY() + (getHeight() - 10) / 2;
            graphics.blit(BACK_ICON, iconX, iconY, 0, 0, 20, 10, 20, 10);
            graphics.setColor(1f, 1f, 1f, 1f);
            graphics.drawString(
                    Minecraft.getInstance().font,
                    getMessage(),
                    iconX + 22,
                    getY() + (getHeight() - 8) / 2,
                    hover ? 0xE0FFFF : 0xA8C8D0,
                    false
            );
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
            defaultButtonNarrationText(narration);
        }
    }

    private static final class Row extends AbstractWidget {
        private final String key;
        private final ItemStack icon;
        private final Component label;
        private final boolean meta;

        private Row(String key, ItemStack icon, Component label, boolean meta) {
            super(0, 0, 100, ROW_H - 2, label);
            this.key = key;
            this.icon = icon;
            this.label = label;
            this.meta = meta;
            this.setTooltip(Tooltip.create(Component.translatable("gui.cyberneticsvanity.row.tooltip")));
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if (!VanityState.isVanityEnabled()) {
                return;
            }
            VanityState.toggleImplant(key);
            playClick();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean armed = ClientVanityConfig.snapshot().hiddenKeys().contains(key);
            Minecraft mc = Minecraft.getInstance();

            int iconX = getX();
            int iconY = getY() + (getHeight() - ICON_SIZE) / 2;
            int toggleX = getX() + getWidth() - TOGGLE_SIZE - 2;
            int statusRight = toggleX - 4;
            int statusLeft = statusRight - STATUS_COL_W;
            int labelX = iconX + ICON_SIZE + 4;
            int labelMaxW = Math.max(8, statusLeft - labelX - 4);

            ResourceLocation slotTex = this.isHoveredOrFocused() ? SLOT_HOVER : SLOT;
            graphics.blit(slotTex, iconX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            // Brighter well so dark limb item sprites remain readable
            graphics.fill(iconX + 1, iconY + 1, iconX + ICON_SIZE - 1, iconY + ICON_SIZE - 1, ICON_WELL);

            if (!icon.isEmpty()) {
                graphics.renderItem(icon, iconX + 1, iconY + 1);
            } else {
                graphics.fill(iconX + 4, iconY + 4, iconX + ICON_SIZE - 4, iconY + ICON_SIZE - 4, 0xFF40C8E0);
            }

            int textColor = this.active ? 0xD0F0F8 : 0x667788;
            String clipped = ellipsize(mc.font, label.getString(), labelMaxW);
            graphics.drawString(mc.font, clipped, labelX, getY() + (getHeight() - 8) / 2, textColor, false);

            int ty = getY() + (getHeight() - TOGGLE_SIZE) / 2;
            blitToggle(graphics, !armed, toggleX, ty);

            Component state;
            if (meta && VanityKeys.LIMB_HIDING.equals(key)) {
                state = Component.translatable(armed
                        ? "gui.cyberneticsvanity.state.skip"
                        : "gui.cyberneticsvanity.state.normal");
            } else {
                state = Component.translatable(armed
                        ? "gui.cyberneticsvanity.state.hide"
                        : "gui.cyberneticsvanity.state.show");
            }
            int stateW = mc.font.width(state);
            int stateX = statusRight - stateW;
            graphics.drawString(
                    mc.font,
                    state,
                    stateX,
                    ty + 1,
                    armed ? 0xFF8866 : 0x66FFAA,
                    false
            );
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
            defaultButtonNarrationText(narration);
        }
    }
}
