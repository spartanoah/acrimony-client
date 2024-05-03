/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

import org.apache.hc.core5.http.HttpException;

public class TunnelRefusedException
extends HttpException {
    private static final long serialVersionUID = -8646722842745617323L;
    private final String responseMessage;

    public TunnelRefusedException(String message, String responseMessage) {
        super(message);
        this.responseMessage = responseMessage;
    }

    public String getResponseMessage() {
        return this.responseMessage;
    }
}

