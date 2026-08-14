package com.cyberneticsvanity.yofred.dev.mixin.compat;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/** Applies Corpse compatibility only when the optional Corpse mod is installed. */
public final class CorpseMixinPlugin implements IMixinConfigPlugin {
    private boolean loaded;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            var list = LoadingModList.get();
            loaded = list != null && list.getModFileById("corpse") != null;
        } catch (Throwable ignored) {
            loaded = false;
        }
    }

    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return loaded; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
