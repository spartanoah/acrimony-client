/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cache;

public class HttpCacheUpdateException
extends Exception {
    private static final long serialVersionUID = 823573584868632876L;

    public HttpCacheUpdateException(String message) {
        super(message);
    }

    public HttpCacheUpdateException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }
}

