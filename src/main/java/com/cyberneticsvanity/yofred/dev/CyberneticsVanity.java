package com.cyberneticsvanity.yofred.dev;

import com.cyberneticsvanity.yofred.dev.compat.CpmCompat;
import com.cyberneticsvanity.yofred.dev.compat.CyberSpellsCompat;
import com.cyberneticsvanity.yofred.dev.item.ModItems;
import com.cyberneticsvanity.yofred.dev.network.VanityNetwork;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(CyberneticsVanity.MODID)
public class CyberneticsVanity {
    public static final String MODID = "cyberneticsvanity";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CyberneticsVanity(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(VanityNetwork::register);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientVanityConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerVanityConfig.SPEC);
        if (FMLEnvironment.dist.isClient()) {
            com.cyberneticsvanity.yofred.dev.client.ClientModExtensions.register(modContainer);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info(
                "Cybernetics Vanity loaded (server config + implant-gated synced vanity; CPM={} CyberSpells={})",
                CpmCompat.isLoaded(),
                CyberSpellsCompat.isLoaded()
        );
    }
}
