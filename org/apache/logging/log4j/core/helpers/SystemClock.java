/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.helpers;

import org.apache.logging.log4j.core.helpers.Clock;

public class SystemClock
implements Clock {
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}

