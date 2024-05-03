/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util;

public interface ResourceLeak {
    public void record();

    public boolean close();
}

