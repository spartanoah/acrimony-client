/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cookie;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.hc.client5.http.cookie.CommonCookieAttributeHandler;
import org.apache.hc.client5.http.cookie.CookieAttributeHandler;
import org.apache.hc.client5.http.cookie.CookieSpec;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Asserts;

@Contract(threading=ThreadingBehavior.SAFE)
public abstract class AbstractCookieSpec
implements CookieSpec {
    private final Map<String, CookieAttributeHandler> attribHandlerMap;

    public AbstractCookieSpec() {
        this.attribHandlerMap = new ConcurrentHashMap<String, CookieAttributeHandler>(10);
    }

    protected AbstractCookieSpec(HashMap<String, CookieAttributeHandler> map) {
        Asserts.notNull(map, "Attribute handler map");
        this.attribHandlerMap = new ConcurrentHashMap<String, CookieAttributeHandler>(map);
    }

    protected AbstractCookieSpec(CommonCookieAttributeHandler ... handlers) {
        this.attribHandlerMap = new ConcurrentHashMap<String, CookieAttributeHandler>(handlers.length);
        for (CommonCookieAttributeHandler handler : handlers) {
            this.attribHandlerMap.put(handler.getAttributeName(), handler);
        }
    }

    protected CookieAttributeHandler findAttribHandler(String name) {
        return this.attribHandlerMap.get(name);
    }

    protected CookieAttributeHandler getAttribHandler(String name) {
        CookieAttributeHandler handler = this.findAttribHandler(name);
        Asserts.check(handler != null, "Handler not registered for " + name + " attribute");
        return handler;
    }

    protected Collection<CookieAttributeHandler> getAttribHandlers() {
        return this.attribHandlerMap.values();
    }
}

