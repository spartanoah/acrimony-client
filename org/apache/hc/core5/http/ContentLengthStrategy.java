/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpMessage;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface ContentLengthStrategy {
    public static final long CHUNKED = -1L;
    public static final long UNDEFINED = -9223372036854775807L;

    public long determineLength(HttpMessage var1) throws HttpException;
}

