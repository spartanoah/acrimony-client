/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http;

import java.io.IOException;

public class ContentTooLongException
extends IOException {
    private static final long serialVersionUID = -924287689552495383L;

    public ContentTooLongException(String message) {
        super(message);
    }
}

