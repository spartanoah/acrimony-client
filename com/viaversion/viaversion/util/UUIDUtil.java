/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.util;

import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class UUIDUtil {
    public static UUID fromIntArray(int[] parts) {
        if (parts.length != 4) {
            return new UUID(0L, 0L);
        }
        return new UUID((long)parts[0] << 32 | (long)parts[1] & 0xFFFFFFFFL, (long)parts[2] << 32 | (long)parts[3] & 0xFFFFFFFFL);
    }

    public static int[] toIntArray(UUID uuid) {
        return UUIDUtil.toIntArray(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    public static int[] toIntArray(long msb, long lsb) {
        return new int[]{(int)(msb >> 32), (int)msb, (int)(lsb >> 32), (int)lsb};
    }

    public static @Nullable UUID parseUUID(String uuidString) {
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

