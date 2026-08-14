package com.cyberneticsvanity.yofred.dev.mixin.compat;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/** Keeps every Pehkui API reference outside class loading when the optional mod is absent. */
public final class PehkuiMixinPlugin implements IMixinConfigPlugin {
    private boolean loaded;

    @Override public void onLoad(String mixinPackage) {
        try {
            var list = LoadingModList.get();
            loaded = list != null && list.getModFileById("pehkui") != null;
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
