/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.MessageHeaders;
import org.apache.hc.core5.http.io.SessionOutputBuffer;

public interface HttpMessageWriter<T extends MessageHeaders> {
    public void write(T var1, SessionOutputBuffer var2, OutputStream var3) throws IOException, HttpException;
}

