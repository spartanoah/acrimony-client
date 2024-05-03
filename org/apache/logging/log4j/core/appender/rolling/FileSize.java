/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender.rolling;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;

public final class FileSize {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final long KB = 1024L;
    private static final long MB = 0x100000L;
    private static final long GB = 0x40000000L;
    private static final long TB = 0x10000000000L;
    private static final Pattern VALUE_PATTERN = Pattern.compile("([0-9]+([.,][0-9]+)?)\\s*(|K|M|G|T)B?", 2);

    private FileSize() {
    }

    public static long parse(String string, long defaultValue) {
        Matcher matcher = VALUE_PATTERN.matcher(string);
        if (matcher.matches()) {
            try {
                String quantityString = matcher.group(1);
                double quantity = NumberFormat.getNumberInstance(Locale.ROOT).parse(quantityString).doubleValue();
                String unit = matcher.group(3);
                if (unit == null || unit.isEmpty()) {
                    return (long)quantity;
                }
                if (unit.equalsIgnoreCase("K")) {
                    return (long)(quantity * 1024.0);
                }
                if (unit.equalsIgnoreCase("M")) {
                    return (long)(quantity * 1048576.0);
                }
                if (unit.equalsIgnoreCase("G")) {
                    return (long)(quantity * 1.073741824E9);
                }
                if (unit.equalsIgnoreCase("T")) {
                    return (long)(quantity * 1.099511627776E12);
                }
                LOGGER.error("FileSize units not recognized: " + string);
                return defaultValue;
            } catch (ParseException error) {
                LOGGER.error("FileSize unable to parse numeric part: " + string, (Throwable)error);
                return defaultValue;
            }
        }
        LOGGER.error("FileSize unable to parse bytes: " + string);
        return defaultValue;
    }
}

