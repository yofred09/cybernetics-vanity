package com.cyberneticsvanity.yofred.dev.network;

import com.cyberneticsvanity.yofred.dev.CyberneticsVanity;
import com.cyberneticsvanity.yofred.dev.VanitySnapshot;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

/** Server → clients: vanity state for a specific player. */
public record SyncVanityS2CPayload(UUID playerId, boolean enabled, List<String> hiddenKeys) implements CustomPacketPayload {
    public static final Type<SyncVanityS2CPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CyberneticsVanity.MODID, "sync_vanity_s2c")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncVanityS2CPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SyncVanityS2CPayload::playerId,
            ByteBufCodecs.BOOL, SyncVanityS2CPayload::enabled,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), SyncVanityS2CPayload::hiddenKeys,
            SyncVanityS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static SyncVanityS2CPayload from(UUID playerId, VanitySnapshot snap) {
        return new SyncVanityS2CPayload(playerId, snap.enabled(), snap.hiddenKeyList());
    }

    public static void handle(SyncVanityS2CPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> VanitySync.applyClientSync(
                payload.playerId(),
                VanitySnapshot.of(payload.enabled(), payload.hiddenKeys())
        ));
    }
}
