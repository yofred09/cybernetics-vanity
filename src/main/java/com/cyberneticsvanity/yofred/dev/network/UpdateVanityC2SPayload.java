package com.cyberneticsvanity.yofred.dev.network;

import com.cyberneticsvanity.yofred.dev.CyberneticsVanity;
import com.cyberneticsvanity.yofred.dev.ServerVanityGate;
import com.cyberneticsvanity.yofred.dev.VanitySnapshot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/** Client → server: full vanity preference push. */
public record UpdateVanityC2SPayload(boolean enabled, List<String> hiddenKeys) implements CustomPacketPayload {
    public static final Type<UpdateVanityC2SPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CyberneticsVanity.MODID, "update_vanity_c2s")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateVanityC2SPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, UpdateVanityC2SPayload::enabled,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), UpdateVanityC2SPayload::hiddenKeys,
            UpdateVanityC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateVanityC2SPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sender)) {
                return;
            }
            // Must use ServerVanityGate — VanityState pulls client classes and crashes dedicated.
            if (!ServerVanityGate.canUse(sender)) {
                return;
            }
            List<String> cleaned = sanitize(payload.hiddenKeys());
            VanitySnapshot snap = VanitySnapshot.of(payload.enabled(), cleaned);
            VanitySync.storeAndBroadcast(sender, snap);
        });
    }

    private static List<String> sanitize(List<String> keys) {
        List<String> out = new ArrayList<>();
        if (keys == null) {
            return out;
        }
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            String trimmed = key.trim();
            if (trimmed.isEmpty() || trimmed.length() > 256) {
                continue;
            }
            if (out.size() >= 256) {
                break;
            }
            out.add(trimmed);
        }
        return out;
    }
}
