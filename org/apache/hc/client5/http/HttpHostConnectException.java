/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http;

import java.net.ConnectException;
import org.apache.hc.core5.net.NamedEndpoint;

public class HttpHostConnectException
extends ConnectException {
    private static final long serialVersionUID = -3194482710275220224L;
    private final NamedEndpoint namedEndpoint;

    public HttpHostConnectException(String message) {
        super(message);
        this.namedEndpoint = null;
    }

    public HttpHostConnectException(String message, NamedEndpoint namedEndpoint) {
        super(message);
        this.namedEndpoint = namedEndpoint;
    }

    public NamedEndpoint getHost() {
        return this.namedEndpoint;
    }
}

