/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

@Plugin(name="MethodLocationPatternConverter", category="Converter")
@ConverterKeys(value={"M", "method"})
public final class MethodLocationPatternConverter
extends LogEventPatternConverter
implements LocationAware {
    private static final MethodLocationPatternConverter INSTANCE = new MethodLocationPatternConverter();

    private MethodLocationPatternConverter() {
        super("Method", "method");
    }

    public static MethodLocationPatternConverter newInstance(String[] options) {
        return INSTANCE;
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        StackTraceElement element = event.getSource();
        if (element != null) {
            toAppendTo.append(element.getMethodName());
        }
    }

    @Override
    public boolean requiresLocation() {
        return true;
    }
}

