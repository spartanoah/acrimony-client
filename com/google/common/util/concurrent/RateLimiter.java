/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Ticker;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@Beta
public abstract class RateLimiter {
    private final SleepingTicker ticker;
    private final long offsetNanos;
    double storedPermits;
    double maxPermits;
    volatile double stableIntervalMicros;
    private final Object mutex = new Object();
    private long nextFreeTicketMicros = 0L;

    public static RateLimiter create(double permitsPerSecond) {
        return RateLimiter.create(SleepingTicker.SYSTEM_TICKER, permitsPerSecond);
    }

    @VisibleForTesting
    static RateLimiter create(SleepingTicker ticker, double permitsPerSecond) {
        Bursty rateLimiter = new Bursty(ticker, 1.0);
        rateLimiter.setRate(permitsPerSecond);
        return rateLimiter;
    }

    public static RateLimiter create(double permitsPerSecond, long warmupPeriod, TimeUnit unit) {
        return RateLimiter.create(SleepingTicker.SYSTEM_TICKER, permitsPerSecond, warmupPeriod, unit);
    }

    @VisibleForTesting
    static RateLimiter create(SleepingTicker ticker, double permitsPerSecond, long warmupPeriod, TimeUnit unit) {
        WarmingUp rateLimiter = new WarmingUp(ticker, warmupPeriod, unit);
        rateLimiter.setRate(permitsPerSecond);
        return rateLimiter;
    }

    @VisibleForTesting
    static RateLimiter createWithCapacity(SleepingTicker ticker, double permitsPerSecond, long maxBurstBuildup, TimeUnit unit) {
        double maxBurstSeconds = (double)unit.toNanos(maxBurstBuildup) / 1.0E9;
        Bursty rateLimiter = new Bursty(ticker, maxBurstSeconds);
        rateLimiter.setRate(permitsPerSecond);
        return rateLimiter;
    }

    private RateLimiter(SleepingTicker ticker) {
        this.ticker = ticker;
        this.offsetNanos = ticker.read();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void setRate(double permitsPerSecond) {
        Preconditions.checkArgument(permitsPerSecond > 0.0 && !Double.isNaN(permitsPerSecond), "rate must be positive");
        Object object = this.mutex;
        synchronized (object) {
            double stableIntervalMicros;
            this.resync(this.readSafeMicros());
            this.stableIntervalMicros = stableIntervalMicros = (double)TimeUnit.SECONDS.toMicros(1L) / permitsPerSecond;
            this.doSetRate(permitsPerSecond, stableIntervalMicros);
        }
    }

    abstract void doSetRate(double var1, double var3);

    public final double getRate() {
        return (double)TimeUnit.SECONDS.toMicros(1L) / this.stableIntervalMicros;
    }

    public double acquire() {
        return this.acquire(1);
    }

    public double acquire(int permits) {
        long microsToWait = this.reserve(permits);
        this.ticker.sleepMicrosUninterruptibly(microsToWait);
        return 1.0 * (double)microsToWait / (double)TimeUnit.SECONDS.toMicros(1L);
    }

    long reserve() {
        return this.reserve(1);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    long reserve(int permits) {
        RateLimiter.checkPermits(permits);
        Object object = this.mutex;
        synchronized (object) {
            return this.reserveNextTicket(permits, this.readSafeMicros());
        }
    }

    public boolean tryAcquire(long timeout, TimeUnit unit) {
        return this.tryAcquire(1, timeout, unit);
    }

    public boolean tryAcquire(int permits) {
        return this.tryAcquire(permits, 0L, TimeUnit.MICROSECONDS);
    }

    public boolean tryAcquire() {
        return this.tryAcquire(1, 0L, TimeUnit.MICROSECONDS);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean tryAcquire(int permits, long timeout, TimeUnit unit) {
        long microsToWait;
        long timeoutMicros = unit.toMicros(timeout);
        RateLimiter.checkPermits(permits);
        Object object = this.mutex;
        synchronized (object) {
            long nowMicros = this.readSafeMicros();
            if (this.nextFreeTicketMicros > nowMicros + timeoutMicros) {
                return false;
            }
            microsToWait = this.reserveNextTicket(permits, nowMicros);
        }
        this.ticker.sleepMicrosUninterruptibly(microsToWait);
        return true;
    }

    private static void checkPermits(int permits) {
        Preconditions.checkArgument(permits > 0, "Requested permits must be positive");
    }

    private long reserveNextTicket(double requiredPermits, long nowMicros) {
        this.resync(nowMicros);
        long microsToNextFreeTicket = Math.max(0L, this.nextFreeTicketMicros - nowMicros);
        double storedPermitsToSpend = Math.min(requiredPermits, this.storedPermits);
        double freshPermits = requiredPermits - storedPermitsToSpend;
        long waitMicros = this.storedPermitsToWaitTime(this.storedPermits, storedPermitsToSpend) + (long)(freshPermits * this.stableIntervalMicros);
        this.nextFreeTicketMicros += waitMicros;
        this.storedPermits -= storedPermitsToSpend;
        return microsToNextFreeTicket;
    }

    abstract long storedPermitsToWaitTime(double var1, double var3);

    private void resync(long nowMicros) {
        if (nowMicros > this.nextFreeTicketMicros) {
            this.storedPermits = Math.min(this.maxPermits, this.storedPermits + (double)(nowMicros - this.nextFreeTicketMicros) / this.stableIntervalMicros);
            this.nextFreeTicketMicros = nowMicros;
        }
    }

    private long readSafeMicros() {
        return TimeUnit.NANOSECONDS.toMicros(this.ticker.read() - this.offsetNanos);
    }

    public String toString() {
        return String.format("RateLimiter[stableRate=%3.1fqps]", 1000000.0 / this.stableIntervalMicros);
    }

    @VisibleForTesting
    static abstract class SleepingTicker
    extends Ticker {
        static final SleepingTicker SYSTEM_TICKER = new SleepingTicker(){

            @Override
            public long read() {
                return 1.systemTicker().read();
            }

            @Override
            public void sleepMicrosUninterruptibly(long micros) {
                if (micros > 0L) {
                    Uninterruptibles.sleepUninterruptibly(micros, TimeUnit.MICROSECONDS);
                }
            }
        };

        SleepingTicker() {
        }

        abstract void sleepMicrosUninterruptibly(long var1);
    }

    private static class Bursty
    extends RateLimiter {
        final double maxBurstSeconds;

        Bursty(SleepingTicker ticker, double maxBurstSeconds) {
            super(ticker);
            this.maxBurstSeconds = maxBurstSeconds;
        }

        @Override
        void doSetRate(double permitsPerSecond, double stableIntervalMicros) {
            double oldMaxPermits = this.maxPermits;
            this.maxPermits = this.maxBurstSeconds * permitsPerSecond;
            this.storedPermits = oldMaxPermits == 0.0 ? 0.0 : this.storedPermits * this.maxPermits / oldMaxPermits;
        }

        @Override
        long storedPermitsToWaitTime(double storedPermits, double permitsToTake) {
            return 0L;
        }
    }

    private static class WarmingUp
    extends RateLimiter {
        final long warmupPeriodMicros;
        private double slope;
        private double halfPermits;

        WarmingUp(SleepingTicker ticker, long warmupPeriod, TimeUnit timeUnit) {
            super(ticker);
            this.warmupPeriodMicros = timeUnit.toMicros(warmupPeriod);
        }

        @Override
        void doSetRate(double permitsPerSecond, double stableIntervalMicros) {
            double oldMaxPermits = this.maxPermits;
            this.maxPermits = (double)this.warmupPeriodMicros / stableIntervalMicros;
            this.halfPermits = this.maxPermits / 2.0;
            double coldIntervalMicros = stableIntervalMicros * 3.0;
            this.slope = (coldIntervalMicros - stableIntervalMicros) / this.halfPermits;
            this.storedPermits = oldMaxPermits == Double.POSITIVE_INFINITY ? 0.0 : (oldMaxPermits == 0.0 ? this.maxPermits : this.storedPermits * this.maxPermits / oldMaxPermits);
        }

        @Override
        long storedPermitsToWaitTime(double storedPermits, double permitsToTake) {
            double availablePermitsAboveHalf = storedPermits - this.halfPermits;
            long micros = 0L;
            if (availablePermitsAboveHalf > 0.0) {
                double permitsAboveHalfToTake = Math.min(availablePermitsAboveHalf, permitsToTake);
                micros = (long)(permitsAboveHalfToTake * (this.permitsToTime(availablePermitsAboveHalf) + this.permitsToTime(availablePermitsAboveHalf - permitsAboveHalfToTake)) / 2.0);
                permitsToTake -= permitsAboveHalfToTake;
            }
            micros = (long)((double)micros + this.stableIntervalMicros * permitsToTake);
            return micros;
        }

        private double permitsToTime(double permits) {
            return this.stableIntervalMicros + permits * this.slope;
        }
    }
}

