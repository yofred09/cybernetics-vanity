package com.cyberneticsvanity.yofred.dev.compat;

import com.cyberneticsvanity.yofred.dev.CyberneticsVanity;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.InterModComms;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Isolated CPM soft-compat bridge. Loaded only via {@link Class#forName} from
 * {@link CpmCompat} when CPM is present — never referenced from common server paths
 * at class-init time for CPM types (all CPM access is reflective).
 * <p>
 * Registers a no-op {@code ICPMPlugin} via IMC when possible; probes optional API
 * entry points. Any failure disables API features once and never crashes.
 */
public final class CpmSoftBridge {
    private static final AtomicBoolean INIT_DONE = new AtomicBoolean(false);
    private static final AtomicBoolean PLUGIN_LOGGED = new AtomicBoolean(false);

    private static volatile boolean apiAvailable;
    private static volatile Method hasCustomModelMethod;
    private static volatile Object hasCustomModelTarget;
    private static volatile Object playerRenderer;
    private static volatile Method rendererSetGameProfile;
    private static volatile Method rendererSetRenderModel;
    private static volatile Method rendererSetRenderType;
    private static volatile Method rendererGetDefaultRenderType;
    private static volatile Method rendererGetDefaultTexture;
    private static volatile Method rendererPreRender;
    private static volatile Method rendererPostRender;
    private static volatile Method rendererGetAnimationState;
    private static volatile Field animationSyncState;
    private static volatile Constructor<?> serverAnimationStateConstructor;
    private static volatile Object playerAnimationMode;
    private static volatile boolean definitionProbeAttempted;
    private static volatile Object definitionLoader;
    private static volatile Method definitionLoadPlayer;
    private static volatile Method definitionGetModel;
    private static volatile Method definitionDoRender;

    private CpmSoftBridge() {}

    /** Invoked reflectively from {@link CpmCompat#bootstrap()}. */
    public static void init() {
        if (!INIT_DONE.compareAndSet(false, true)) {
            return;
        }
        try {
            probeApi();
            registerPluginViaImc();
            CyberneticsVanity.LOGGER.info(
                    "CPM soft bridge ready (apiAvailable={}, plugin IMC attempted)",
                    apiAvailable
            );
        } catch (Throwable t) {
            CpmCompat.disableApiFeatures("CpmSoftBridge.init", t);
        }
    }

    public static boolean isApiAvailable() {
        return apiAvailable;
    }

    /**
     * @return {@link Boolean#TRUE}/{@link Boolean#FALSE} when probe succeeds,
     *         {@code null} when unknown / unavailable
     */
    public static Boolean hasCustomModelSafe() {
        Method method = hasCustomModelMethod;
        if (method == null) {
            return null;
        }
        try {
            Object result = method.invoke(hasCustomModelTarget);
            if (result instanceof Boolean b) {
                return b;
            }
            return null;
        } catch (Throwable t) {
            CpmCompat.disableApiFeatures("hasCustomModelSafe", t);
            hasCustomModelMethod = null;
            hasCustomModelTarget = null;
            return null;
        }
    }

    /**
     * Detects whether CPM resolves a profile to a custom texture/model.
     * A null result means the CPM renderer API is not ready and callers should
     * choose their safest fallback.
     */
    public static Boolean hasCustomModelSafe(AbstractClientPlayer player) {
        if (player == null) {
            return null;
        }
        try {
            initializeDefinitionProbe();
            if (definitionLoader != null && definitionLoadPlayer != null
                    && definitionGetModel != null && definitionDoRender != null) {
                Object cpmPlayer = definitionLoadPlayer.invoke(
                        definitionLoader, player.getGameProfile(), "cyberneticsvanity_probe"
                );
                if (cpmPlayer == null) return false;
                Object definition = definitionGetModel.invoke(cpmPlayer);
                if (definition == null) return false;
                Object renders = definitionDoRender.invoke(definition);
                return renders instanceof Boolean value ? value : true;
            }
            if (playerRenderer == null || rendererGetDefaultTexture == null) {
                return null;
            }
            rendererSetGameProfile.invoke(playerRenderer, player.getGameProfile());
            Object texture = rendererGetDefaultTexture.invoke(playerRenderer);
            if (!(texture instanceof ResourceLocation resolved)) {
                return null;
            }
            return !resolved.equals(player.getSkin().texture());
        } catch (Throwable t) {
            CpmCompat.disableApiFeatures("profile custom-model probe", t);
            return null;
        }
    }

    private static synchronized void initializeDefinitionProbe() {
        if (definitionProbeAttempted) return;
        definitionProbeAttempted = true;
        try {
            Class<?> accessType = Class.forName("com.tom.cpm.shared.MinecraftClientAccess");
            Object access = accessType.getMethod("get").invoke(null);
            Object loader = accessType.getMethod("getDefinitionLoader").invoke(access);
            Method loadPlayer = loader.getClass().getMethod("loadPlayer", Object.class, String.class);
            Class<?> playerType = Class.forName("com.tom.cpm.shared.config.Player");
            Method getDefinition = playerType.getMethod("getModelDefinition");
            Class<?> definitionType = Class.forName("com.tom.cpm.shared.definition.ModelDefinition");
            Method doRender = definitionType.getMethod("doRender");
            definitionLoader = loader;
            definitionLoadPlayer = loadPlayer;
            definitionGetModel = getDefinition;
            definitionDoRender = doRender;
        } catch (Throwable t) {
            CyberneticsVanity.LOGGER.debug("CPM profile definition probe unavailable: {}", t.toString());
        }
    }

    public static boolean renderPlayerModelSafe(
            AbstractClientPlayer player,
            PlayerModel<?> model,
            PoseStack poseStack,
            VertexConsumer fallbackConsumer,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay,
            int color
    ) {
        if (player == null || model == null || buffers == null || playerRenderer == null) {
            return false;
        }
        boolean preRendered = false;
        Object activeRenderer = playerRenderer;
        try {
            rendererSetGameProfile.invoke(activeRenderer, player.getGameProfile());
            rendererSetRenderModel.invoke(activeRenderer, model);
            rendererSetRenderType.invoke(
                    activeRenderer,
                    (java.util.function.Function<ResourceLocation, RenderType>) RenderType::entityTranslucent
            );
            // Auxiliary renderers created through CPM's API do not always get a
            // network animation state. CPM's PLAYER pose resolver dereferences
            // it unconditionally, so seed the same harmless default state used
            // before the first animation sync packet arrives.
            Object animationState = rendererGetAnimationState.invoke(activeRenderer);
            if (animationState != null && animationSyncState.get(animationState) == null) {
                animationSyncState.set(animationState, serverAnimationStateConstructor.newInstance());
            }
            rendererPreRender.invoke(activeRenderer, buffers, playerAnimationMode);
            preRendered = true;
            // CPM may have a valid model definition before it has assigned a
            // default texture for this auxiliary render. Calling
            // getDefaultRenderType() in that state passes null to RenderType
            // and throws. The original player-skin consumer remains a valid
            // fallback while CPM still supplies the model transforms/cubes.
            Object defaultTexture = rendererGetDefaultTexture.invoke(activeRenderer);
            Object cpmRenderType = defaultTexture == null
                    ? null
                    : rendererGetDefaultRenderType.invoke(playerRenderer);
            VertexConsumer consumer = cpmRenderType instanceof RenderType renderType
                    ? buffers.getBuffer(renderType)
                    : fallbackConsumer;
            model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, color);
            return true;
        } catch (Throwable t) {
            playerRenderer = null;
            CpmCompat.disableApiFeatures("CPM auxiliary player render", t);
            return false;
        } finally {
            if (preRendered) {
                try {
                    rendererPostRender.invoke(activeRenderer);
                } catch (Throwable t) {
                    CpmCompat.disableApiFeatures("CPM postRender", t);
                }
            }
        }
    }

    private static void acceptClientApi(Object api) {
        if (api == null || playerRenderer != null) {
            return;
        }
        try {
            Method createRenderer = api.getClass().getMethod(
                    "createPlayerRenderer",
                    Class.class,
                    Class.class,
                    Class.class,
                    Class.class,
                    Class.class
            );
            Object renderer = createRenderer.invoke(
                    api,
                    Model.class,
                    ResourceLocation.class,
                    RenderType.class,
                    MultiBufferSource.class,
                    GameProfile.class
            );
            Class<?> rendererType = Class.forName(
                    "com.tom.cpm.api.IClientAPI$PlayerRenderer",
                    false,
                    CpmSoftBridge.class.getClassLoader()
            );
            Class<?> animationModeType = Class.forName(
                    "com.tom.cpm.shared.animation.AnimationEngine$AnimationMode",
                    false,
                    CpmSoftBridge.class.getClassLoader()
            );
            @SuppressWarnings({"rawtypes", "unchecked"})
            Object animationMode = Enum.valueOf(
                    (Class<? extends Enum>) animationModeType.asSubclass(Enum.class),
                    "PLAYER"
            );

            rendererSetGameProfile = rendererType.getMethod("setGameProfile", Object.class);
            rendererSetRenderModel = rendererType.getMethod("setRenderModel", Object.class);
            rendererSetRenderType = rendererType.getMethod("setRenderType", java.util.function.Function.class);
            rendererGetDefaultRenderType = rendererType.getMethod("getDefaultRenderType");
            rendererGetDefaultTexture = rendererType.getMethod("getDefaultTexture");
            rendererPreRender = rendererType.getMethod("preRender", Object.class, animationModeType);
            rendererPostRender = rendererType.getMethod("postRender");
            rendererGetAnimationState = rendererType.getMethod("getAnimationState");
            Class<?> animationStateType = Class.forName(
                    "com.tom.cpm.shared.animation.AnimationState", false, CpmSoftBridge.class.getClassLoader()
            );
            Class<?> serverAnimationStateType = Class.forName(
                    "com.tom.cpm.shared.animation.ServerAnimationState", false, CpmSoftBridge.class.getClassLoader()
            );
            animationSyncState = animationStateType.getField("syncState");
            serverAnimationStateConstructor = serverAnimationStateType.getConstructor();
            playerAnimationMode = animationMode;
            playerRenderer = renderer;
            CyberneticsVanity.LOGGER.info("CPM player renderer ready for auxiliary Create-Cybernetics effects");
        } catch (Throwable t) {
            playerRenderer = null;
            CpmCompat.disableApiFeatures("create CPM player renderer", t);
        }
    }

    private static void probeApi() {
        String[] classNames = {
                "com.tom.cpm.api.CPMApiManager",
                "com.tom.cpm.shared.MinecraftClientObject",
                "com.tom.cpm.client.ClientProxy",
                "com.tom.cpm.CustomPlayerModels"
        };
        for (String name : classNames) {
            try {
                Class<?> clazz = Class.forName(name, false, CpmSoftBridge.class.getClassLoader());
                apiAvailable = true;
                for (Method method : clazz.getMethods()) {
                    String m = method.getName().toLowerCase();
                    if (!(m.contains("custommodel") || m.contains("hasmodel") || m.contains("iscustom"))) {
                        continue;
                    }
                    if (method.getParameterCount() != 0) {
                        continue;
                    }
                    if (method.getReturnType() != boolean.class && method.getReturnType() != Boolean.class) {
                        continue;
                    }
                    hasCustomModelMethod = method;
                    hasCustomModelTarget = java.lang.reflect.Modifier.isStatic(method.getModifiers())
                            ? null
                            : trySingleton(clazz);
                    return;
                }
                // Class exists even if no boolean probe method — API surface is still "available".
                return;
            } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                // try next
            } catch (Throwable t) {
                CyberneticsVanity.LOGGER.debug("CPM probe skipped for {}: {}", name, t.toString());
            }
        }
        apiAvailable = false;
    }

    private static Object trySingleton(Class<?> clazz) {
        try {
            for (String field : new String[]{"INSTANCE", "instance", "getInstance"}) {
                try {
                    if (field.startsWith("get")) {
                        Method m = clazz.getMethod(field);
                        if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 0) {
                            return m.invoke(null);
                        }
                    } else {
                        var f = clazz.getField(field);
                        if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                            return f.get(null);
                        }
                    }
                } catch (ReflectiveOperationException ignored) {
                    // continue
                }
            }
        } catch (Throwable ignored) {
            // ignore
        }
        return null;
    }

    /**
     * Safe IMC registration for Forge/NeoForge CPM ({@code cpm} / legacy id).
     * Supplies a reflective JDK proxy implementing {@code ICPMPlugin} so we never
     * compile against CPM classes.
     */
    private static void registerPluginViaImc() {
        try {
            Class<?> pluginIface = Class.forName(
                    "com.tom.cpm.api.ICPMPlugin",
                    false,
                    CpmSoftBridge.class.getClassLoader()
            );
            Object plugin = Proxy.newProxyInstance(
                    CpmSoftBridge.class.getClassLoader(),
                    new Class<?>[]{pluginIface},
                    (proxy, method, args) -> {
                        String name = method.getName();
                        if ("getOwnerModId".equals(name)) {
                            return CyberneticsVanity.MODID;
                        }
                        if ("initClient".equals(name) || "initCommon".equals(name)) {
                            if ("initClient".equals(name) && args != null && args.length > 0) {
                                acceptClientApi(args[0]);
                            }
                            if (PLUGIN_LOGGED.compareAndSet(false, true)) {
                                CyberneticsVanity.LOGGER.info(
                                        "CPM ICPMPlugin callback {} — vanity uses soft limb-skip only",
                                        name
                                );
                            }
                            return null;
                        }
                        if ("toString".equals(name)) {
                            return "CyberneticsVanityCpmPlugin";
                        }
                        if ("hashCode".equals(name)) {
                            return System.identityHashCode(proxy);
                        }
                        if ("equals".equals(name)) {
                            return proxy == args[0];
                        }
                        Class<?> rt = method.getReturnType();
                        if (rt == boolean.class) {
                            return false;
                        }
                        if (rt == int.class) {
                            return 0;
                        }
                        if (rt == long.class) {
                            return 0L;
                        }
                        if (rt == float.class) {
                            return 0f;
                        }
                        if (rt == double.class) {
                            return 0d;
                        }
                        return null;
                    }
            );

            Supplier<?> pluginSupplier = () -> plugin;
            // Nested supplier form expected by CPM Forge/NeoForge IMC ("api").
            Supplier<Supplier<?>> imcPayload = () -> pluginSupplier;

            boolean sent = false;
            try {
                sent |= InterModComms.sendTo(CpmCompat.MOD_ID_CPM, "api", imcPayload);
            } catch (Throwable t) {
                CyberneticsVanity.LOGGER.debug("IMC to cpm failed: {}", t.toString());
            }
            try {
                sent |= InterModComms.sendTo(CpmCompat.MOD_ID_LEGACY, "api", imcPayload);
            } catch (Throwable t) {
                CyberneticsVanity.LOGGER.debug("IMC to customplayermodels failed: {}", t.toString());
            }
            if (sent) {
                CyberneticsVanity.LOGGER.info("Registered CPM soft-compat plugin via InterModComms");
            } else {
                CyberneticsVanity.LOGGER.debug(
                        "CPM IMC send returned false (mod may use @CPMPlugin-only discovery); limb-skip still active"
                );
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            CyberneticsVanity.LOGGER.debug("ICPMPlugin not on classpath; skipping IMC plugin registration");
        } catch (Throwable t) {
            CpmCompat.disableApiFeatures("registerPluginViaImc", t);
        }
    }
}
