/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;

public class DateDeserializers {
    private static final HashSet<String> _classNames;

    public static JsonDeserializer<?> find(Class<?> rawType, String clsName) {
        if (_classNames.contains(clsName)) {
            if (rawType == Calendar.class) {
                return new CalendarDeserializer();
            }
            if (rawType == java.util.Date.class) {
                return DateDeserializer.instance;
            }
            if (rawType == Date.class) {
                return new SqlDateDeserializer();
            }
            if (rawType == Timestamp.class) {
                return new TimestampDeserializer();
            }
            if (rawType == GregorianCalendar.class) {
                return new CalendarDeserializer((Class<? extends Calendar>)GregorianCalendar.class);
            }
        }
        return null;
    }

    public static boolean hasDeserializerFor(Class<?> rawType) {
        return _classNames.contains(rawType.getName());
    }

    static {
        Class[] numberTypes;
        _classNames = new HashSet();
        for (Class cls : numberTypes = new Class[]{Calendar.class, GregorianCalendar.class, Date.class, java.util.Date.class, Timestamp.class}) {
            _classNames.add(cls.getName());
        }
    }

    public static class TimestampDeserializer
    extends DateBasedDeserializer<Timestamp> {
        public TimestampDeserializer() {
            super(Timestamp.class);
        }

        public TimestampDeserializer(TimestampDeserializer src, DateFormat df, String formatString) {
            super(src, df, formatString);
        }

        protected TimestampDeserializer withDateFormat(DateFormat df, String formatString) {
            return new TimestampDeserializer(this, df, formatString);
        }

        @Override
        public Timestamp deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            java.util.Date d = this._parseDate(p, ctxt);
            return d == null ? null : new Timestamp(d.getTime());
        }
    }

    public static class SqlDateDeserializer
    extends DateBasedDeserializer<Date> {
        public SqlDateDeserializer() {
            super(Date.class);
        }

        public SqlDateDeserializer(SqlDateDeserializer src, DateFormat df, String formatString) {
            super(src, df, formatString);
        }

        protected SqlDateDeserializer withDateFormat(DateFormat df, String formatString) {
            return new SqlDateDeserializer(this, df, formatString);
        }

        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            java.util.Date d = this._parseDate(p, ctxt);
            return d == null ? null : new Date(d.getTime());
        }
    }

    @JacksonStdImpl
    public static class DateDeserializer
    extends DateBasedDeserializer<java.util.Date> {
        public static final DateDeserializer instance = new DateDeserializer();

        public DateDeserializer() {
            super(java.util.Date.class);
        }

        public DateDeserializer(DateDeserializer base, DateFormat df, String formatString) {
            super(base, df, formatString);
        }

        protected DateDeserializer withDateFormat(DateFormat df, String formatString) {
            return new DateDeserializer(this, df, formatString);
        }

        @Override
        public java.util.Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return this._parseDate(p, ctxt);
        }
    }

    @JacksonStdImpl
    public static class CalendarDeserializer
    extends DateBasedDeserializer<Calendar> {
        protected final Constructor<Calendar> _defaultCtor;

        public CalendarDeserializer() {
            super(Calendar.class);
            this._defaultCtor = null;
        }

        public CalendarDeserializer(Class<? extends Calendar> cc) {
            super(cc);
            this._defaultCtor = ClassUtil.findConstructor(cc, false);
        }

        public CalendarDeserializer(CalendarDeserializer src, DateFormat df, String formatString) {
            super(src, df, formatString);
            this._defaultCtor = src._defaultCtor;
        }

        protected CalendarDeserializer withDateFormat(DateFormat df, String formatString) {
            return new CalendarDeserializer(this, df, formatString);
        }

        @Override
        public Calendar deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            java.util.Date d = this._parseDate(p, ctxt);
            if (d == null) {
                return null;
            }
            if (this._defaultCtor == null) {
                return ctxt.constructCalendar(d);
            }
            try {
                Calendar c = this._defaultCtor.newInstance(new Object[0]);
                c.setTimeInMillis(d.getTime());
                TimeZone tz = ctxt.getTimeZone();
                if (tz != null) {
                    c.setTimeZone(tz);
                }
                return c;
            } catch (Exception e) {
                return (Calendar)ctxt.handleInstantiationProblem(this.handledType(), d, e);
            }
        }
    }

    protected static abstract class DateBasedDeserializer<T>
    extends StdScalarDeserializer<T>
    implements ContextualDeserializer {
        protected final DateFormat _customFormat;
        protected final String _formatString;

        protected DateBasedDeserializer(Class<?> clz) {
            super(clz);
            this._customFormat = null;
            this._formatString = null;
        }

        protected DateBasedDeserializer(DateBasedDeserializer<T> base, DateFormat format, String formatStr) {
            super(base._valueClass);
            this._customFormat = format;
            this._formatString = formatStr;
        }

        protected abstract DateBasedDeserializer<T> withDateFormat(DateFormat var1, String var2);

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            JsonFormat.Value format = this.findFormatOverrides(ctxt, property, this.handledType());
            if (format != null) {
                TimeZone tz = format.getTimeZone();
                Boolean lenient = format.getLenient();
                if (format.hasPattern()) {
                    String pattern = format.getPattern();
                    Locale loc = format.hasLocale() ? format.getLocale() : ctxt.getLocale();
                    SimpleDateFormat df = new SimpleDateFormat(pattern, loc);
                    if (tz == null) {
                        tz = ctxt.getTimeZone();
                    }
                    df.setTimeZone(tz);
                    if (lenient != null) {
                        df.setLenient(lenient);
                    }
                    return this.withDateFormat(df, pattern);
                }
                if (tz != null) {
                    DateFormat df = ctxt.getConfig().getDateFormat();
                    if (df.getClass() == StdDateFormat.class) {
                        Locale loc = format.hasLocale() ? format.getLocale() : ctxt.getLocale();
                        StdDateFormat std = (StdDateFormat)df;
                        std = std.withTimeZone(tz);
                        std = std.withLocale(loc);
                        if (lenient != null) {
                            std = std.withLenient(lenient);
                        }
                        df = std;
                    } else {
                        df = (DateFormat)df.clone();
                        df.setTimeZone(tz);
                        if (lenient != null) {
                            df.setLenient(lenient);
                        }
                    }
                    return this.withDateFormat(df, this._formatString);
                }
                if (lenient != null) {
                    DateFormat df = ctxt.getConfig().getDateFormat();
                    String pattern = this._formatString;
                    if (df.getClass() == StdDateFormat.class) {
                        StdDateFormat std = (StdDateFormat)df;
                        std = std.withLenient(lenient);
                        df = std;
                        pattern = std.toPattern();
                    } else {
                        df = (DateFormat)df.clone();
                        df.setLenient(lenient);
                        if (df instanceof SimpleDateFormat) {
                            ((SimpleDateFormat)df).toPattern();
                        }
                    }
                    if (pattern == null) {
                        pattern = "[unknown]";
                    }
                    return this.withDateFormat(df, pattern);
                }
            }
            return this;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        protected java.util.Date _parseDate(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (this._customFormat != null && p.hasToken(JsonToken.VALUE_STRING)) {
                String str = p.getText().trim();
                if (str.length() == 0) {
                    return (java.util.Date)this.getEmptyValue(ctxt);
                }
                DateFormat dateFormat = this._customFormat;
                synchronized (dateFormat) {
                    try {
                        return this._customFormat.parse(str);
                    } catch (ParseException e) {
                        return (java.util.Date)ctxt.handleWeirdStringValue(this.handledType(), str, "expected format \"%s\"", this._formatString);
                    }
                }
            }
            return super._parseDate(p, ctxt);
        }
    }
}

