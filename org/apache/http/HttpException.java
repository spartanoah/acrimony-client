/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http;

public class HttpException
extends Exception {
    private static final long serialVersionUID = -5437299376222011036L;

    public HttpException() {
    }

    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }
}

