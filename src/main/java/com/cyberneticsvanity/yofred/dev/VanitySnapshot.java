package com.cyberneticsvanity.yofred.dev;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Synced vanity preferences for one player.
 * {@code hiddenKeys} use stable implant keys ({@code SLOT:index:namespace:path})
 * plus meta keys ({@link VanityKeys#HIGHLIGHTS}, {@link VanityKeys#LIMB_HIDING}).
 */
public final class VanitySnapshot {
    public static final VanitySnapshot DISABLED = new VanitySnapshot(false, Set.of());

    private final boolean enabled;
    private final Set<String> hiddenKeys;

    public VanitySnapshot(boolean enabled, Set<String> hiddenKeys) {
        this.enabled = enabled;
        this.hiddenKeys = Collections.unmodifiableSet(new LinkedHashSet<>(hiddenKeys));
    }

    public static VanitySnapshot of(boolean enabled, List<String> hiddenKeys) {
        return new VanitySnapshot(enabled, new LinkedHashSet<>(hiddenKeys));
    }

    public boolean enabled() {
        return enabled;
    }

    public Set<String> hiddenKeys() {
        return hiddenKeys;
    }

    public boolean isHidden(String key) {
        return enabled && hiddenKeys.contains(key);
    }

    public VanitySnapshot withEnabled(boolean next) {
        return new VanitySnapshot(next, hiddenKeys);
    }

    public VanitySnapshot withHidden(String key, boolean hidden) {
        LinkedHashSet<String> next = new LinkedHashSet<>(hiddenKeys);
        if (hidden) {
            next.add(key);
        } else {
            next.remove(key);
        }
        return new VanitySnapshot(enabled, next);
    }

    public List<String> hiddenKeyList() {
        return List.copyOf(hiddenKeys);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VanitySnapshot that)) {
            return false;
        }
        return enabled == that.enabled && hiddenKeys.equals(that.hiddenKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, hiddenKeys);
    }
}
