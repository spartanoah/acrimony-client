/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io;

import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.MessageHeaders;
import org.apache.hc.core5.http.io.SessionInputBuffer;

public interface HttpMessageParser<T extends MessageHeaders> {
    public T parse(SessionInputBuffer var1, InputStream var2) throws IOException, HttpException;
}

