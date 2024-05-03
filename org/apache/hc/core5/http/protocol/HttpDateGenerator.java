/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;

@Contract(threading=ThreadingBehavior.SAFE)
public class HttpDateGenerator {
    private static final int GRANULARITY_MILLIS = 1000;
    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    public static final HttpDateGenerator INSTANCE = new HttpDateGenerator("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US, GMT);
    private final DateFormat dateformat;
    private long dateAsMillis = 0L;
    private String dateAsText = null;

    HttpDateGenerator() {
        this.dateformat = new SimpleDateFormat(PATTERN_RFC1123, Locale.US);
        this.dateformat.setTimeZone(GMT);
    }

    private HttpDateGenerator(String pattern, Locale locale, TimeZone timeZone) {
        this.dateformat = new SimpleDateFormat(pattern, locale);
        this.dateformat.setTimeZone(timeZone);
    }

    public synchronized String getCurrentDate() {
        long now = System.currentTimeMillis();
        if (now - this.dateAsMillis > 1000L) {
            this.dateAsText = this.dateformat.format(new Date(now));
            this.dateAsMillis = now;
        }
        return this.dateAsText;
    }
}

