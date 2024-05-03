/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.helpers;

import java.nio.charset.Charset;
import org.apache.logging.log4j.status.StatusLogger;

public final class Charsets {
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public static Charset getSupportedCharset(String charsetName) {
        return Charsets.getSupportedCharset(charsetName, Charset.defaultCharset());
    }

    public static Charset getSupportedCharset(String charsetName, Charset defaultCharset) {
        Charset charset = null;
        if (charsetName != null && Charset.isSupported(charsetName)) {
            charset = Charset.forName(charsetName);
        }
        if (charset == null) {
            charset = defaultCharset;
            if (charsetName != null) {
                StatusLogger.getLogger().error("Charset " + charsetName + " is not supported for layout, using " + charset.displayName());
            }
        }
        return charset;
    }

    private Charsets() {
    }
}

