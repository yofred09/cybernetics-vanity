package com.cyberneticsvanity.yofred.dev.item;

import com.perigrine3.createcybernetics.item.cyberware.skin.SynthSkinItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
/**
 * Synthskin variant that unlocks Cyber Vanity after surgical install.
 * Acquired as an uncommon villager kill drop (not craftable).
 */
public class VanityImplantItem extends SynthSkinItem {
    public VanityImplantItem(Properties properties) {
        super(properties, 0);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.cyberneticsvanity.vanity_implant").withStyle(ChatFormatting.GRAY));
        // Preserve Synthskin humanity and uploaded-tattoo information.
        super.appendHoverText(stack, context, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.cyberneticsvanity.vanity_implant.install")
                    .withStyle(ChatFormatting.DARK_AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.cyberneticsvanity.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

}
