package com.cyberneticsvanity.yofred.dev.network;

import com.cyberneticsvanity.yofred.dev.CyberneticsVanity;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class VanityNetwork {
    private VanityNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(CyberneticsVanity.MODID).versioned("2");
        registrar.playToServer(
                UpdateVanityC2SPayload.TYPE,
                UpdateVanityC2SPayload.STREAM_CODEC,
                UpdateVanityC2SPayload::handle
        );
        registrar.playToClient(
                SyncVanityS2CPayload.TYPE,
                SyncVanityS2CPayload.STREAM_CODEC,
                SyncVanityS2CPayload::handle
        );
        registrar.playToClient(
                SyncServerRulesS2CPayload.TYPE,
                SyncServerRulesS2CPayload.STREAM_CODEC,
                SyncServerRulesS2CPayload::handle
        );
    }
}
