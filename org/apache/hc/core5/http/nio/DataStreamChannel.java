/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.nio.StreamChannel;

@Contract(threading=ThreadingBehavior.SAFE)
public interface DataStreamChannel
extends StreamChannel<ByteBuffer> {
    public void requestOutput();

    @Override
    public int write(ByteBuffer var1) throws IOException;

    public void endStream(List<? extends Header> var1) throws IOException;
}

