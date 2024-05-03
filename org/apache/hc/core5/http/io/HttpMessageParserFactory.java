/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io;

import org.apache.hc.core5.http.MessageHeaders;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.io.HttpMessageParser;

public interface HttpMessageParserFactory<T extends MessageHeaders> {
    public HttpMessageParser<T> create(Http1Config var1);
}

