package com.cyberneticsvanity.yofred.dev.loot;

import com.cyberneticsvanity.yofred.dev.CyberneticsVanity;
import com.cyberneticsvanity.yofred.dev.ServerVanityConfig;
import com.cyberneticsvanity.yofred.dev.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * Villagers drop the vanity implant on death (configurable via server.toml).
 */
@EventBusSubscriber(modid = CyberneticsVanity.MODID)
public final class VanityImplantDrops {
    private VanityImplantDrops() {}

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        if (event.getEntity().getType() != EntityType.VILLAGER) {
            return;
        }
        if (!ServerVanityConfig.enableVillagerDrop()) {
            return;
        }

        int looting = 0;
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            var lootingHolder = level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .get(Enchantments.LOOTING);
            if (lootingHolder.isPresent()) {
                looting = EnchantmentHelper.getEnchantmentLevel(lootingHolder.get(), attacker);
            }
        }

        double chance = Math.min(
                ServerVanityConfig.maxDropChance(),
                ServerVanityConfig.villagerDropChance()
                        + looting * ServerVanityConfig.lootingBonusPerLevel()
        );
        if (event.getEntity().getRandom().nextDouble() >= chance) {
            return;
        }

        ItemStack stack = new ItemStack(ModItems.VANITY_IMPLANT.get());
        ItemEntity drop = new ItemEntity(
                level,
                event.getEntity().getX(),
                event.getEntity().getY() + 0.5,
                event.getEntity().getZ(),
                stack
        );
        drop.setDefaultPickUpDelay();
        event.getDrops().add(drop);
    }
}
