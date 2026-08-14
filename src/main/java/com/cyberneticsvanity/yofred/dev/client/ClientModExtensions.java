package com.cyberneticsvanity.yofred.dev.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class ClientModExtensions {
    private ClientModExtensions() {}

    public static void register(ModContainer container) {
        IConfigScreenFactory factory = (mod, parent) -> new ModSettingsScreen(parent);
        container.registerExtensionPoint(IConfigScreenFactory.class, factory);
    }
}
