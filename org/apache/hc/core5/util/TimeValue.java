/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.util;

import java.text.ParseException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.LangUtils;
import org.apache.hc.core5.util.Timeout;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class TimeValue
implements Comparable<TimeValue> {
    static final int INT_UNDEFINED = -1;
    public static final TimeValue MAX_VALUE = TimeValue.ofDays(Long.MAX_VALUE);
    public static final TimeValue NEG_ONE_MILLISECOND = TimeValue.of(-1L, TimeUnit.MILLISECONDS);
    public static final TimeValue NEG_ONE_SECOND = TimeValue.of(-1L, TimeUnit.SECONDS);
    public static final TimeValue ZERO_MILLISECONDS = TimeValue.of(0L, TimeUnit.MILLISECONDS);
    private final long duration;
    private final TimeUnit timeUnit;

    public static int asBoundInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int)value;
    }

    public static <T extends TimeValue> T defaultsTo(T timeValue, T defaultValue) {
        return timeValue != null ? timeValue : defaultValue;
    }

    public static TimeValue defaultsToNegativeOneMillisecond(TimeValue timeValue) {
        return TimeValue.defaultsTo(timeValue, NEG_ONE_MILLISECOND);
    }

    public static TimeValue defaultsToNegativeOneSecond(TimeValue timeValue) {
        return TimeValue.defaultsTo(timeValue, NEG_ONE_SECOND);
    }

    public static TimeValue defaultsToZeroMilliseconds(TimeValue timeValue) {
        return TimeValue.defaultsTo(timeValue, ZERO_MILLISECONDS);
    }

    public static boolean isNonNegative(TimeValue timeValue) {
        return timeValue != null && timeValue.getDuration() >= 0L;
    }

    public static boolean isPositive(TimeValue timeValue) {
        return timeValue != null && timeValue.getDuration() > 0L;
    }

    public static TimeValue of(long duration, TimeUnit timeUnit) {
        return new TimeValue(duration, timeUnit);
    }

    public static TimeValue ofDays(long days) {
        return TimeValue.of(days, TimeUnit.DAYS);
    }

    public static TimeValue ofHours(long hours) {
        return TimeValue.of(hours, TimeUnit.HOURS);
    }

    public static TimeValue ofMicroseconds(long microseconds) {
        return TimeValue.of(microseconds, TimeUnit.MICROSECONDS);
    }

    public static TimeValue ofMilliseconds(long millis) {
        return TimeValue.of(millis, TimeUnit.MILLISECONDS);
    }

    public static TimeValue ofMinutes(long minutes) {
        return TimeValue.of(minutes, TimeUnit.MINUTES);
    }

    public static TimeValue ofNanoseconds(long nanoseconds) {
        return TimeValue.of(nanoseconds, TimeUnit.NANOSECONDS);
    }

    public static TimeValue ofSeconds(long seconds) {
        return TimeValue.of(seconds, TimeUnit.SECONDS);
    }

    public static TimeValue parse(String value) throws ParseException {
        String[] split = value.trim().split("\\s+");
        if (split.length < 2) {
            throw new IllegalArgumentException(String.format("Expected format for <Long><SPACE><java.util.concurrent.TimeUnit>: %s", value));
        }
        String clean0 = split[0].trim();
        String clean1 = split[1].trim().toUpperCase(Locale.ROOT);
        String timeUnitStr = clean1.endsWith("S") ? clean1 : clean1 + "S";
        return TimeValue.of(Long.parseLong(clean0), TimeUnit.valueOf(timeUnitStr));
    }

    TimeValue(long duration, TimeUnit timeUnit) {
        this.duration = duration;
        this.timeUnit = Args.notNull(timeUnit, "timeUnit");
    }

    public long convert(TimeUnit targetTimeUnit) {
        Args.notNull(targetTimeUnit, "timeUnit");
        return targetTimeUnit.convert(this.duration, this.timeUnit);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof TimeValue) {
            long thatDuration;
            TimeValue that = (TimeValue)obj;
            long thisDuration = this.convert(TimeUnit.NANOSECONDS);
            return thisDuration == (thatDuration = that.convert(TimeUnit.NANOSECONDS));
        }
        return false;
    }

    public TimeValue divide(long divisor) {
        long newDuration = this.duration / divisor;
        return TimeValue.of(newDuration, this.timeUnit);
    }

    public TimeValue divide(long divisor, TimeUnit targetTimeUnit) {
        return TimeValue.of(this.convert(targetTimeUnit) / divisor, targetTimeUnit);
    }

    public long getDuration() {
        return this.duration;
    }

    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }

    public int hashCode() {
        int hash = 17;
        hash = LangUtils.hashCode(hash, this.convert(TimeUnit.NANOSECONDS));
        return hash;
    }

    public TimeValue min(TimeValue other) {
        return this.compareTo(other) > 0 ? other : this;
    }

    private TimeUnit min(TimeUnit other) {
        return this.scale() > this.scale(other) ? other : this.getTimeUnit();
    }

    private int scale() {
        return this.scale(this.timeUnit);
    }

    private int scale(TimeUnit tUnit) {
        switch (tUnit) {
            case NANOSECONDS: {
                return 1;
            }
            case MICROSECONDS: {
                return 2;
            }
            case MILLISECONDS: {
                return 3;
            }
            case SECONDS: {
                return 4;
            }
            case MINUTES: {
                return 5;
            }
            case HOURS: {
                return 6;
            }
            case DAYS: {
                return 7;
            }
        }
        throw new IllegalStateException();
    }

    public void sleep() throws InterruptedException {
        this.timeUnit.sleep(this.duration);
    }

    public void timedJoin(Thread thread) throws InterruptedException {
        this.timeUnit.timedJoin(thread, this.duration);
    }

    public void timedWait(Object obj) throws InterruptedException {
        this.timeUnit.timedWait(obj, this.duration);
    }

    public long toDays() {
        return this.timeUnit.toDays(this.duration);
    }

    public long toHours() {
        return this.timeUnit.toHours(this.duration);
    }

    public long toMicroseconds() {
        return this.timeUnit.toMicros(this.duration);
    }

    public long toMilliseconds() {
        return this.timeUnit.toMillis(this.duration);
    }

    public int toMillisecondsIntBound() {
        return TimeValue.asBoundInt(this.toMilliseconds());
    }

    public long toMinutes() {
        return this.timeUnit.toMinutes(this.duration);
    }

    public long toNanoseconds() {
        return this.timeUnit.toNanos(this.duration);
    }

    public long toSeconds() {
        return this.timeUnit.toSeconds(this.duration);
    }

    public int toSecondsIntBound() {
        return TimeValue.asBoundInt(this.toSeconds());
    }

    @Override
    public int compareTo(TimeValue other) {
        TimeUnit targetTimeUnit = this.min(other.getTimeUnit());
        return Long.compare(this.convert(targetTimeUnit), other.convert(targetTimeUnit));
    }

    public String toString() {
        return String.format("%d %s", new Object[]{this.duration, this.timeUnit});
    }

    public Timeout toTimeout() {
        return Timeout.of(this.duration, this.timeUnit);
    }
}

