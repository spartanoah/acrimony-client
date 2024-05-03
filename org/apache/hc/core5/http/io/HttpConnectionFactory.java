/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io;

import java.io.IOException;
import java.net.Socket;
import org.apache.hc.core5.http.HttpConnection;

public interface HttpConnectionFactory<T extends HttpConnection> {
    public T createConnection(Socket var1) throws IOException;
}

