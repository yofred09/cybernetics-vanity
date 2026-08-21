package com.cyberneticsvanity.yofred.dev;

import com.cyberneticsvanity.yofred.dev.client.VanityKeybinds;
import com.cyberneticsvanity.yofred.dev.compat.CyberSpellsCompat;
import com.cyberneticsvanity.yofred.dev.compat.VisualBridgeCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = CyberneticsVanity.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CyberneticsVanity.MODID, value = Dist.CLIENT)
public class CyberneticsVanityClient {
    public CyberneticsVanityClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(VanityKeybinds::register);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        CyberneticsVanity.LOGGER.info(
                "Cybernetics Vanity client ready (Visual Bridge={} CyberSpells={})",
                VisualBridgeCompat.isLoaded(),
                CyberSpellsCompat.isLoaded()
        );
    }
}
