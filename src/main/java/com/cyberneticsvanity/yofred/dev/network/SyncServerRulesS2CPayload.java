package com.cyberneticsvanity.yofred.dev.network;

import com.cyberneticsvanity.yofred.dev.ClientSyncedServerRules;
import com.cyberneticsvanity.yofred.dev.ClientVanityConfig;
import com.cyberneticsvanity.yofred.dev.CyberneticsVanity;
import com.cyberneticsvanity.yofred.dev.ServerVanityConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server → client: staff config flags that affect menu gating. */
public record SyncServerRulesS2CPayload(boolean requireVanityImplant, int allowedPermissionLevel,
                                        boolean hideMissingLimbs)
        implements CustomPacketPayload {
    public static final Type<SyncServerRulesS2CPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CyberneticsVanity.MODID, "sync_server_rules_s2c")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncServerRulesS2CPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncServerRulesS2CPayload::requireVanityImplant,
                    ByteBufCodecs.VAR_INT, SyncServerRulesS2CPayload::allowedPermissionLevel,
                    ByteBufCodecs.BOOL, SyncServerRulesS2CPayload::hideMissingLimbs,
                    SyncServerRulesS2CPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static SyncServerRulesS2CPayload fromServerConfig() {
        return new SyncServerRulesS2CPayload(
                ServerVanityConfig.requireVanityImplant(),
                ServerVanityConfig.allowedPermissionLevel(),
                ServerVanityConfig.hideMissingLimbs()
        );
    }

    public static void handle(SyncServerRulesS2CPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientSyncedServerRules.apply(
                    payload.requireVanityImplant(),
                    payload.allowedPermissionLevel(),
                    payload.hideMissingLimbs()
            );
            VanitySync.sendLocalToServer(ClientVanityConfig.snapshot());
        });
    }
}
