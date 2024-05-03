/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.filter;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.util.Clock;
import org.apache.logging.log4j.core.util.ClockFactory;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name="TimeFilter", category="Core", elementType="filter", printObject=true)
@PerformanceSensitive(value={"allocation"})
public final class TimeFilter
extends AbstractFilter {
    private static final Clock CLOCK = ClockFactory.getClock();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final long HOUR_MS = 3600000L;
    private static final long DAY_MS = 86400000L;
    private volatile long start;
    private final LocalTime startTime;
    private volatile long end;
    private final LocalTime endTime;
    private final long duration;
    private final ZoneId timeZone;

    TimeFilter(LocalTime start, LocalTime end, ZoneId timeZone, Filter.Result onMatch, Filter.Result onMismatch, LocalDate now) {
        super(onMatch, onMismatch);
        this.startTime = start;
        this.endTime = end;
        this.timeZone = timeZone;
        this.start = ZonedDateTime.of(now, this.startTime, timeZone).withEarlierOffsetAtOverlap().toInstant().toEpochMilli();
        long endMillis = ZonedDateTime.of(now, this.endTime, timeZone).withEarlierOffsetAtOverlap().toInstant().toEpochMilli();
        if (end.isBefore(start)) {
            endMillis += 86400000L;
        }
        this.duration = this.startTime.isBefore(this.endTime) ? Duration.between(this.startTime, this.endTime).toMillis() : Duration.between(this.startTime, this.endTime).plusHours(24L).toMillis();
        long difference = endMillis - this.start - this.duration;
        if (difference != 0L) {
            endMillis -= difference;
        }
        this.end = endMillis;
    }

    private TimeFilter(LocalTime start, LocalTime end, ZoneId timeZone, Filter.Result onMatch, Filter.Result onMismatch) {
        this(start, end, timeZone, onMatch, onMismatch, LocalDate.now(timeZone));
    }

    private synchronized void adjustTimes(long currentTimeMillis) {
        long difference;
        if (currentTimeMillis <= this.end) {
            return;
        }
        LocalDate date = Instant.ofEpochMilli(currentTimeMillis).atZone(this.timeZone).toLocalDate();
        this.start = ZonedDateTime.of(date, this.startTime, this.timeZone).withEarlierOffsetAtOverlap().toInstant().toEpochMilli();
        long endMillis = ZonedDateTime.of(date, this.endTime, this.timeZone).withEarlierOffsetAtOverlap().toInstant().toEpochMilli();
        if (this.endTime.isBefore(this.startTime)) {
            endMillis += 86400000L;
        }
        if ((difference = endMillis - this.start - this.duration) != 0L) {
            endMillis -= difference;
        }
        this.end = endMillis;
    }

    Filter.Result filter(long currentTimeMillis) {
        if (currentTimeMillis > this.end) {
            this.adjustTimes(currentTimeMillis);
        }
        return currentTimeMillis >= this.start && currentTimeMillis <= this.end ? this.onMatch : this.onMismatch;
    }

    @Override
    public Filter.Result filter(LogEvent event) {
        return this.filter(event.getTimeMillis());
    }

    private Filter.Result filter() {
        return this.filter(CLOCK.currentTimeMillis());
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return this.filter();
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return this.filter();
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object ... params) {
        return this.filter();
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0) {
        return this.filter();
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1) {
        return this.filter();
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2) {
        return this.filter();
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3) {
        return this.filter();
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return this.filter();
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return this.filter();
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return this.filter();
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return this.filter();
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return this.filter();
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return this.filter();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("start=").append(this.start);
        sb.append(", end=").append(this.end);
        sb.append(", timezone=").append(this.timeZone.toString());
        return sb.toString();
    }

    @PluginFactory
    public static TimeFilter createFilter(@PluginAttribute(value="start") String start, @PluginAttribute(value="end") String end, @PluginAttribute(value="timezone") String tz, @PluginAttribute(value="onMatch") Filter.Result match, @PluginAttribute(value="onMismatch") Filter.Result mismatch) {
        LocalTime startTime = TimeFilter.parseTimestamp(start, LocalTime.MIN);
        LocalTime endTime = TimeFilter.parseTimestamp(end, LocalTime.MAX);
        ZoneId timeZone = tz == null ? ZoneId.systemDefault() : ZoneId.of(tz);
        Filter.Result onMatch = match == null ? Filter.Result.NEUTRAL : match;
        Filter.Result onMismatch = mismatch == null ? Filter.Result.DENY : mismatch;
        return new TimeFilter(startTime, endTime, timeZone, onMatch, onMismatch);
    }

    private static LocalTime parseTimestamp(String timestamp, LocalTime defaultValue) {
        if (timestamp == null) {
            return defaultValue;
        }
        try {
            return LocalTime.parse(timestamp, FORMATTER);
        } catch (Exception e) {
            LOGGER.warn("Error parsing TimeFilter timestamp value {}", (Object)timestamp, (Object)e);
            return defaultValue;
        }
    }
}

