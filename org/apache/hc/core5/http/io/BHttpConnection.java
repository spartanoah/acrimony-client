/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io;

import java.io.IOException;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.util.Timeout;

public interface BHttpConnection
extends HttpConnection {
    public boolean isDataAvailable(Timeout var1) throws IOException;

    public boolean isStale() throws IOException;

    public void flush() throws IOException;
}

