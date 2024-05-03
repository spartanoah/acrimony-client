/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.io.IOException;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.MessageHeaders;
import org.apache.hc.core5.http.nio.SessionOutputBuffer;

public interface NHttpMessageWriter<T extends MessageHeaders> {
    public void reset();

    public void write(T var1, SessionOutputBuffer var2) throws IOException, HttpException;
}

