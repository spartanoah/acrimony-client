/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.hc.core5.http.Header;

public interface ContentEncoder {
    public int write(ByteBuffer var1) throws IOException;

    public void complete(List<? extends Header> var1) throws IOException;

    public boolean isCompleted();
}

