/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.core.pattern.PatternConverter;

public interface ArrayPatternConverter
extends PatternConverter {
    public void format(StringBuilder var1, Object ... var2);
}

