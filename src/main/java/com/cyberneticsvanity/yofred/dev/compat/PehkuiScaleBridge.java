package com.cyberneticsvanity.yofred.dev.compat;

import com.cyberneticsvanity.yofred.dev.CyberneticsVanity;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

/** Optional reflective Pehkui bridge safe to call when Pehkui is not installed. */
public final class PehkuiScaleBridge {
    private static boolean initialized;
    private static boolean available;
    private static Method widthMethod;
    private static Method heightMethod;

    private PehkuiScaleBridge() {}

    public static float modelWidth(Entity entity, float partialTick) {
        return invoke(widthMethod(), entity, partialTick);
    }

    public static float modelHeight(Entity entity, float partialTick) {
        return invoke(heightMethod(), entity, partialTick);
    }

    private static Method widthMethod() {
        initialize();
        return widthMethod;
    }

    private static Method heightMethod() {
        initialize();
        return heightMethod;
    }

    private static synchronized void initialize() {
        if (initialized) return;
        initialized = true;
        if (!ModList.get().isLoaded("pehkui")) return;
        try {
            Class<?> scaleUtils = Class.forName("virtuoel.pehkui.util.ScaleUtils");
            widthMethod = scaleUtils.getMethod("getModelWidthScale", Entity.class, float.class);
            heightMethod = scaleUtils.getMethod("getModelHeightScale", Entity.class, float.class);
            available = true;
        } catch (ReflectiveOperationException | LinkageError error) {
            CyberneticsVanity.LOGGER.warn("Could not initialize optional Pehkui scale bridge", error);
        }
    }

    private static float invoke(Method method, Entity entity, float partialTick) {
        if (!available || method == null || entity == null) return 1.0F;
        try {
            Object result = method.invoke(null, entity, partialTick);
            return result instanceof Number number ? number.floatValue() : 1.0F;
        } catch (ReflectiveOperationException | LinkageError error) {
            available = false;
            CyberneticsVanity.LOGGER.warn("Disabling Pehkui scale bridge after a render failure", error);
            return 1.0F;
        }
    }
}
