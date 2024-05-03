/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.pattern;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ArrayPatternConverter;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.core.time.MutableInstant;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.datetime.FastDateFormat;
import org.apache.logging.log4j.core.util.datetime.FixedDateFormat;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name="DatePatternConverter", category="Converter")
@ConverterKeys(value={"d", "date"})
@PerformanceSensitive(value={"allocation"})
public final class DatePatternConverter
extends LogEventPatternConverter
implements ArrayPatternConverter {
    private static final String UNIX_FORMAT = "UNIX";
    private static final String UNIX_MILLIS_FORMAT = "UNIX_MILLIS";
    private final String[] options;
    private final ThreadLocal<MutableInstant> threadLocalMutableInstant = new ThreadLocal();
    private final ThreadLocal<Formatter> threadLocalFormatter = new ThreadLocal();
    private final AtomicReference<CachedTime> cachedTime;
    private final Formatter formatter;

    private DatePatternConverter(String[] options) {
        super("Date", "date");
        this.options = options == null ? null : Arrays.copyOf(options, options.length);
        this.formatter = this.createFormatter(options);
        this.cachedTime = new AtomicReference<CachedTime>(this.fromEpochMillis(System.currentTimeMillis()));
    }

    private CachedTime fromEpochMillis(long epochMillis) {
        MutableInstant temp = new MutableInstant();
        temp.initFromEpochMilli(epochMillis, 0);
        return new CachedTime(temp);
    }

    private Formatter createFormatter(String[] options) {
        FixedDateFormat fixedDateFormat = FixedDateFormat.createIfSupported(options);
        if (fixedDateFormat != null) {
            return DatePatternConverter.createFixedFormatter(fixedDateFormat);
        }
        return DatePatternConverter.createNonFixedFormatter(options);
    }

    public static DatePatternConverter newInstance(String[] options) {
        return new DatePatternConverter(options);
    }

    private static Formatter createFixedFormatter(FixedDateFormat fixedDateFormat) {
        return new FixedFormatter(fixedDateFormat);
    }

    private static Formatter createNonFixedFormatter(String[] options) {
        Objects.requireNonNull(options);
        if (options.length == 0) {
            throw new IllegalArgumentException("Options array must have at least one element");
        }
        Objects.requireNonNull(options[0]);
        String patternOption = options[0];
        if (UNIX_FORMAT.equals(patternOption)) {
            return new UnixFormatter();
        }
        if (UNIX_MILLIS_FORMAT.equals(patternOption)) {
            return new UnixMillisFormatter();
        }
        FixedDateFormat.FixedFormat fixedFormat = FixedDateFormat.FixedFormat.lookup(patternOption);
        String pattern = fixedFormat == null ? patternOption : fixedFormat.getPattern();
        TimeZone tz = null;
        if (options.length > 1 && options[1] != null) {
            tz = TimeZone.getTimeZone(options[1]);
        }
        Locale locale = null;
        if (options.length > 2 && options[2] != null) {
            locale = Locale.forLanguageTag(options[2]);
        }
        try {
            FastDateFormat tempFormat = FastDateFormat.getInstance(pattern, tz, locale);
            return new PatternFormatter(tempFormat);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Could not instantiate FastDateFormat with pattern " + pattern, (Throwable)e);
            return DatePatternConverter.createFixedFormatter(FixedDateFormat.create(FixedDateFormat.FixedFormat.DEFAULT, tz));
        }
    }

    public void format(Date date, StringBuilder toAppendTo) {
        this.format(date.getTime(), toAppendTo);
    }

    @Override
    public void format(LogEvent event, StringBuilder output) {
        this.format(event.getInstant(), output);
    }

    public void format(long epochMilli, StringBuilder output) {
        MutableInstant instant = this.getMutableInstant();
        instant.initFromEpochMilli(epochMilli, 0);
        this.format(instant, output);
    }

    private MutableInstant getMutableInstant() {
        if (Constants.ENABLE_THREADLOCALS) {
            MutableInstant result = this.threadLocalMutableInstant.get();
            if (result == null) {
                result = new MutableInstant();
                this.threadLocalMutableInstant.set(result);
            }
            return result;
        }
        return new MutableInstant();
    }

    public void format(Instant instant, StringBuilder output) {
        if (Constants.ENABLE_THREADLOCALS) {
            this.formatWithoutAllocation(instant, output);
        } else {
            this.formatWithoutThreadLocals(instant, output);
        }
    }

    private void formatWithoutAllocation(Instant instant, StringBuilder output) {
        this.getThreadLocalFormatter().formatToBuffer(instant, output);
    }

    private Formatter getThreadLocalFormatter() {
        Formatter result = this.threadLocalFormatter.get();
        if (result == null) {
            result = this.createFormatter(this.options);
            this.threadLocalFormatter.set(result);
        }
        return result;
    }

    private void formatWithoutThreadLocals(Instant instant, StringBuilder output) {
        CachedTime cached = this.cachedTime.get();
        if (instant.getEpochSecond() != cached.epochSecond || instant.getNanoOfSecond() != cached.nanoOfSecond) {
            CachedTime newTime = new CachedTime(instant);
            cached = this.cachedTime.compareAndSet(cached, newTime) ? newTime : this.cachedTime.get();
        }
        output.append(cached.formatted);
    }

    @Override
    public void format(Object obj, StringBuilder output) {
        if (obj instanceof Date) {
            this.format((Date)obj, output);
        }
        super.format(obj, output);
    }

    @Override
    public void format(StringBuilder toAppendTo, Object ... objects) {
        for (Object obj : objects) {
            if (!(obj instanceof Date)) continue;
            this.format(obj, toAppendTo);
            break;
        }
    }

    public String getPattern() {
        return this.formatter.toPattern();
    }

    private final class CachedTime {
        public long epochSecond;
        public int nanoOfSecond;
        public String formatted;

        public CachedTime(Instant instant) {
            this.epochSecond = instant.getEpochSecond();
            this.nanoOfSecond = instant.getNanoOfSecond();
            this.formatted = DatePatternConverter.this.formatter.format(instant);
        }
    }

    private static final class UnixMillisFormatter
    extends Formatter {
        private UnixMillisFormatter() {
        }

        @Override
        String format(Instant instant) {
            return Long.toString(instant.getEpochMillisecond());
        }

        @Override
        void formatToBuffer(Instant instant, StringBuilder destination) {
            destination.append(instant.getEpochMillisecond());
        }
    }

    private static final class UnixFormatter
    extends Formatter {
        private UnixFormatter() {
        }

        @Override
        String format(Instant instant) {
            return Long.toString(instant.getEpochSecond());
        }

        @Override
        void formatToBuffer(Instant instant, StringBuilder destination) {
            destination.append(instant.getEpochSecond());
        }
    }

    private static final class FixedFormatter
    extends Formatter {
        private final FixedDateFormat fixedDateFormat;
        private final char[] cachedBuffer = new char[70];
        private int length = 0;

        FixedFormatter(FixedDateFormat fixedDateFormat) {
            this.fixedDateFormat = fixedDateFormat;
        }

        @Override
        String format(Instant instant) {
            return this.fixedDateFormat.formatInstant(instant);
        }

        @Override
        void formatToBuffer(Instant instant, StringBuilder destination) {
            int nanoOfSecond;
            long epochSecond = instant.getEpochSecond();
            if (!this.fixedDateFormat.isEquivalent(this.previousTime, this.nanos, epochSecond, nanoOfSecond = instant.getNanoOfSecond())) {
                this.length = this.fixedDateFormat.formatInstant(instant, this.cachedBuffer, 0);
                this.previousTime = epochSecond;
                this.nanos = nanoOfSecond;
            }
            destination.append(this.cachedBuffer, 0, this.length);
        }

        @Override
        public String toPattern() {
            return this.fixedDateFormat.getFormat();
        }
    }

    private static final class PatternFormatter
    extends Formatter {
        private final FastDateFormat fastDateFormat;
        private final StringBuilder cachedBuffer = new StringBuilder(64);

        PatternFormatter(FastDateFormat fastDateFormat) {
            this.fastDateFormat = fastDateFormat;
        }

        @Override
        String format(Instant instant) {
            return this.fastDateFormat.format(instant.getEpochMillisecond());
        }

        @Override
        void formatToBuffer(Instant instant, StringBuilder destination) {
            long timeMillis = instant.getEpochMillisecond();
            if (this.previousTime != timeMillis) {
                this.cachedBuffer.setLength(0);
                this.fastDateFormat.format(timeMillis, this.cachedBuffer);
            }
            destination.append((CharSequence)this.cachedBuffer);
        }

        @Override
        public String toPattern() {
            return this.fastDateFormat.getPattern();
        }
    }

    private static abstract class Formatter {
        long previousTime;
        int nanos;

        private Formatter() {
        }

        abstract String format(Instant var1);

        abstract void formatToBuffer(Instant var1, StringBuilder var2);

        public String toPattern() {
            return null;
        }
    }
}

