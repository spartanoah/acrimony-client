/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.impl;

import com.ibm.icu.util.TimeZone;
import java.util.Date;

public class TimeZoneAdapter
extends java.util.TimeZone {
    static final long serialVersionUID = -2040072218820018557L;
    private TimeZone zone;

    public static java.util.TimeZone wrap(TimeZone tz) {
        return new TimeZoneAdapter(tz);
    }

    public TimeZone unwrap() {
        return this.zone;
    }

    public TimeZoneAdapter(TimeZone zone) {
        this.zone = zone;
        super.setID(zone.getID());
    }

    public void setID(String ID2) {
        super.setID(ID2);
        this.zone.setID(ID2);
    }

    public boolean hasSameRules(java.util.TimeZone other) {
        return other instanceof TimeZoneAdapter && this.zone.hasSameRules(((TimeZoneAdapter)other).zone);
    }

    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
        return this.zone.getOffset(era, year, month, day, dayOfWeek, millis);
    }

    public int getRawOffset() {
        return this.zone.getRawOffset();
    }

    public void setRawOffset(int offsetMillis) {
        this.zone.setRawOffset(offsetMillis);
    }

    public boolean useDaylightTime() {
        return this.zone.useDaylightTime();
    }

    public boolean inDaylightTime(Date date) {
        return this.zone.inDaylightTime(date);
    }

    public Object clone() {
        return new TimeZoneAdapter((TimeZone)this.zone.clone());
    }

    public synchronized int hashCode() {
        return this.zone.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof TimeZoneAdapter) {
            obj = ((TimeZoneAdapter)obj).zone;
        }
        return this.zone.equals(obj);
    }

    public String toString() {
        return "TimeZoneAdapter: " + this.zone.toString();
    }
}

