/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.layout;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.pattern.PatternFormatter;

public interface PatternSelector {
    public static final String ELEMENT_TYPE = "patternSelector";

    public PatternFormatter[] getFormatters(LogEvent var1);
}

