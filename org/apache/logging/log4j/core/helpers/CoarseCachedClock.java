/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.helpers;

import java.util.concurrent.locks.LockSupport;
import org.apache.logging.log4j.core.helpers.Clock;

public final class CoarseCachedClock
implements Clock {
    private static CoarseCachedClock instance = new CoarseCachedClock();
    private volatile long millis = System.currentTimeMillis();
    private final Thread updater = new Thread("Clock Updater Thread"){

        @Override
        public void run() {
            while (true) {
                long time = System.currentTimeMillis();
                CoarseCachedClock.this.millis = time;
                LockSupport.parkNanos(1000000L);
            }
        }
    };

    private CoarseCachedClock() {
        this.updater.setDaemon(true);
        this.updater.start();
    }

    public static CoarseCachedClock instance() {
        return instance;
    }

    @Override
    public long currentTimeMillis() {
        return this.millis;
    }
}

