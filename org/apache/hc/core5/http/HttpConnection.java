/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.io.IOException;
import java.net.SocketAddress;
import javax.net.ssl.SSLSession;
import org.apache.hc.core5.http.EndpointDetails;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.SocketModalCloseable;

public interface HttpConnection
extends SocketModalCloseable {
    @Override
    public void close() throws IOException;

    public EndpointDetails getEndpointDetails();

    public SocketAddress getLocalAddress();

    public SocketAddress getRemoteAddress();

    public ProtocolVersion getProtocolVersion();

    public SSLSession getSSLSession();

    public boolean isOpen();
}

