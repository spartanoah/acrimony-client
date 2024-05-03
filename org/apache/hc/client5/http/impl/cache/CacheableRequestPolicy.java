/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.util.Iterator;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.MessageSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CacheableRequestPolicy {
    private static final Logger LOG = LoggerFactory.getLogger(CacheableRequestPolicy.class);

    CacheableRequestPolicy() {
    }

    public boolean isServableFromCache(HttpRequest request) {
        ProtocolVersion pv;
        String method = request.getMethod();
        ProtocolVersion protocolVersion = pv = request.getVersion() != null ? request.getVersion() : HttpVersion.DEFAULT;
        if (HttpVersion.HTTP_1_1.compareToVersion(pv) != 0) {
            LOG.debug("non-HTTP/1.1 request is not serveable from cache");
            return false;
        }
        if (!method.equals("GET") && !method.equals("HEAD")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} request is not serveable from cache", (Object)method);
            }
            return false;
        }
        if (request.countHeaders("Pragma") > 0) {
            LOG.debug("request with Pragma header is not serveable from cache");
            return false;
        }
        Iterator<HeaderElement> it = MessageSupport.iterate(request, "Cache-Control");
        while (it.hasNext()) {
            HeaderElement cacheControlElement = it.next();
            if ("no-store".equalsIgnoreCase(cacheControlElement.getName())) {
                LOG.debug("Request with no-store is not serveable from cache");
                return false;
            }
            if (!"no-cache".equalsIgnoreCase(cacheControlElement.getName())) continue;
            LOG.debug("Request with no-cache is not serveable from cache");
            return false;
        }
        LOG.debug("Request is serveable from cache");
        return true;
    }
}

