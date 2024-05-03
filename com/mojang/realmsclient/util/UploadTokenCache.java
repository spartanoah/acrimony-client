/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.mojang.realmsclient.util;

import java.util.HashMap;
import java.util.Map;

public class UploadTokenCache {
    private static Map<Long, String> tokenCache = new HashMap<Long, String>();

    public static String get(long worldId) {
        return tokenCache.get(worldId);
    }

    public static void invalidate(long world) {
        tokenCache.remove(world);
    }

    public static void put(long wid, String token) {
        tokenCache.put(wid, token);
    }
}

