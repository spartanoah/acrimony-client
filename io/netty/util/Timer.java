/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface Timer {
    public Timeout newTimeout(TimerTask var1, long var2, TimeUnit var4);

    public Set<Timeout> stop();
}

