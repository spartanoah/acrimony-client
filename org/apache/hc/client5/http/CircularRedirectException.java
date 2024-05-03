/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http;

import org.apache.hc.client5.http.RedirectException;

public class CircularRedirectException
extends RedirectException {
    private static final long serialVersionUID = 6830063487001091803L;

    public CircularRedirectException() {
    }

    public CircularRedirectException(String message) {
        super(message);
    }

    public CircularRedirectException(String message, Throwable cause) {
        super(message, cause);
    }
}

