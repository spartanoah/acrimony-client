/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.Strings;

@Plugin(name="LineSeparatorPatternConverter", category="Converter")
@ConverterKeys(value={"n"})
@PerformanceSensitive(value={"allocation"})
public final class LineSeparatorPatternConverter
extends LogEventPatternConverter {
    private static final LineSeparatorPatternConverter INSTANCE = new LineSeparatorPatternConverter();

    private LineSeparatorPatternConverter() {
        super("Line Sep", "lineSep");
    }

    public static LineSeparatorPatternConverter newInstance(String[] options) {
        return INSTANCE;
    }

    @Override
    public void format(LogEvent ignored, StringBuilder toAppendTo) {
        toAppendTo.append(Strings.LINE_SEPARATOR);
    }

    @Override
    public void format(Object ignored, StringBuilder output) {
        output.append(Strings.LINE_SEPARATOR);
    }

    @Override
    public boolean isVariable() {
        return false;
    }
}

