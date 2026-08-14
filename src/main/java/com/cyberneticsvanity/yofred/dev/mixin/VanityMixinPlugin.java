package com.cyberneticsvanity.yofred.dev.mixin;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/** Lets CPM Visual Bridge take ownership of migrated renderer integrations. */
public final class VanityMixinPlugin implements IMixinConfigPlugin {
    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.endsWith("SandevistanMirageTrailCpmMixin")) return true;
        return !isBridgeLoaded();
    }

    private static boolean isBridgeLoaded() {
        try {
            var list = LoadingModList.get();
            return list != null && list.getModFileById("cpmvisualbridge") != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
