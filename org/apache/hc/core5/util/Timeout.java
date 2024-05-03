/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.util;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class Timeout
extends TimeValue {
    public static final Timeout ZERO_MILLISECONDS;
    public static final Timeout DISABLED;

    public static Timeout defaultsToDisabled(Timeout timeout) {
        return Timeout.defaultsTo(timeout, DISABLED);
    }

    public static Timeout of(long duration, TimeUnit timeUnit) {
        return new Timeout(duration, timeUnit);
    }

    public static Timeout ofDays(long days) {
        return Timeout.of(days, TimeUnit.DAYS);
    }

    public static Timeout ofHours(long hours) {
        return Timeout.of(hours, TimeUnit.HOURS);
    }

    public static Timeout ofMicroseconds(long microseconds) {
        return Timeout.of(microseconds, TimeUnit.MICROSECONDS);
    }

    public static Timeout ofMilliseconds(long milliseconds) {
        return Timeout.of(milliseconds, TimeUnit.MILLISECONDS);
    }

    public static Timeout ofMinutes(long minutes) {
        return Timeout.of(minutes, TimeUnit.MINUTES);
    }

    public static Timeout ofNanoseconds(long nanoseconds) {
        return Timeout.of(nanoseconds, TimeUnit.NANOSECONDS);
    }

    public static Timeout ofSeconds(long seconds) {
        return Timeout.of(seconds, TimeUnit.SECONDS);
    }

    public static Timeout parse(String value) throws ParseException {
        return TimeValue.parse(value).toTimeout();
    }

    Timeout(long duration, TimeUnit timeUnit) {
        super(Args.notNegative(duration, "duration"), Args.notNull(timeUnit, "timeUnit"));
    }

    public boolean isDisabled() {
        return this.getDuration() == 0L;
    }

    public boolean isEnabled() {
        return !this.isDisabled();
    }

    static {
        DISABLED = ZERO_MILLISECONDS = Timeout.of(0L, TimeUnit.MILLISECONDS);
    }
}

