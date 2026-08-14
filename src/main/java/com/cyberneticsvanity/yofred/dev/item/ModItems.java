package com.cyberneticsvanity.yofred.dev.item;

import com.cyberneticsvanity.yofred.dev.CyberneticsVanity;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CyberneticsVanity.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CyberneticsVanity.MODID);

    public static final DeferredHolder<Item, VanityImplantItem> VANITY_IMPLANT = ITEMS.register(
            "vanity_implant",
            () -> new VanityImplantItem(new Item.Properties().stacksTo(1))
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.cyberneticsvanity"))
                    .icon(() -> new ItemStack(VANITY_IMPLANT.get()))
                    .displayItems((params, output) -> output.accept(VANITY_IMPLANT.get()))
                    .build()
    );

    private ModItems() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        CREATIVE_TABS.register(bus);
    }
}
