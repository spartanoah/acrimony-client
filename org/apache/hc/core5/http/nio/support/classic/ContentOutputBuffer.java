/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support.classic;

import java.io.IOException;

public interface ContentOutputBuffer {
    public int length();

    public void reset();

    public void write(byte[] var1, int var2, int var3) throws IOException;

    public void write(int var1) throws IOException;

    public void writeCompleted() throws IOException;
}

