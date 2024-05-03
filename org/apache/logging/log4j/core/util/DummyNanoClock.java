/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.util;

import org.apache.logging.log4j.core.util.NanoClock;

public final class DummyNanoClock
implements NanoClock {
    private final long fixedNanoTime;

    public DummyNanoClock() {
        this(0L);
    }

    public DummyNanoClock(long fixedNanoTime) {
        this.fixedNanoTime = fixedNanoTime;
    }

    @Override
    public long nanoTime() {
        return this.fixedNanoTime;
    }
}

