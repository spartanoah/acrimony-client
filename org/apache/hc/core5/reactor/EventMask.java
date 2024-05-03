/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.reactor;

public final class EventMask {
    public static final int READ = 1;
    public static final int WRITE = 4;
    public static final int READ_WRITE = 5;

    private EventMask() {
    }
}

