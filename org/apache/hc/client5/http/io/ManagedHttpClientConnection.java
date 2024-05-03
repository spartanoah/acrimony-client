/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.io;

import java.io.IOException;
import java.net.Socket;
import javax.net.ssl.SSLSession;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.http.io.HttpClientConnection;

@Internal
public interface ManagedHttpClientConnection
extends HttpClientConnection {
    public void bind(Socket var1) throws IOException;

    public Socket getSocket();

    @Override
    public SSLSession getSSLSession();

    public void passivate();

    public void activate();
}

