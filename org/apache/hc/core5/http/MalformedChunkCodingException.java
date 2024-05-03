/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.io.IOException;
import org.apache.hc.core5.http.HttpException;

public class MalformedChunkCodingException
extends IOException {
    private static final long serialVersionUID = 2158560246948994524L;

    public MalformedChunkCodingException() {
    }

    public MalformedChunkCodingException(String message) {
        super(message);
    }

    public MalformedChunkCodingException(String format, Object ... args) {
        super(HttpException.clean(String.format(format, args)));
    }
}

