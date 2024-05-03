/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.util;

import org.apache.logging.log4j.message.MultiformatMessage;
import org.apache.logging.log4j.util.StringBuilderFormattable;

public interface MultiFormatStringBuilderFormattable
extends MultiformatMessage,
StringBuilderFormattable {
    public void formatTo(String[] var1, StringBuilder var2);
}

