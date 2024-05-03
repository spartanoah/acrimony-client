/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.util.datetime;

import java.text.FieldPosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public interface DatePrinter {
    public String format(long var1);

    public String format(Date var1);

    public String format(Calendar var1);

    public <B extends Appendable> B format(long var1, B var3);

    public <B extends Appendable> B format(Date var1, B var2);

    public <B extends Appendable> B format(Calendar var1, B var2);

    public String getPattern();

    public TimeZone getTimeZone();

    public Locale getLocale();

    public StringBuilder format(Object var1, StringBuilder var2, FieldPosition var3);
}

