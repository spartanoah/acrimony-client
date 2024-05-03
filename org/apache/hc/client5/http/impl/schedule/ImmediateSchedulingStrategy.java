/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.schedule;

import org.apache.hc.client5.http.schedule.SchedulingStrategy;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.TimeValue;

@Contract(threading=ThreadingBehavior.STATELESS)
public class ImmediateSchedulingStrategy
implements SchedulingStrategy {
    public static final ImmediateSchedulingStrategy INSTANCE = new ImmediateSchedulingStrategy();

    @Override
    public TimeValue schedule(int attemptNumber) {
        return TimeValue.ZERO_MILLISECONDS;
    }
}

