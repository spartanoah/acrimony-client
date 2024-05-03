/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.util;

import java.io.Serializable;

public class DateTimeRule
implements Serializable {
    private static final long serialVersionUID = 2183055795738051443L;
    public static final int DOM = 0;
    public static final int DOW = 1;
    public static final int DOW_GEQ_DOM = 2;
    public static final int DOW_LEQ_DOM = 3;
    public static final int WALL_TIME = 0;
    public static final int STANDARD_TIME = 1;
    public static final int UTC_TIME = 2;
    private final int dateRuleType;
    private final int month;
    private final int dayOfMonth;
    private final int dayOfWeek;
    private final int weekInMonth;
    private final int timeRuleType;
    private final int millisInDay;
    private static final String[] DOWSTR = new String[]{"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final String[] MONSTR = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    public DateTimeRule(int month, int dayOfMonth, int millisInDay, int timeType) {
        this.dateRuleType = 0;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.millisInDay = millisInDay;
        this.timeRuleType = timeType;
        this.dayOfWeek = 0;
        this.weekInMonth = 0;
    }

    public DateTimeRule(int month, int weekInMonth, int dayOfWeek, int millisInDay, int timeType) {
        this.dateRuleType = 1;
        this.month = month;
        this.weekInMonth = weekInMonth;
        this.dayOfWeek = dayOfWeek;
        this.millisInDay = millisInDay;
        this.timeRuleType = timeType;
        this.dayOfMonth = 0;
    }

    public DateTimeRule(int month, int dayOfMonth, int dayOfWeek, boolean after, int millisInDay, int timeType) {
        this.dateRuleType = after ? 2 : 3;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        this.millisInDay = millisInDay;
        this.timeRuleType = timeType;
        this.weekInMonth = 0;
    }

    public int getDateRuleType() {
        return this.dateRuleType;
    }

    public int getRuleMonth() {
        return this.month;
    }

    public int getRuleDayOfMonth() {
        return this.dayOfMonth;
    }

    public int getRuleDayOfWeek() {
        return this.dayOfWeek;
    }

    public int getRuleWeekInMonth() {
        return this.weekInMonth;
    }

    public int getTimeRuleType() {
        return this.timeRuleType;
    }

    public int getRuleMillisInDay() {
        return this.millisInDay;
    }

    public String toString() {
        String sDate = null;
        String sTimeRuleType = null;
        switch (this.dateRuleType) {
            case 0: {
                sDate = Integer.toString(this.dayOfMonth);
                break;
            }
            case 1: {
                sDate = Integer.toString(this.weekInMonth) + DOWSTR[this.dayOfWeek];
                break;
            }
            case 2: {
                sDate = DOWSTR[this.dayOfWeek] + ">=" + Integer.toString(this.dayOfMonth);
                break;
            }
            case 3: {
                sDate = DOWSTR[this.dayOfWeek] + "<=" + Integer.toString(this.dayOfMonth);
            }
        }
        switch (this.timeRuleType) {
            case 0: {
                sTimeRuleType = "WALL";
                break;
            }
            case 1: {
                sTimeRuleType = "STD";
                break;
            }
            case 2: {
                sTimeRuleType = "UTC";
            }
        }
        int time = this.millisInDay;
        int millis = time % 1000;
        int secs = (time /= 1000) % 60;
        int mins = (time /= 60) % 60;
        int hours = time / 60;
        StringBuilder buf = new StringBuilder();
        buf.append("month=");
        buf.append(MONSTR[this.month]);
        buf.append(", date=");
        buf.append(sDate);
        buf.append(", time=");
        buf.append(hours);
        buf.append(":");
        buf.append(mins / 10);
        buf.append(mins % 10);
        buf.append(":");
        buf.append(secs / 10);
        buf.append(secs % 10);
        buf.append(".");
        buf.append(millis / 100);
        buf.append(millis / 10 % 10);
        buf.append(millis % 10);
        buf.append("(");
        buf.append(sTimeRuleType);
        buf.append(")");
        return buf.toString();
    }
}

