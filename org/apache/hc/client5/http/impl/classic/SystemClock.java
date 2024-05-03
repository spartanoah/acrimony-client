/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import org.apache.hc.client5.http.impl.classic.Clock;

class SystemClock
implements Clock {
    SystemClock() {
    }

    @Override
    public long getCurrentTime() {
        return System.currentTimeMillis();
    }
}

