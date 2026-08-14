package com.cyberneticsvanity.yofred.dev.client;

import com.cyberneticsvanity.yofred.dev.ClientVanityConfig;
import com.cyberneticsvanity.yofred.dev.ServerVanityConfig;
import com.cyberneticsvanity.yofred.dev.VanitySnapshot;
import com.cyberneticsvanity.yofred.dev.network.SyncServerRulesS2CPayload;
import com.cyberneticsvanity.yofred.dev.network.VanitySync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.PacketDistributor;

/** Configuration entry opened from NeoForge's Mods screen. */
public final class ModSettingsScreen extends Screen {
    private static final int WIDTH = 310;
    private final Screen parent;
    private final boolean canEditServer;

    public ModSettingsScreen(Screen parent) {
        super(Component.translatable("config.cyberneticsvanity.title"));
        this.parent = parent;
        this.canEditServer = Minecraft.getInstance().hasSingleplayerServer();
    }

    @Override
    protected void init() {
        clearWidgets();
        int x = (width - WIDTH) / 2;
        int half = (WIDTH - 6) / 2;
        int y = 36;

        addRenderableWidget(CycleButton.onOffBuilder(ClientVanityConfig.snapshot().enabled())
                .create(x, y, WIDTH, 20, Component.translatable("config.cyberneticsvanity.enabled"),
                        (button, value) -> applyClientEnabled(value)));
        y += 30;

        addRenderableWidget(CycleButton.onOffBuilder(ClientVanityConfig.sandevistanCpmModels())
                .create(x, y, WIDTH, 20,
                        Component.translatable("config.cyberneticsvanity.sandevistan_cpm"),
                        (button, value) -> ClientVanityConfig.SANDEVISTAN_CPM_MODELS.set(value)));
        y += 30;

        if (canEditServer) {
            addRenderableWidget(CycleButton.onOffBuilder(ServerVanityConfig.enableVillagerDrop())
                    .create(x, y, half, 20, Component.translatable("config.cyberneticsvanity.villager_drop"),
                            (button, value) -> ServerVanityConfig.ENABLE_VILLAGER_DROP.set(value)));
            addRenderableWidget(CycleButton.onOffBuilder(ServerVanityConfig.requireVanityImplant())
                    .create(x + half + 6, y, half, 20, Component.translatable("config.cyberneticsvanity.require_implant"),
                            (button, value) -> ServerVanityConfig.REQUIRE_VANITY_IMPLANT.set(value)));
            y += 24;

            addRenderableWidget(new DoubleSlider(x, y, half,
                    "config.cyberneticsvanity.drop_chance", ServerVanityConfig.villagerDropChance(),
                    value -> ServerVanityConfig.VILLAGER_DROP_CHANCE.set(value)));
            addRenderableWidget(new DoubleSlider(x + half + 6, y, half,
                    "config.cyberneticsvanity.looting_bonus", ServerVanityConfig.lootingBonusPerLevel(),
                    value -> ServerVanityConfig.LOOTING_BONUS_PER_LEVEL.set(value)));
            y += 24;

            addRenderableWidget(new DoubleSlider(x, y, half,
                    "config.cyberneticsvanity.max_chance", ServerVanityConfig.maxDropChance(),
                    value -> ServerVanityConfig.MAX_DROP_CHANCE.set(value)));
            addRenderableWidget(new PermissionSlider(x + half + 6, y, half));
            y += 24;

            addRenderableWidget(CycleButton.onOffBuilder(ServerVanityConfig.preserveCorpseAppearance())
                    .create(x, y, half, 20, Component.translatable("config.cyberneticsvanity.preserve_corpse"),
                            (button, value) -> ServerVanityConfig.PRESERVE_CORPSE_APPEARANCE.set(value)));
            addRenderableWidget(CycleButton.onOffBuilder(ServerVanityConfig.hideMissingLimbs())
                    .create(x + half + 6, y, half, 20, Component.translatable("config.cyberneticsvanity.hide_limbs"),
                            (button, value) -> ServerVanityConfig.HIDE_MISSING_LIMBS.set(value)));
            y += 32;
        } else {
            y += 42;
        }

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(x + (WIDTH - 100) / 2, y, 100, 20).build());
    }

    private static void applyClientEnabled(boolean enabled) {
        VanitySnapshot old = ClientVanityConfig.snapshot();
        VanitySnapshot next = new VanitySnapshot(enabled, old.hiddenKeys());
        ClientVanityConfig.apply(next);
        VanitySync.sendLocalToServer(next);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 14, 0xFFFFFF);
        Component section = Component.translatable("config.cyberneticsvanity.server_section");
        graphics.drawCenteredString(font, section, width / 2, 88, 0xA0DDE8);
        if (!canEditServer) {
            graphics.drawCenteredString(font,
                    Component.translatable("config.cyberneticsvanity.server_read_only"),
                    width / 2, 112, 0xFFAA66);
        }
    }

    @Override
    public void onClose() {
        ClientVanityConfig.SPEC.save();
        if (canEditServer) {
            ServerVanityConfig.SPEC.save();
            MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server != null) {
                server.execute(() -> {
                    SyncServerRulesS2CPayload payload = SyncServerRulesS2CPayload.fromServerConfig();
                    for (var player : server.getPlayerList().getPlayers()) {
                        PacketDistributor.sendToPlayer(player, payload);
                    }
                });
            }
        }
        Minecraft.getInstance().setScreen(parent);
    }

    @FunctionalInterface
    private interface DoubleSetter { void set(double value); }

    private static final class DoubleSlider extends AbstractSliderButton {
        private final String key;
        private final DoubleSetter setter;

        private DoubleSlider(int x, int y, int width, String key, double value, DoubleSetter setter) {
            super(x, y, width, 20, Component.empty(), value);
            this.key = key;
            this.setter = setter;
            updateMessage();
        }

        @Override protected void updateMessage() {
            setMessage(Component.translatable(key).append(": " + String.format(java.util.Locale.ROOT, "%.3f", value)));
        }

        @Override protected void applyValue() { setter.set(value); }
    }

    private static final class PermissionSlider extends AbstractSliderButton {
        private PermissionSlider(int x, int y, int width) {
            super(x, y, width, 20, Component.empty(), ServerVanityConfig.allowedPermissionLevel() / 4.0);
            updateMessage();
        }

        private int level() { return (int) Math.round(value * 4.0); }
        @Override protected void updateMessage() {
            setMessage(Component.translatable("config.cyberneticsvanity.permission").append(": " + level()));
        }
        @Override protected void applyValue() { ServerVanityConfig.ALLOWED_PERMISSION_LEVEL.set(level()); }
    }
}
