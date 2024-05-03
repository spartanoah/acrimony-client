/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.config;

import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class CharCodingConfig {
    public static final CharCodingConfig DEFAULT = new Builder().build();
    private final Charset charset;
    private final CodingErrorAction malformedInputAction;
    private final CodingErrorAction unmappableInputAction;

    CharCodingConfig(Charset charset, CodingErrorAction malformedInputAction, CodingErrorAction unmappableInputAction) {
        this.charset = charset;
        this.malformedInputAction = malformedInputAction;
        this.unmappableInputAction = unmappableInputAction;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public CodingErrorAction getMalformedInputAction() {
        return this.malformedInputAction;
    }

    public CodingErrorAction getUnmappableInputAction() {
        return this.unmappableInputAction;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[charset=").append(this.charset).append(", malformedInputAction=").append(this.malformedInputAction).append(", unmappableInputAction=").append(this.unmappableInputAction).append("]");
        return builder.toString();
    }

    public static Builder custom() {
        return new Builder();
    }

    public static Builder copy(CharCodingConfig config) {
        Args.notNull(config, "Config");
        return new Builder().setCharset(config.getCharset()).setMalformedInputAction(config.getMalformedInputAction()).setUnmappableInputAction(config.getUnmappableInputAction());
    }

    public static class Builder {
        private Charset charset;
        private CodingErrorAction malformedInputAction;
        private CodingErrorAction unmappableInputAction;

        Builder() {
        }

        public Builder setCharset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder setMalformedInputAction(CodingErrorAction malformedInputAction) {
            this.malformedInputAction = malformedInputAction;
            if (malformedInputAction != null && this.charset == null) {
                this.charset = StandardCharsets.US_ASCII;
            }
            return this;
        }

        public Builder setUnmappableInputAction(CodingErrorAction unmappableInputAction) {
            this.unmappableInputAction = unmappableInputAction;
            if (unmappableInputAction != null && this.charset == null) {
                this.charset = StandardCharsets.US_ASCII;
            }
            return this;
        }

        public CharCodingConfig build() {
            Charset charsetCopy = this.charset;
            if (charsetCopy == null && (this.malformedInputAction != null || this.unmappableInputAction != null)) {
                charsetCopy = StandardCharsets.US_ASCII;
            }
            return new CharCodingConfig(charsetCopy, this.malformedInputAction, this.unmappableInputAction);
        }
    }
}

