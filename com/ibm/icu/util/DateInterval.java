/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.util;

import java.io.Serializable;

public final class DateInterval
implements Serializable {
    private static final long serialVersionUID = 1L;
    private final long fromDate;
    private final long toDate;

    public DateInterval(long from, long to) {
        this.fromDate = from;
        this.toDate = to;
    }

    public long getFromDate() {
        return this.fromDate;
    }

    public long getToDate() {
        return this.toDate;
    }

    public boolean equals(Object a) {
        if (a instanceof DateInterval) {
            DateInterval di = (DateInterval)a;
            return this.fromDate == di.fromDate && this.toDate == di.toDate;
        }
        return false;
    }

    public int hashCode() {
        return (int)(this.fromDate + this.toDate);
    }

    public String toString() {
        return String.valueOf(this.fromDate) + " " + String.valueOf(this.toDate);
    }
}

