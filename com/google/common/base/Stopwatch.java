/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.base;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.base.Ticker;
import java.util.concurrent.TimeUnit;

@Beta
@GwtCompatible(emulated=true)
public final class Stopwatch {
    private final Ticker ticker;
    private boolean isRunning;
    private long elapsedNanos;
    private long startTick;

    public static Stopwatch createUnstarted() {
        return new Stopwatch();
    }

    public static Stopwatch createUnstarted(Ticker ticker) {
        return new Stopwatch(ticker);
    }

    public static Stopwatch createStarted() {
        return new Stopwatch().start();
    }

    public static Stopwatch createStarted(Ticker ticker) {
        return new Stopwatch(ticker).start();
    }

    @Deprecated
    Stopwatch() {
        this(Ticker.systemTicker());
    }

    @Deprecated
    Stopwatch(Ticker ticker) {
        this.ticker = Preconditions.checkNotNull(ticker, "ticker");
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public Stopwatch start() {
        Preconditions.checkState(!this.isRunning, "This stopwatch is already running.");
        this.isRunning = true;
        this.startTick = this.ticker.read();
        return this;
    }

    public Stopwatch stop() {
        long tick = this.ticker.read();
        Preconditions.checkState(this.isRunning, "This stopwatch is already stopped.");
        this.isRunning = false;
        this.elapsedNanos += tick - this.startTick;
        return this;
    }

    public Stopwatch reset() {
        this.elapsedNanos = 0L;
        this.isRunning = false;
        return this;
    }

    private long elapsedNanos() {
        return this.isRunning ? this.ticker.read() - this.startTick + this.elapsedNanos : this.elapsedNanos;
    }

    public long elapsed(TimeUnit desiredUnit) {
        return desiredUnit.convert(this.elapsedNanos(), TimeUnit.NANOSECONDS);
    }

    @GwtIncompatible(value="String.format()")
    public String toString() {
        long nanos = this.elapsedNanos();
        TimeUnit unit = Stopwatch.chooseUnit(nanos);
        double value = (double)nanos / (double)TimeUnit.NANOSECONDS.convert(1L, unit);
        return String.format("%.4g %s", value, Stopwatch.abbreviate(unit));
    }

    private static TimeUnit chooseUnit(long nanos) {
        if (TimeUnit.DAYS.convert(nanos, TimeUnit.NANOSECONDS) > 0L) {
            return TimeUnit.DAYS;
        }
        if (TimeUnit.HOURS.convert(nanos, TimeUnit.NANOSECONDS) > 0L) {
            return TimeUnit.HOURS;
        }
        if (TimeUnit.MINUTES.convert(nanos, TimeUnit.NANOSECONDS) > 0L) {
            return TimeUnit.MINUTES;
        }
        if (TimeUnit.SECONDS.convert(nanos, TimeUnit.NANOSECONDS) > 0L) {
            return TimeUnit.SECONDS;
        }
        if (TimeUnit.MILLISECONDS.convert(nanos, TimeUnit.NANOSECONDS) > 0L) {
            return TimeUnit.MILLISECONDS;
        }
        if (TimeUnit.MICROSECONDS.convert(nanos, TimeUnit.NANOSECONDS) > 0L) {
            return TimeUnit.MICROSECONDS;
        }
        return TimeUnit.NANOSECONDS;
    }

    private static String abbreviate(TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS: {
                return "ns";
            }
            case MICROSECONDS: {
                return "\u03bcs";
            }
            case MILLISECONDS: {
                return "ms";
            }
            case SECONDS: {
                return "s";
            }
            case MINUTES: {
                return "min";
            }
            case HOURS: {
                return "h";
            }
            case DAYS: {
                return "d";
            }
        }
        throw new AssertionError();
    }
}

