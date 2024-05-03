/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package joptsimple.util;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class DateConverter
implements ValueConverter<Date> {
    private final DateFormat formatter;

    public DateConverter(DateFormat formatter) {
        if (formatter == null) {
            throw new NullPointerException("illegal null formatter");
        }
        this.formatter = formatter;
    }

    public static DateConverter datePattern(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setLenient(false);
        return new DateConverter(formatter);
    }

    @Override
    public Date convert(String value) {
        ParsePosition position = new ParsePosition(0);
        Date date = this.formatter.parse(value, position);
        if (position.getIndex() != value.length()) {
            throw new ValueConversionException(this.message(value));
        }
        return date;
    }

    @Override
    public Class<Date> valueType() {
        return Date.class;
    }

    @Override
    public String valuePattern() {
        return this.formatter instanceof SimpleDateFormat ? ((SimpleDateFormat)this.formatter).toPattern() : "";
    }

    private String message(String value) {
        String message = "Value [" + value + "] does not match date/time pattern";
        if (this.formatter instanceof SimpleDateFormat) {
            message = message + " [" + ((SimpleDateFormat)this.formatter).toPattern() + ']';
        }
        return message;
    }
}

