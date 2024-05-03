/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.hc.core5.http.io.HttpTransportMetrics;
import org.apache.hc.core5.util.CharArrayBuffer;

public interface SessionOutputBuffer {
    public int length();

    public int capacity();

    public int available();

    public void write(byte[] var1, int var2, int var3, OutputStream var4) throws IOException;

    public void write(byte[] var1, OutputStream var2) throws IOException;

    public void write(int var1, OutputStream var2) throws IOException;

    public void writeLine(CharArrayBuffer var1, OutputStream var2) throws IOException;

    public void flush(OutputStream var1) throws IOException;

    public HttpTransportMetrics getMetrics();
}

