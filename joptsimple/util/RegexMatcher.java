/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package joptsimple.util;

import java.util.regex.Pattern;
import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class RegexMatcher
implements ValueConverter<String> {
    private final Pattern pattern;

    public RegexMatcher(String pattern, int flags) {
        this.pattern = Pattern.compile(pattern, flags);
    }

    public static ValueConverter<String> regex(String pattern) {
        return new RegexMatcher(pattern, 0);
    }

    @Override
    public String convert(String value) {
        if (!this.pattern.matcher(value).matches()) {
            throw new ValueConversionException("Value [" + value + "] did not match regex [" + this.pattern.pattern() + ']');
        }
        return value;
    }

    @Override
    public Class<String> valueType() {
        return String.class;
    }

    @Override
    public String valuePattern() {
        return this.pattern.pattern();
    }
}

