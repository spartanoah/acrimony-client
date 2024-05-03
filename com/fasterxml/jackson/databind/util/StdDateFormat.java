/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.io.NumberInput;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StdDateFormat
extends DateFormat {
    protected static final String PATTERN_PLAIN_STR = "\\d\\d\\d\\d[-]\\d\\d[-]\\d\\d";
    protected static final Pattern PATTERN_PLAIN = Pattern.compile("\\d\\d\\d\\d[-]\\d\\d[-]\\d\\d");
    protected static final Pattern PATTERN_ISO8601;
    public static final String DATE_FORMAT_STR_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    protected static final String DATE_FORMAT_STR_PLAIN = "yyyy-MM-dd";
    protected static final String DATE_FORMAT_STR_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
    protected static final String[] ALL_FORMATS;
    protected static final TimeZone DEFAULT_TIMEZONE;
    protected static final Locale DEFAULT_LOCALE;
    protected static final DateFormat DATE_FORMAT_RFC1123;
    public static final StdDateFormat instance;
    protected static final Calendar CALENDAR;
    protected transient TimeZone _timezone;
    protected final Locale _locale;
    protected Boolean _lenient;
    private transient Calendar _calendar;
    private transient DateFormat _formatRFC1123;
    private boolean _tzSerializedWithColon = true;

    public StdDateFormat() {
        this._locale = DEFAULT_LOCALE;
    }

    @Deprecated
    public StdDateFormat(TimeZone tz, Locale loc) {
        this._timezone = tz;
        this._locale = loc;
    }

    protected StdDateFormat(TimeZone tz, Locale loc, Boolean lenient) {
        this(tz, loc, lenient, false);
    }

    protected StdDateFormat(TimeZone tz, Locale loc, Boolean lenient, boolean formatTzOffsetWithColon) {
        this._timezone = tz;
        this._locale = loc;
        this._lenient = lenient;
        this._tzSerializedWithColon = formatTzOffsetWithColon;
    }

    public static TimeZone getDefaultTimeZone() {
        return DEFAULT_TIMEZONE;
    }

    public StdDateFormat withTimeZone(TimeZone tz) {
        if (tz == null) {
            tz = DEFAULT_TIMEZONE;
        }
        if (tz == this._timezone || tz.equals(this._timezone)) {
            return this;
        }
        return new StdDateFormat(tz, this._locale, this._lenient, this._tzSerializedWithColon);
    }

    public StdDateFormat withLocale(Locale loc) {
        if (loc.equals(this._locale)) {
            return this;
        }
        return new StdDateFormat(this._timezone, loc, this._lenient, this._tzSerializedWithColon);
    }

    public StdDateFormat withLenient(Boolean b) {
        if (StdDateFormat._equals(b, this._lenient)) {
            return this;
        }
        return new StdDateFormat(this._timezone, this._locale, b, this._tzSerializedWithColon);
    }

    public StdDateFormat withColonInTimeZone(boolean b) {
        if (this._tzSerializedWithColon == b) {
            return this;
        }
        return new StdDateFormat(this._timezone, this._locale, this._lenient, b);
    }

    @Override
    public StdDateFormat clone() {
        return new StdDateFormat(this._timezone, this._locale, this._lenient, this._tzSerializedWithColon);
    }

    @Deprecated
    public static DateFormat getISO8601Format(TimeZone tz, Locale loc) {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT_STR_ISO8601, loc);
        df.setTimeZone(DEFAULT_TIMEZONE);
        return df;
    }

    @Deprecated
    public static DateFormat getRFC1123Format(TimeZone tz, Locale loc) {
        return StdDateFormat._cloneFormat(DATE_FORMAT_RFC1123, DATE_FORMAT_STR_RFC1123, tz, loc, null);
    }

    @Override
    public TimeZone getTimeZone() {
        return this._timezone;
    }

    @Override
    public void setTimeZone(TimeZone tz) {
        if (!tz.equals(this._timezone)) {
            this._clearFormats();
            this._timezone = tz;
        }
    }

    @Override
    public void setLenient(boolean enabled) {
        Boolean newValue = enabled;
        if (!StdDateFormat._equals(newValue, this._lenient)) {
            this._lenient = newValue;
            this._clearFormats();
        }
    }

    @Override
    public boolean isLenient() {
        return this._lenient == null || this._lenient != false;
    }

    public boolean isColonIncludedInTimeZone() {
        return this._tzSerializedWithColon;
    }

    @Override
    public Date parse(String dateStr) throws ParseException {
        ParsePosition pos;
        Date dt = this._parseDate(dateStr = dateStr.trim(), pos = new ParsePosition(0));
        if (dt != null) {
            return dt;
        }
        StringBuilder sb = new StringBuilder();
        for (String f : ALL_FORMATS) {
            if (sb.length() > 0) {
                sb.append("\", \"");
            } else {
                sb.append('\"');
            }
            sb.append(f);
        }
        sb.append('\"');
        throw new ParseException(String.format("Cannot parse date \"%s\": not compatible with any of standard forms (%s)", dateStr, sb.toString()), pos.getErrorIndex());
    }

    @Override
    public Date parse(String dateStr, ParsePosition pos) {
        try {
            return this._parseDate(dateStr, pos);
        } catch (ParseException parseException) {
            return null;
        }
    }

    protected Date _parseDate(String dateStr, ParsePosition pos) throws ParseException {
        char ch;
        if (this.looksLikeISO8601(dateStr)) {
            return this.parseAsISO8601(dateStr, pos);
        }
        int i = dateStr.length();
        while (--i >= 0 && ((ch = dateStr.charAt(i)) >= '0' && ch <= '9' || i <= 0 && ch == '-')) {
        }
        if (i < 0 && (dateStr.charAt(0) == '-' || NumberInput.inLongRange(dateStr, false))) {
            return this._parseDateFromLong(dateStr, pos);
        }
        return this.parseAsRFC1123(dateStr, pos);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        TimeZone tz = this._timezone;
        if (tz == null) {
            tz = DEFAULT_TIMEZONE;
        }
        this._format(tz, this._locale, date, toAppendTo);
        return toAppendTo;
    }

    protected void _format(TimeZone tz, Locale loc, Date date, StringBuffer buffer) {
        Calendar cal = this._getCalendar(tz);
        cal.setTime(date);
        int year = cal.get(1);
        if (cal.get(0) == 0) {
            this._formatBCEYear(buffer, year);
        } else {
            if (year > 9999) {
                buffer.append('+');
            }
            StdDateFormat.pad4(buffer, year);
        }
        buffer.append('-');
        StdDateFormat.pad2(buffer, cal.get(2) + 1);
        buffer.append('-');
        StdDateFormat.pad2(buffer, cal.get(5));
        buffer.append('T');
        StdDateFormat.pad2(buffer, cal.get(11));
        buffer.append(':');
        StdDateFormat.pad2(buffer, cal.get(12));
        buffer.append(':');
        StdDateFormat.pad2(buffer, cal.get(13));
        buffer.append('.');
        StdDateFormat.pad3(buffer, cal.get(14));
        int offset = tz.getOffset(cal.getTimeInMillis());
        if (offset != 0) {
            int hours = Math.abs(offset / 60000 / 60);
            int minutes = Math.abs(offset / 60000 % 60);
            buffer.append(offset < 0 ? (char)'-' : '+');
            StdDateFormat.pad2(buffer, hours);
            if (this._tzSerializedWithColon) {
                buffer.append(':');
            }
            StdDateFormat.pad2(buffer, minutes);
        } else if (this._tzSerializedWithColon) {
            buffer.append("+00:00");
        } else {
            buffer.append("+0000");
        }
    }

    protected void _formatBCEYear(StringBuffer buffer, int bceYearNoSign) {
        if (bceYearNoSign == 1) {
            buffer.append("+0000");
            return;
        }
        int isoYear = bceYearNoSign - 1;
        buffer.append('-');
        StdDateFormat.pad4(buffer, isoYear);
    }

    private static void pad2(StringBuffer buffer, int value) {
        int tens = value / 10;
        if (tens == 0) {
            buffer.append('0');
        } else {
            buffer.append((char)(48 + tens));
            value -= 10 * tens;
        }
        buffer.append((char)(48 + value));
    }

    private static void pad3(StringBuffer buffer, int value) {
        int h = value / 100;
        if (h == 0) {
            buffer.append('0');
        } else {
            buffer.append((char)(48 + h));
            value -= h * 100;
        }
        StdDateFormat.pad2(buffer, value);
    }

    private static void pad4(StringBuffer buffer, int value) {
        int h = value / 100;
        if (h == 0) {
            buffer.append('0').append('0');
        } else {
            if (h > 99) {
                buffer.append(h);
            } else {
                StdDateFormat.pad2(buffer, h);
            }
            value -= 100 * h;
        }
        StdDateFormat.pad2(buffer, value);
    }

    public String toString() {
        return String.format("DateFormat %s: (timezone: %s, locale: %s, lenient: %s)", this.getClass().getName(), this._timezone, this._locale, this._lenient);
    }

    public String toPattern() {
        StringBuilder sb = new StringBuilder(100);
        sb.append("[one of: '").append(DATE_FORMAT_STR_ISO8601).append("', '").append(DATE_FORMAT_STR_RFC1123).append("' (");
        sb.append(Boolean.FALSE.equals(this._lenient) ? "strict" : "lenient").append(")]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    protected boolean looksLikeISO8601(String dateStr) {
        return dateStr.length() >= 7 && Character.isDigit(dateStr.charAt(0)) && Character.isDigit(dateStr.charAt(3)) && dateStr.charAt(4) == '-' && Character.isDigit(dateStr.charAt(5));
    }

    private Date _parseDateFromLong(String longStr, ParsePosition pos) throws ParseException {
        long ts;
        try {
            ts = NumberInput.parseLong(longStr);
        } catch (NumberFormatException e) {
            throw new ParseException(String.format("Timestamp value %s out of 64-bit value range", longStr), pos.getErrorIndex());
        }
        return new Date(ts);
    }

    protected Date parseAsISO8601(String dateStr, ParsePosition pos) throws ParseException {
        try {
            return this._parseAsISO8601(dateStr, pos);
        } catch (IllegalArgumentException e) {
            throw new ParseException(String.format("Cannot parse date \"%s\", problem: %s", dateStr, e.getMessage()), pos.getErrorIndex());
        }
    }

    protected Date _parseAsISO8601(String dateStr, ParsePosition bogus) throws IllegalArgumentException, ParseException {
        String formatStr;
        int totalLen = dateStr.length();
        TimeZone tz = DEFAULT_TIMEZONE;
        if (this._timezone != null && 'Z' != dateStr.charAt(totalLen - 1)) {
            tz = this._timezone;
        }
        Calendar cal = this._getCalendar(tz);
        cal.clear();
        if (totalLen <= 10) {
            Matcher m = PATTERN_PLAIN.matcher(dateStr);
            if (m.matches()) {
                int year = StdDateFormat._parse4D(dateStr, 0);
                int month = StdDateFormat._parse2D(dateStr, 5) - 1;
                int day = StdDateFormat._parse2D(dateStr, 8);
                cal.set(year, month, day, 0, 0, 0);
                cal.set(14, 0);
                return cal.getTime();
            }
            formatStr = DATE_FORMAT_STR_PLAIN;
        } else {
            Matcher m = PATTERN_ISO8601.matcher(dateStr);
            if (m.matches()) {
                int start = m.start(2);
                int end = m.end(2);
                int len = end - start;
                if (len > 1) {
                    int offsetSecs = StdDateFormat._parse2D(dateStr, start + 1) * 3600;
                    if (len >= 5) {
                        offsetSecs += StdDateFormat._parse2D(dateStr, end - 2) * 60;
                    }
                    offsetSecs = dateStr.charAt(start) == '-' ? (offsetSecs *= -1000) : (offsetSecs *= 1000);
                    cal.set(15, offsetSecs);
                    cal.set(16, 0);
                }
                int year = StdDateFormat._parse4D(dateStr, 0);
                int month = StdDateFormat._parse2D(dateStr, 5) - 1;
                int day = StdDateFormat._parse2D(dateStr, 8);
                int hour = StdDateFormat._parse2D(dateStr, 11);
                int minute = StdDateFormat._parse2D(dateStr, 14);
                int seconds = totalLen > 16 && dateStr.charAt(16) == ':' ? StdDateFormat._parse2D(dateStr, 17) : 0;
                cal.set(year, month, day, hour, minute, seconds);
                start = m.start(1) + 1;
                end = m.end(1);
                int msecs = 0;
                if (start >= end) {
                    cal.set(14, 0);
                } else {
                    msecs = 0;
                    int fractLen = end - start;
                    switch (fractLen) {
                        default: {
                            if (fractLen > 9) {
                                throw new ParseException(String.format("Cannot parse date \"%s\": invalid fractional seconds '%s'; can use at most 9 digits", dateStr, m.group(1).substring(1)), start);
                            }
                        }
                        case 3: {
                            msecs += dateStr.charAt(start + 2) - 48;
                        }
                        case 2: {
                            msecs += 10 * (dateStr.charAt(start + 1) - 48);
                        }
                        case 1: {
                            msecs += 100 * (dateStr.charAt(start) - 48);
                        }
                        case 0: 
                    }
                    cal.set(14, msecs);
                }
                return cal.getTime();
            }
            formatStr = DATE_FORMAT_STR_ISO8601;
        }
        throw new ParseException(String.format("Cannot parse date \"%s\": while it seems to fit format '%s', parsing fails (leniency? %s)", dateStr, formatStr, this._lenient), 0);
    }

    private static int _parse4D(String str, int index) {
        return 1000 * (str.charAt(index) - 48) + 100 * (str.charAt(index + 1) - 48) + 10 * (str.charAt(index + 2) - 48) + (str.charAt(index + 3) - 48);
    }

    private static int _parse2D(String str, int index) {
        return 10 * (str.charAt(index) - 48) + (str.charAt(index + 1) - 48);
    }

    protected Date parseAsRFC1123(String dateStr, ParsePosition pos) {
        if (this._formatRFC1123 == null) {
            this._formatRFC1123 = StdDateFormat._cloneFormat(DATE_FORMAT_RFC1123, DATE_FORMAT_STR_RFC1123, this._timezone, this._locale, this._lenient);
        }
        return this._formatRFC1123.parse(dateStr, pos);
    }

    private static final DateFormat _cloneFormat(DateFormat df, String format, TimeZone tz, Locale loc, Boolean lenient) {
        if (!loc.equals(DEFAULT_LOCALE)) {
            df = new SimpleDateFormat(format, loc);
            df.setTimeZone(tz == null ? DEFAULT_TIMEZONE : tz);
        } else {
            df = (DateFormat)df.clone();
            if (tz != null) {
                df.setTimeZone(tz);
            }
        }
        if (lenient != null) {
            df.setLenient(lenient);
        }
        return df;
    }

    protected void _clearFormats() {
        this._formatRFC1123 = null;
    }

    protected Calendar _getCalendar(TimeZone tz) {
        Calendar cal = this._calendar;
        if (cal == null) {
            this._calendar = cal = (Calendar)CALENDAR.clone();
        }
        if (!cal.getTimeZone().equals(tz)) {
            cal.setTimeZone(tz);
        }
        cal.setLenient(this.isLenient());
        return cal;
    }

    protected static <T> boolean _equals(T value1, T value2) {
        if (value1 == value2) {
            return true;
        }
        return value1 != null && value1.equals(value2);
    }

    static {
        Pattern p = null;
        try {
            p = Pattern.compile("\\d\\d\\d\\d[-]\\d\\d[-]\\d\\d[T]\\d\\d[:]\\d\\d(?:[:]\\d\\d)?(\\.\\d+)?(Z|[+-]\\d\\d(?:[:]?\\d\\d)?)?");
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        PATTERN_ISO8601 = p;
        ALL_FORMATS = new String[]{DATE_FORMAT_STR_ISO8601, "yyyy-MM-dd'T'HH:mm:ss.SSS", DATE_FORMAT_STR_RFC1123, DATE_FORMAT_STR_PLAIN};
        DEFAULT_TIMEZONE = TimeZone.getTimeZone("UTC");
        DEFAULT_LOCALE = Locale.US;
        DATE_FORMAT_RFC1123 = new SimpleDateFormat(DATE_FORMAT_STR_RFC1123, DEFAULT_LOCALE);
        DATE_FORMAT_RFC1123.setTimeZone(DEFAULT_TIMEZONE);
        instance = new StdDateFormat();
        CALENDAR = new GregorianCalendar(DEFAULT_TIMEZONE, DEFAULT_LOCALE);
    }
}

