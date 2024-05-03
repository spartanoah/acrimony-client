/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

@Plugin(name="LineLocationPatternConverter", category="Converter")
@ConverterKeys(value={"L", "line"})
public final class LineLocationPatternConverter
extends LogEventPatternConverter
implements LocationAware {
    private static final LineLocationPatternConverter INSTANCE = new LineLocationPatternConverter();

    private LineLocationPatternConverter() {
        super("Line", "line");
    }

    public static LineLocationPatternConverter newInstance(String[] options) {
        return INSTANCE;
    }

    @Override
    public void format(LogEvent event, StringBuilder output) {
        StackTraceElement element = event.getSource();
        if (element != null) {
            output.append(element.getLineNumber());
        }
    }

    @Override
    public boolean requiresLocation() {
        return true;
    }
}

