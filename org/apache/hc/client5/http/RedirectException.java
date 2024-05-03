/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http;

import org.apache.hc.core5.http.ProtocolException;

public class RedirectException
extends ProtocolException {
    private static final long serialVersionUID = 4418824536372559326L;

    public RedirectException() {
    }

    public RedirectException(String message) {
        super(message);
    }

    public RedirectException(String message, Throwable cause) {
        super(message, cause);
    }
}

