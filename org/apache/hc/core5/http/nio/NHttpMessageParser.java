/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio;

import java.io.IOException;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.MessageHeaders;
import org.apache.hc.core5.http.nio.SessionInputBuffer;

public interface NHttpMessageParser<T extends MessageHeaders> {
    public void reset();

    public T parse(SessionInputBuffer var1, boolean var2) throws IOException, HttpException;
}

