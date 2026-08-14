package com.cyberneticsvanity.yofred.dev.network;

import com.cyberneticsvanity.yofred.dev.ClientSyncedServerRules;
import com.cyberneticsvanity.yofred.dev.ClientVanityConfig;
import com.cyberneticsvanity.yofred.dev.CyberneticsVanity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class VanitySyncEvents {
    private VanitySyncEvents() {}

    @EventBusSubscriber(modid = CyberneticsVanity.MODID)
    public static final class Server {
        private Server() {}

        @SubscribeEvent
        public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, SyncServerRulesS2CPayload.fromServerConfig());
                VanitySync.broadcastExisting(player);
                // Joiner must receive existing players' vanity even if StartTracking races.
                for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
                    if (other != player) {
                        VanitySync.sendTo(player, other);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onTracking(PlayerEvent.StartTracking event) {
            if (!(event.getEntity() instanceof ServerPlayer tracker)) {
                return;
            }
            if (!(event.getTarget() instanceof Player subject)) {
                return;
            }
            VanitySync.sendTo(tracker, subject);
        }

        @SubscribeEvent
        public static void onClone(PlayerEvent.Clone event) {
            // Death + dimension change both create a new player entity; ForgeData is not
            // auto-copied for our vanity root, so re-apply and rebroadcast for trackers.
            if (!(event.getEntity() instanceof ServerPlayer neu) || !(event.getOriginal() instanceof ServerPlayer original)) {
                return;
            }
            VanitySync.write(neu, VanitySync.read(original));
            VanitySync.broadcastExisting(neu);
        }
    }

    @EventBusSubscriber(modid = CyberneticsVanity.MODID, value = Dist.CLIENT)
    public static final class Client {
        private static int respawnResyncTicks = -1;

        private Client() {}

        @SubscribeEvent
        public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
            ClientSyncedServerRules.clear();
            VanitySync.clearClientCache();
        }

        @SubscribeEvent
        public static void onClone(ClientPlayerNetworkEvent.Clone event) {
            if (ClientSyncedServerRules.serverHasVanityMod()) {
                VanitySync.sendLocalToServer(ClientVanityConfig.snapshot());
                // Repeat once the replacement player and Create-Cybernetics attachments have settled.
                // This closes the death/respawn race where the server clone packet can arrive first.
                respawnResyncTicks = 10;
            }
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            if (respawnResyncTicks < 0 || --respawnResyncTicks > 0) {
                return;
            }
            respawnResyncTicks = -1;
            if (ClientSyncedServerRules.serverHasVanityMod()) {
                VanitySync.sendLocalToServer(ClientVanityConfig.snapshot());
            }
        }

        @SubscribeEvent
        public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            // Ensure toggles hit disk before process teardown / world leave.
            ClientVanityConfig.SPEC.save();
            VanitySync.clearClientCache();
            ClientSyncedServerRules.clear();
            respawnResyncTicks = -1;
        }
    }
}
