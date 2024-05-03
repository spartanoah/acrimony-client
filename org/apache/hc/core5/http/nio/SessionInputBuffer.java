/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import org.apache.hc.core5.util.CharArrayBuffer;

public interface SessionInputBuffer {
    public boolean hasData();

    public int length();

    public int fill(ReadableByteChannel var1) throws IOException;

    public int read();

    public int read(ByteBuffer var1, int var2);

    public int read(ByteBuffer var1);

    public int read(WritableByteChannel var1, int var2) throws IOException;

    public int read(WritableByteChannel var1) throws IOException;

    public boolean readLine(CharArrayBuffer var1, boolean var2) throws IOException;
}

