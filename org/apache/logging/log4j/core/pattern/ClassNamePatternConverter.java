/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.NamePatternConverter;

@Plugin(name="ClassNamePatternConverter", category="Converter")
@ConverterKeys(value={"C", "class"})
public final class ClassNamePatternConverter
extends NamePatternConverter
implements LocationAware {
    private static final String NA = "?";

    private ClassNamePatternConverter(String[] options) {
        super("Class Name", "class name", options);
    }

    public static ClassNamePatternConverter newInstance(String[] options) {
        return new ClassNamePatternConverter(options);
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        StackTraceElement element = event.getSource();
        if (element == null) {
            toAppendTo.append(NA);
        } else {
            this.abbreviate(element.getClassName(), toAppendTo);
        }
    }

    @Override
    public boolean requiresLocation() {
        return true;
    }
}

