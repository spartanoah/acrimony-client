/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import org.apache.hc.core5.http.config.CharCodingConfig;

public final class CharCodingSupport {
    private CharCodingSupport() {
    }

    public static CharsetDecoder createDecoder(CharCodingConfig cconfig) {
        if (cconfig == null) {
            return null;
        }
        Charset charset = cconfig.getCharset();
        CodingErrorAction malformed = cconfig.getMalformedInputAction();
        CodingErrorAction unmappable = cconfig.getUnmappableInputAction();
        if (charset != null) {
            return charset.newDecoder().onMalformedInput(malformed != null ? malformed : CodingErrorAction.REPORT).onUnmappableCharacter(unmappable != null ? unmappable : CodingErrorAction.REPORT);
        }
        return null;
    }

    public static CharsetEncoder createEncoder(CharCodingConfig cconfig) {
        if (cconfig == null) {
            return null;
        }
        Charset charset = cconfig.getCharset();
        if (charset != null) {
            CodingErrorAction malformed = cconfig.getMalformedInputAction();
            CodingErrorAction unmappable = cconfig.getUnmappableInputAction();
            return charset.newEncoder().onMalformedInput(malformed != null ? malformed : CodingErrorAction.REPORT).onUnmappableCharacter(unmappable != null ? unmappable : CodingErrorAction.REPORT);
        }
        return null;
    }
}

