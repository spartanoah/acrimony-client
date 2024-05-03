/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.core5.http.io.HttpTransportMetrics;
import org.apache.hc.core5.util.CharArrayBuffer;

public interface SessionInputBuffer {
    public int length();

    public int capacity();

    public int available();

    public int read(byte[] var1, int var2, int var3, InputStream var4) throws IOException;

    public int read(byte[] var1, InputStream var2) throws IOException;

    public int read(InputStream var1) throws IOException;

    public int readLine(CharArrayBuffer var1, InputStream var2) throws IOException;

    public HttpTransportMetrics getMetrics();
}

