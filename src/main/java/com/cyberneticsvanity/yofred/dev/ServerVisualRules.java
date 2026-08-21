package com.cyberneticsvanity.yofred.dev;

import com.cyberneticsvanity.yofred.dev.network.VanitySync;
import com.perigrine3.createcybernetics.api.CyberwareSlot;
import com.perigrine3.createcybernetics.api.InstalledCyberware;
import com.perigrine3.createcybernetics.common.capabilities.PlayerCyberwareData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * Server-safe visual decisions for effects that Cybernetics spawns server-side.
 * This class deliberately has no references to Minecraft client or renderer classes.
 */
public final class ServerVisualRules {
    private ServerVisualRules() {}

    public static boolean isInstalledItemHidden(
            ServerPlayer player,
            CyberwareSlot slot,
            Predicate<ResourceLocation> itemMatcher
    ) {
        if (player == null || slot == null || itemMatcher == null || !ServerVanityGate.canUse(player)) {
            return false;
        }
        VanitySnapshot snapshot = VanitySync.read(player);
        if (!snapshot.enabled()) {
            return false;
        }
        PlayerCyberwareData data = PlayerCyberwareData.getForVisual(player, player.registryAccess());
        if (data == null) {
            return false;
        }
        boolean foundMatchingItem = false;
        for (int index = 0; index < slot.size; index++) {
            InstalledCyberware installed = data.get(slot, index);
            ItemStack stack = installed == null ? ItemStack.EMPTY : installed.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!itemMatcher.test(itemId)) {
                continue;
            }

            foundMatchingItem = true;
            if (!snapshot.isHidden(VanityKeys.implantKey(slot, index, stack))) {
                // A visible copy still needs the shared server-side visual effect.
                return false;
            }
        }
        return foundMatchingItem;
    }
}
