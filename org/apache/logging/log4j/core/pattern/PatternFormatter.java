/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.pattern.FormattingInfo;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

public class PatternFormatter {
    public static final PatternFormatter[] EMPTY_ARRAY = new PatternFormatter[0];
    private final LogEventPatternConverter converter;
    private final FormattingInfo field;
    private final boolean skipFormattingInfo;

    public PatternFormatter(LogEventPatternConverter converter, FormattingInfo field) {
        this.converter = converter;
        this.field = field;
        this.skipFormattingInfo = field == FormattingInfo.getDefault();
    }

    public void format(LogEvent event, StringBuilder buf) {
        if (this.skipFormattingInfo) {
            this.converter.format(event, buf);
        } else {
            this.formatWithInfo(event, buf);
        }
    }

    private void formatWithInfo(LogEvent event, StringBuilder buf) {
        int startField = buf.length();
        this.converter.format(event, buf);
        this.field.format(startField, buf);
    }

    public LogEventPatternConverter getConverter() {
        return this.converter;
    }

    public FormattingInfo getFormattingInfo() {
        return this.field;
    }

    public boolean handlesThrowable() {
        return this.converter.handlesThrowable();
    }

    public boolean requiresLocation() {
        return this.converter instanceof LocationAware && ((LocationAware)((Object)this.converter)).requiresLocation();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("[converter=");
        sb.append(this.converter);
        sb.append(", field=");
        sb.append(this.field);
        sb.append(']');
        return sb.toString();
    }
}

