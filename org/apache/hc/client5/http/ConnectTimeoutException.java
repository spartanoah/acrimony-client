/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http;

import java.net.SocketTimeoutException;
import org.apache.hc.core5.net.NamedEndpoint;

public class ConnectTimeoutException
extends SocketTimeoutException {
    private static final long serialVersionUID = -4816682903149535989L;
    private final NamedEndpoint namedEndpoint;

    public ConnectTimeoutException(String message) {
        super(message);
        this.namedEndpoint = null;
    }

    public ConnectTimeoutException(String message, NamedEndpoint namedEndpoint) {
        super(message);
        this.namedEndpoint = namedEndpoint;
    }

    public NamedEndpoint getHost() {
        return this.namedEndpoint;
    }
}

