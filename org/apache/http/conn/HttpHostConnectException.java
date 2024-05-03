/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.conn;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.util.Arrays;
import org.apache.http.HttpHost;
import org.apache.http.annotation.Immutable;

@Immutable
public class HttpHostConnectException
extends ConnectException {
    private static final long serialVersionUID = -3194482710275220224L;
    private final HttpHost host;

    @Deprecated
    public HttpHostConnectException(HttpHost host, ConnectException cause) {
        this(cause, host, null);
    }

    public HttpHostConnectException(IOException cause, HttpHost host, InetAddress ... remoteAddresses) {
        super("Connect to " + (host != null ? host.toHostString() : "remote host") + (remoteAddresses != null && remoteAddresses.length > 0 ? " " + Arrays.asList(remoteAddresses) : "") + (cause != null && cause.getMessage() != null ? " failed: " + cause.getMessage() : " refused"));
        this.host = host;
        this.initCause(cause);
    }

    public HttpHost getHost() {
        return this.host;
    }
}

