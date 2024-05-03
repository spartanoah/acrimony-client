/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.entity;

import org.apache.http.HttpException;
import org.apache.http.HttpMessage;

public interface ContentLengthStrategy {
    public static final int IDENTITY = -1;
    public static final int CHUNKED = -2;

    public long determineLength(HttpMessage var1) throws HttpException;
}

