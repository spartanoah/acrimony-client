/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.CharacterCodingException;
import org.apache.hc.core5.util.CharArrayBuffer;

public interface SessionOutputBuffer {
    public boolean hasData();

    public int capacity();

    public int length();

    public int flush(WritableByteChannel var1) throws IOException;

    public void write(ByteBuffer var1);

    public void write(ReadableByteChannel var1) throws IOException;

    public void writeLine(CharArrayBuffer var1) throws CharacterCodingException;
}

