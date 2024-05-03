/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.io.IOException;
import org.apache.hc.core5.http.HttpException;

public class NoHttpResponseException
extends IOException {
    private static final long serialVersionUID = -7658940387386078766L;

    public NoHttpResponseException(String message) {
        super(HttpException.clean(message));
    }

    public NoHttpResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}

