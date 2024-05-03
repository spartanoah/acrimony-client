/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.text;

import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class CurrencyMetaInfo {
    private static final CurrencyMetaInfo impl;
    private static final boolean hasData;
    protected static final CurrencyDigits defaultDigits;

    public static CurrencyMetaInfo getInstance() {
        return impl;
    }

    public static CurrencyMetaInfo getInstance(boolean noSubstitute) {
        return hasData ? impl : null;
    }

    public static boolean hasData() {
        return hasData;
    }

    protected CurrencyMetaInfo() {
    }

    public List<CurrencyInfo> currencyInfo(CurrencyFilter filter) {
        return Collections.emptyList();
    }

    public List<String> currencies(CurrencyFilter filter) {
        return Collections.emptyList();
    }

    public List<String> regions(CurrencyFilter filter) {
        return Collections.emptyList();
    }

    public CurrencyDigits currencyDigits(String isoCode) {
        return defaultDigits;
    }

    private static String dateString(long date) {
        if (date == Long.MAX_VALUE || date == Long.MIN_VALUE) {
            return null;
        }
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeZone(TimeZone.getTimeZone("GMT"));
        gc.setTimeInMillis(date);
        return "" + gc.get(1) + '-' + (gc.get(2) + 1) + '-' + gc.get(5);
    }

    private static String debugString(Object o) {
        StringBuilder sb = new StringBuilder();
        try {
            for (Field f : o.getClass().getFields()) {
                String s;
                Object v = f.get(o);
                if (v == null || (s = v instanceof Date ? CurrencyMetaInfo.dateString(((Date)v).getTime()) : (v instanceof Long ? CurrencyMetaInfo.dateString((Long)v) : String.valueOf(v))) == null) continue;
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(f.getName()).append("='").append(s).append("'");
            }
        } catch (Throwable throwable) {
            // empty catch block
        }
        sb.insert(0, o.getClass().getSimpleName() + "(");
        sb.append(")");
        return sb.toString();
    }

    static {
        defaultDigits = new CurrencyDigits(2, 0);
        CurrencyMetaInfo temp = null;
        boolean tempHasData = false;
        try {
            Class<?> clzz = Class.forName("com.ibm.icu.impl.ICUCurrencyMetaInfo");
            temp = (CurrencyMetaInfo)clzz.newInstance();
            tempHasData = true;
        } catch (Throwable t) {
            temp = new CurrencyMetaInfo();
        }
        impl = temp;
        hasData = tempHasData;
    }

    public static final class CurrencyInfo {
        public final String region;
        public final String code;
        public final long from;
        public final long to;
        public final int priority;
        private final boolean tender;

        public CurrencyInfo(String region, String code, long from, long to, int priority) {
            this(region, code, from, to, priority, true);
        }

        public CurrencyInfo(String region, String code, long from, long to, int priority, boolean tender) {
            this.region = region;
            this.code = code;
            this.from = from;
            this.to = to;
            this.priority = priority;
            this.tender = tender;
        }

        public String toString() {
            return CurrencyMetaInfo.debugString(this);
        }

        public boolean isTender() {
            return this.tender;
        }
    }

    public static final class CurrencyDigits {
        public final int fractionDigits;
        public final int roundingIncrement;

        public CurrencyDigits(int fractionDigits, int roundingIncrement) {
            this.fractionDigits = fractionDigits;
            this.roundingIncrement = roundingIncrement;
        }

        public String toString() {
            return CurrencyMetaInfo.debugString(this);
        }
    }

    public static final class CurrencyFilter {
        public final String region;
        public final String currency;
        public final long from;
        public final long to;
        public final boolean tenderOnly;
        private static final CurrencyFilter ALL = new CurrencyFilter(null, null, Long.MIN_VALUE, Long.MAX_VALUE, false);

        private CurrencyFilter(String region, String currency, long from, long to, boolean tenderOnly) {
            this.region = region;
            this.currency = currency;
            this.from = from;
            this.to = to;
            this.tenderOnly = tenderOnly;
        }

        public static CurrencyFilter all() {
            return ALL;
        }

        public static CurrencyFilter now() {
            return ALL.withDate(new Date());
        }

        public static CurrencyFilter onRegion(String region) {
            return ALL.withRegion(region);
        }

        public static CurrencyFilter onCurrency(String currency) {
            return ALL.withCurrency(currency);
        }

        public static CurrencyFilter onDate(Date date) {
            return ALL.withDate(date);
        }

        public static CurrencyFilter onDateRange(Date from, Date to) {
            return ALL.withDateRange(from, to);
        }

        public static CurrencyFilter onDate(long date) {
            return ALL.withDate(date);
        }

        public static CurrencyFilter onDateRange(long from, long to) {
            return ALL.withDateRange(from, to);
        }

        public static CurrencyFilter onTender() {
            return ALL.withTender();
        }

        public CurrencyFilter withRegion(String region) {
            return new CurrencyFilter(region, this.currency, this.from, this.to, this.tenderOnly);
        }

        public CurrencyFilter withCurrency(String currency) {
            return new CurrencyFilter(this.region, currency, this.from, this.to, this.tenderOnly);
        }

        public CurrencyFilter withDate(Date date) {
            return new CurrencyFilter(this.region, this.currency, date.getTime(), date.getTime(), this.tenderOnly);
        }

        public CurrencyFilter withDateRange(Date from, Date to) {
            long fromLong = from == null ? Long.MIN_VALUE : from.getTime();
            long toLong = to == null ? Long.MAX_VALUE : to.getTime();
            return new CurrencyFilter(this.region, this.currency, fromLong, toLong, this.tenderOnly);
        }

        public CurrencyFilter withDate(long date) {
            return new CurrencyFilter(this.region, this.currency, date, date, this.tenderOnly);
        }

        public CurrencyFilter withDateRange(long from, long to) {
            return new CurrencyFilter(this.region, this.currency, from, to, this.tenderOnly);
        }

        public CurrencyFilter withTender() {
            return new CurrencyFilter(this.region, this.currency, this.from, this.to, true);
        }

        public boolean equals(Object rhs) {
            return rhs instanceof CurrencyFilter && this.equals((CurrencyFilter)rhs);
        }

        public boolean equals(CurrencyFilter rhs) {
            return this == rhs || rhs != null && CurrencyFilter.equals(this.region, rhs.region) && CurrencyFilter.equals(this.currency, rhs.currency) && this.from == rhs.from && this.to == rhs.to && this.tenderOnly == rhs.tenderOnly;
        }

        public int hashCode() {
            int hc = 0;
            if (this.region != null) {
                hc = this.region.hashCode();
            }
            if (this.currency != null) {
                hc = hc * 31 + this.currency.hashCode();
            }
            hc = hc * 31 + (int)this.from;
            hc = hc * 31 + (int)(this.from >>> 32);
            hc = hc * 31 + (int)this.to;
            hc = hc * 31 + (int)(this.to >>> 32);
            hc = hc * 31 + (this.tenderOnly ? 1 : 0);
            return hc;
        }

        public String toString() {
            return CurrencyMetaInfo.debugString(this);
        }

        private static boolean equals(String lhs, String rhs) {
            return lhs == rhs || lhs != null && lhs.equals(rhs);
        }
    }
}

