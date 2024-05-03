/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.io.IOException;
import org.apache.hc.core5.http.HttpException;

public class ContentTooLongException
extends IOException {
    private static final long serialVersionUID = -924287689552495383L;

    public ContentTooLongException(String message) {
        super(HttpException.clean(message));
    }

    public ContentTooLongException(String format, Object ... args) {
        super(HttpException.clean(String.format(format, args)));
    }
}

