/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import org.apache.hc.core5.http.ProtocolVersion;

public interface HttpContext {
    public static final String RESERVED_PREFIX = "http.";

    public ProtocolVersion getProtocolVersion();

    public void setProtocolVersion(ProtocolVersion var1);

    public Object getAttribute(String var1);

    public Object setAttribute(String var1, Object var2);

    public Object removeAttribute(String var1);
}

