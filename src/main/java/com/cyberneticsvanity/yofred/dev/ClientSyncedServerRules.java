package com.cyberneticsvanity.yofred.dev;

/**
 * Client cache of server rules that gate menu / vanity usage.
 * {@link #hasReceivedSync()} is also the handshake that the dedicated / integrated server runs
 * Cybernetics Vanity — until then, stay client-local and do not send C2S vanity packets.
 */
public final class ClientSyncedServerRules {
    private static boolean requireVanityImplant = true;
    private static int allowedPermissionLevel = 0;
    private static boolean hideMissingLimbs;
    private static boolean received;

    private ClientSyncedServerRules() {}

    public static void apply(boolean requireImplant, int permissionLevel, boolean hideLimbs) {
        requireVanityImplant = requireImplant;
        allowedPermissionLevel = Math.max(0, Math.min(4, permissionLevel));
        hideMissingLimbs = hideLimbs;
        received = true;
    }

    public static void clear() {
        requireVanityImplant = true;
        allowedPermissionLevel = 0;
        hideMissingLimbs = false;
        received = false;
    }

    public static boolean requireVanityImplant() {
        return requireVanityImplant;
    }

    public static int allowedPermissionLevel() {
        return allowedPermissionLevel;
    }

    public static boolean hideMissingLimbs() {
        return hideMissingLimbs;
    }

    public static boolean hasReceivedSync() {
        return received;
    }

    /** True when the connected server runs Cybernetics Vanity (rules packet received). */
    public static boolean serverHasVanityMod() {
        return received;
    }

    /** Client-only vanity on servers without this mod; no server implant / permission gates. */
    public static boolean isClientOnlyMode() {
        return !received;
    }
}
