/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cookie;

import java.util.BitSet;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hc.client5.http.cookie.CommonCookieAttributeHandler;
import org.apache.hc.client5.http.cookie.MalformedCookieException;
import org.apache.hc.client5.http.cookie.SetCookie;
import org.apache.hc.client5.http.impl.cookie.AbstractCookieAttributeHandler;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TextUtils;

@Contract(threading=ThreadingBehavior.STATELESS)
public class LaxExpiresHandler
extends AbstractCookieAttributeHandler
implements CommonCookieAttributeHandler {
    static final TimeZone UTC;
    private static final BitSet DELIMS;
    private static final Map<String, Integer> MONTHS;
    private static final Pattern TIME_PATTERN;
    private static final Pattern DAY_OF_MONTH_PATTERN;
    private static final Pattern MONTH_PATTERN;
    private static final Pattern YEAR_PATTERN;

    @Override
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        if (TextUtils.isBlank(value)) {
            return;
        }
        ParserCursor cursor = new ParserCursor(0, value.length());
        StringBuilder content = new StringBuilder();
        int second = 0;
        int minute = 0;
        int hour = 0;
        int day = 0;
        int month = 0;
        int year = 0;
        boolean foundTime = false;
        boolean foundDayOfMonth = false;
        boolean foundMonth = false;
        boolean foundYear = false;
        try {
            while (!cursor.atEnd()) {
                this.skipDelims(value, cursor);
                content.setLength(0);
                this.copyContent(value, cursor, content);
                if (content.length() != 0) {
                    Matcher matcher;
                    if (!foundTime && (matcher = TIME_PATTERN.matcher(content)).matches()) {
                        foundTime = true;
                        hour = Integer.parseInt(matcher.group(1));
                        minute = Integer.parseInt(matcher.group(2));
                        second = Integer.parseInt(matcher.group(3));
                        continue;
                    }
                    if (!foundDayOfMonth && (matcher = DAY_OF_MONTH_PATTERN.matcher(content)).matches()) {
                        foundDayOfMonth = true;
                        day = Integer.parseInt(matcher.group(1));
                        continue;
                    }
                    if (!foundMonth && (matcher = MONTH_PATTERN.matcher(content)).matches()) {
                        foundMonth = true;
                        month = MONTHS.get(matcher.group(1).toLowerCase(Locale.ROOT));
                        continue;
                    }
                    if (foundYear || !(matcher = YEAR_PATTERN.matcher(content)).matches()) continue;
                    foundYear = true;
                    year = Integer.parseInt(matcher.group(1));
                    continue;
                }
                break;
            }
        } catch (NumberFormatException ignore) {
            throw new MalformedCookieException("Invalid 'expires' attribute: " + value);
        }
        if (!(foundTime && foundDayOfMonth && foundMonth && foundYear)) {
            throw new MalformedCookieException("Invalid 'expires' attribute: " + value);
        }
        if (year >= 70 && year <= 99) {
            year = 1900 + year;
        }
        if (year >= 0 && year <= 69) {
            year = 2000 + year;
        }
        if (day < 1 || day > 31 || year < 1601 || hour > 23 || minute > 59 || second > 59) {
            throw new MalformedCookieException("Invalid 'expires' attribute: " + value);
        }
        Calendar c = Calendar.getInstance();
        c.setTimeZone(UTC);
        c.setTimeInMillis(0L);
        c.set(13, second);
        c.set(12, minute);
        c.set(11, hour);
        c.set(5, day);
        c.set(2, month);
        c.set(1, year);
        cookie.setExpiryDate(c.getTime());
    }

    private void skipDelims(CharSequence buf, ParserCursor cursor) {
        char current;
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        for (int i = indexFrom; i < indexTo && DELIMS.get(current = buf.charAt(i)); ++i) {
            ++pos;
        }
        cursor.updatePos(pos);
    }

    private void copyContent(CharSequence buf, ParserCursor cursor, StringBuilder dst) {
        char current;
        int pos = cursor.getPos();
        int indexFrom = cursor.getPos();
        int indexTo = cursor.getUpperBound();
        for (int i = indexFrom; i < indexTo && !DELIMS.get(current = buf.charAt(i)); ++i) {
            ++pos;
            dst.append(current);
        }
        cursor.updatePos(pos);
    }

    @Override
    public String getAttributeName() {
        return "expires";
    }

    static {
        int b;
        UTC = TimeZone.getTimeZone("UTC");
        BitSet bitSet = new BitSet();
        bitSet.set(9);
        for (b = 32; b <= 47; ++b) {
            bitSet.set(b);
        }
        for (b = 59; b <= 64; ++b) {
            bitSet.set(b);
        }
        for (b = 91; b <= 96; ++b) {
            bitSet.set(b);
        }
        for (b = 123; b <= 126; ++b) {
            bitSet.set(b);
        }
        DELIMS = bitSet;
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<String, Integer>(12);
        map.put("jan", 0);
        map.put("feb", 1);
        map.put("mar", 2);
        map.put("apr", 3);
        map.put("may", 4);
        map.put("jun", 5);
        map.put("jul", 6);
        map.put("aug", 7);
        map.put("sep", 8);
        map.put("oct", 9);
        map.put("nov", 10);
        map.put("dec", 11);
        MONTHS = map;
        TIME_PATTERN = Pattern.compile("^([0-9]{1,2}):([0-9]{1,2}):([0-9]{1,2})([^0-9].*)?$");
        DAY_OF_MONTH_PATTERN = Pattern.compile("^([0-9]{1,2})([^0-9].*)?$");
        MONTH_PATTERN = Pattern.compile("^(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)(.*)?$", 2);
        YEAR_PATTERN = Pattern.compile("^([0-9]{2,4})([^0-9].*)?$");
    }
}

