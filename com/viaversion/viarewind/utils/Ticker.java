/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viarewind.utils;

import com.viaversion.viarewind.utils.Tickable;
import com.viaversion.viaversion.api.Via;

public class Ticker {
    private static boolean init = false;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void init() {
        if (init) {
            return;
        }
        Class<Ticker> clazz = Ticker.class;
        synchronized (Ticker.class) {
            if (init) {
                // ** MonitorExit[var0] (shouldn't be in output)
                return;
            }
            init = true;
            // ** MonitorExit[var0] (shouldn't be in output)
            Via.getPlatform().runRepeatingSync(() -> Via.getManager().getConnectionManager().getConnections().forEach(user -> user.getStoredObjects().values().stream().filter(Tickable.class::isInstance).map(Tickable.class::cast).forEach(Tickable::tick)), 1L);
            return;
        }
    }
}

