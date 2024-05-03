/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

@Contract(threading=ThreadingBehavior.STATELESS)
@Internal
public interface HttpClientConnectionOperator {
    public void connect(ManagedHttpClientConnection var1, HttpHost var2, InetSocketAddress var3, TimeValue var4, SocketConfig var5, HttpContext var6) throws IOException;

    public void upgrade(ManagedHttpClientConnection var1, HttpHost var2, HttpContext var3) throws IOException;
}

