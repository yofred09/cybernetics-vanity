package com.cyberneticsvanity.yofred.dev.network;

import com.cyberneticsvanity.yofred.dev.ClientSyncedServerRules;
import com.cyberneticsvanity.yofred.dev.ServerVanityGate;
import com.cyberneticsvanity.yofred.dev.VanitySnapshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server persistence + client cache for vanity sync.
 */
public final class VanitySync {
    private static final String ROOT = "cyberneticsvanity";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_HIDDEN = "hidden";

    private static final Map<UUID, VanitySnapshot> CLIENT_CACHE = new ConcurrentHashMap<>();

    private VanitySync() {}

    public static VanitySnapshot clientSnapshot(UUID playerId) {
        return CLIENT_CACHE.getOrDefault(playerId, VanitySnapshot.DISABLED);
    }

    public static void applyClientSync(UUID playerId, VanitySnapshot snapshot) {
        CLIENT_CACHE.put(playerId, snapshot);
    }

    public static void clearClientCache() {
        CLIENT_CACHE.clear();
    }

    public static void sendLocalToServer(VanitySnapshot snapshot) {
        if (!ClientSyncedServerRules.serverHasVanityMod()) {
            return;
        }
        PacketDistributor.sendToServer(new UpdateVanityC2SPayload(snapshot.enabled(), snapshot.hiddenKeyList()));
    }

    public static VanitySnapshot read(Player player) {
        CompoundTag root = player.getPersistentData().getCompound(ROOT);
        boolean enabled = root.getBoolean(KEY_ENABLED);
        List<String> hidden = new ArrayList<>();
        ListTag list = root.getList(KEY_HIDDEN, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            hidden.add(list.getString(i));
        }
        return VanitySnapshot.of(enabled, hidden);
    }

    public static void write(Player player, VanitySnapshot snapshot) {
        CompoundTag root = new CompoundTag();
        root.putBoolean(KEY_ENABLED, snapshot.enabled());
        ListTag list = new ListTag();
        for (String key : snapshot.hiddenKeyList()) {
            list.add(StringTag.valueOf(key));
        }
        root.put(KEY_HIDDEN, list);
        player.getPersistentData().put(ROOT, root);
    }

    public static void storeAndBroadcast(ServerPlayer player, VanitySnapshot snapshot) {
        write(player, snapshot);
        SyncVanityS2CPayload payload = SyncVanityS2CPayload.from(player.getUUID(), snapshot);
        // Self + everyone currently tracking this player (LAN + dedicated).
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, payload);
    }

    public static void sendTo(ServerPlayer target, Player subject) {
        VanitySnapshot snap = effectiveSnapshot(subject);
        PacketDistributor.sendToPlayer(target, SyncVanityS2CPayload.from(subject.getUUID(), snap));
    }

    public static void broadcastExisting(ServerPlayer player) {
        VanitySnapshot snap = effectiveSnapshot(player);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                player,
                SyncVanityS2CPayload.from(player.getUUID(), snap)
        );
    }

    /**
     * Keep saved preferences intact, but never expose them as active while the
     * player fails the server's current implant or permission requirements.
     */
    private static VanitySnapshot effectiveSnapshot(Player player) {
        return ServerVanityGate.canUse(player) ? read(player) : VanitySnapshot.DISABLED;
    }
}
