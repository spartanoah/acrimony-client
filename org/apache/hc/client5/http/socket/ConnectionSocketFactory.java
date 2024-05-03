/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface ConnectionSocketFactory {
    public Socket createSocket(HttpContext var1) throws IOException;

    public Socket connectSocket(TimeValue var1, Socket var2, HttpHost var3, InetSocketAddress var4, InetSocketAddress var5, HttpContext var6) throws IOException;
}

