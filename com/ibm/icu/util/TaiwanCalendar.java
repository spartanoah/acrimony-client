/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.util;

import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;
import java.util.Date;
import java.util.Locale;

public class TaiwanCalendar
extends GregorianCalendar {
    private static final long serialVersionUID = 2583005278132380631L;
    public static final int BEFORE_MINGUO = 0;
    public static final int MINGUO = 1;
    private static final int Taiwan_ERA_START = 1911;
    private static final int GREGORIAN_EPOCH = 1970;

    public TaiwanCalendar() {
    }

    public TaiwanCalendar(TimeZone zone) {
        super(zone);
    }

    public TaiwanCalendar(Locale aLocale) {
        super(aLocale);
    }

    public TaiwanCalendar(ULocale locale) {
        super(locale);
    }

    public TaiwanCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
    }

    public TaiwanCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
    }

    public TaiwanCalendar(Date date) {
        this();
        this.setTime(date);
    }

    public TaiwanCalendar(int year, int month, int date) {
        super(year, month, date);
    }

    public TaiwanCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(year, month, date, hour, minute, second);
    }

    protected int handleGetExtendedYear() {
        int era;
        int year = 1970;
        year = this.newerField(19, 1) == 19 && this.newerField(19, 0) == 19 ? this.internalGet(19, 1970) : ((era = this.internalGet(0, 1)) == 1 ? this.internalGet(1, 1) + 1911 : 1 - this.internalGet(1, 1) + 1911);
        return year;
    }

    protected void handleComputeFields(int julianDay) {
        super.handleComputeFields(julianDay);
        int y = this.internalGet(19) - 1911;
        if (y > 0) {
            this.internalSet(0, 1);
            this.internalSet(1, y);
        } else {
            this.internalSet(0, 0);
            this.internalSet(1, 1 - y);
        }
    }

    protected int handleGetLimit(int field, int limitType) {
        if (field == 0) {
            if (limitType == 0 || limitType == 1) {
                return 0;
            }
            return 1;
        }
        return super.handleGetLimit(field, limitType);
    }

    public String getType() {
        return "roc";
    }
}

