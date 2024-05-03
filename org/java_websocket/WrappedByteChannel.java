/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public interface WrappedByteChannel
extends ByteChannel {
    public boolean isNeedWrite();

    public void writeMore() throws IOException;

    public boolean isNeedRead();

    public int readMore(ByteBuffer var1) throws IOException;

    public boolean isBlocking();
}

