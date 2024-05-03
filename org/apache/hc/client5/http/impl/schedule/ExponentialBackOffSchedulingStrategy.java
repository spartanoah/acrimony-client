/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.schedule;

import org.apache.hc.client5.http.schedule.SchedulingStrategy;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

@Contract(threading=ThreadingBehavior.STATELESS)
public class ExponentialBackOffSchedulingStrategy
implements SchedulingStrategy {
    public static final long DEFAULT_BACK_OFF_RATE = 10L;
    public static final TimeValue DEFAULT_INITIAL_EXPIRY = TimeValue.ofSeconds(6L);
    public static final TimeValue DEFAULT_MAX_EXPIRY = TimeValue.ofSeconds(86400L);
    private static final ExponentialBackOffSchedulingStrategy INSTANCE = new ExponentialBackOffSchedulingStrategy(10L, DEFAULT_INITIAL_EXPIRY, DEFAULT_MAX_EXPIRY);
    private final long backOffRate;
    private final TimeValue initialExpiry;
    private final TimeValue maxExpiry;

    public ExponentialBackOffSchedulingStrategy(long backOffRate, TimeValue initialExpiry, TimeValue maxExpiry) {
        this.backOffRate = Args.notNegative(backOffRate, "BackOff rate");
        this.initialExpiry = Args.notNull(initialExpiry, "Initial expiry");
        this.maxExpiry = Args.notNull(maxExpiry, "Max expiry");
    }

    public ExponentialBackOffSchedulingStrategy(long backOffRate, TimeValue initialExpiry) {
        this(backOffRate, initialExpiry, DEFAULT_MAX_EXPIRY);
    }

    public ExponentialBackOffSchedulingStrategy(long backOffRate) {
        this(backOffRate, DEFAULT_INITIAL_EXPIRY, DEFAULT_MAX_EXPIRY);
    }

    public ExponentialBackOffSchedulingStrategy() {
        this(10L, DEFAULT_INITIAL_EXPIRY, DEFAULT_MAX_EXPIRY);
    }

    @Override
    public TimeValue schedule(int attemptNumber) {
        return this.calculateDelay(attemptNumber);
    }

    public long getBackOffRate() {
        return this.backOffRate;
    }

    public TimeValue getInitialExpiry() {
        return this.initialExpiry;
    }

    public TimeValue getMaxExpiry() {
        return this.maxExpiry;
    }

    protected TimeValue calculateDelay(int consecutiveFailedAttempts) {
        if (consecutiveFailedAttempts > 0) {
            long delay = (long)((double)this.initialExpiry.toMilliseconds() * Math.pow(this.backOffRate, consecutiveFailedAttempts - 1));
            return TimeValue.ofMilliseconds(Math.min(delay, this.maxExpiry.toMilliseconds()));
        }
        return TimeValue.ZERO_MILLISECONDS;
    }
}

