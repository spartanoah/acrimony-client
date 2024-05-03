/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.GregorianCalendar;

@Deprecated
public class ISO8601DateFormat
extends DateFormat {
    private static final long serialVersionUID = 1L;

    public ISO8601DateFormat() {
        this.numberFormat = new DecimalFormat();
        this.calendar = new GregorianCalendar();
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        toAppendTo.append(ISO8601Utils.format(date));
        return toAppendTo;
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        try {
            return ISO8601Utils.parse(source, pos);
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public Date parse(String source) throws ParseException {
        return ISO8601Utils.parse(source, new ParsePosition(0));
    }

    @Override
    public Object clone() {
        return this;
    }
}

