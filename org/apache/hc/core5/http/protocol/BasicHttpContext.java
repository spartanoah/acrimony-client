/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.SAFE_CONDITIONAL)
public class BasicHttpContext
implements HttpContext {
    private final HttpContext parentContext;
    private final Map<String, Object> map = new ConcurrentHashMap<String, Object>();
    private ProtocolVersion version;

    public BasicHttpContext() {
        this(null);
    }

    public BasicHttpContext(HttpContext parentContext) {
        this.parentContext = parentContext;
    }

    @Override
    public Object getAttribute(String id) {
        Args.notNull(id, "Id");
        Object obj = this.map.get(id);
        if (obj == null && this.parentContext != null) {
            obj = this.parentContext.getAttribute(id);
        }
        return obj;
    }

    @Override
    public Object setAttribute(String id, Object obj) {
        Args.notNull(id, "Id");
        if (obj != null) {
            return this.map.put(id, obj);
        }
        return this.map.remove(id);
    }

    @Override
    public Object removeAttribute(String id) {
        Args.notNull(id, "Id");
        return this.map.remove(id);
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return this.version != null ? this.version : HttpVersion.DEFAULT;
    }

    @Override
    public void setProtocolVersion(ProtocolVersion version) {
        this.version = version;
    }

    public void clear() {
        this.map.clear();
    }

    public String toString() {
        return this.map.toString();
    }
}

