package com.cyberneticsvanity.yofred.dev.client;

import com.cyberneticsvanity.yofred.dev.CyberneticsVanity;
import com.cyberneticsvanity.yofred.dev.VanityState;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class VanityKeybinds {
    public static final KeyMapping OPEN_VANITY_MENU = new KeyMapping(
            "key.cyberneticsvanity.open_menu",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "key.categories.cyberneticsvanity"
    );

    private VanityKeybinds() {}

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_VANITY_MENU);
    }

    @EventBusSubscriber(modid = CyberneticsVanity.MODID, value = Dist.CLIENT)
    public static final class Handler {
        private Handler() {}

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return;
            }

            while (OPEN_VANITY_MENU.consumeClick()) {
                if (mc.screen instanceof VanityConfigScreen) {
                    mc.setScreen(null);
                    continue;
                }
                if (mc.screen != null) {
                    continue;
                }
                if (!VanityState.canUseVanityLocally()) {
                    mc.player.displayClientMessage(
                            Component.translatable("message.cyberneticsvanity.need_implant"),
                            true
                    );
                    continue;
                }
                VanityConfigScreen.open();
            }
        }
    }
}
