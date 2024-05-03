/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.MessageHeaders;
import org.apache.hc.core5.http.ProtocolVersion;

public interface HttpMessage
extends MessageHeaders {
    public void setVersion(ProtocolVersion var1);

    public ProtocolVersion getVersion();

    public void addHeader(Header var1);

    public void addHeader(String var1, Object var2);

    public void setHeader(Header var1);

    public void setHeader(String var1, Object var2);

    public void setHeaders(Header ... var1);

    public boolean removeHeader(Header var1);

    public boolean removeHeaders(String var1);
}

