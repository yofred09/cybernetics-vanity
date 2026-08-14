package com.cyberneticsvanity.yofred.dev;

import com.cyberneticsvanity.yofred.dev.client.VanityKeybinds;
import com.cyberneticsvanity.yofred.dev.compat.CpmCompat;
import com.cyberneticsvanity.yofred.dev.compat.CyberSpellsCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

@Mod(value = CyberneticsVanity.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CyberneticsVanity.MODID, value = Dist.CLIENT)
public class CyberneticsVanityClient {
    public CyberneticsVanityClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(VanityKeybinds::register);
        modEventBus.addListener(CyberneticsVanityClient::enqueueCpmCompat);
    }

    private static void enqueueCpmCompat(InterModEnqueueEvent event) {
        CpmCompat.bootstrap();
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        CpmCompat.bootstrap();
        CyberneticsVanity.LOGGER.info(
                "Cybernetics Vanity client ready (keybind menu + synced implant toggles; CPM soft-compat={}; CyberSpells soft-compat={})",
                CpmCompat.isLoaded(),
                CyberSpellsCompat.isLoaded()
        );
    }
}
