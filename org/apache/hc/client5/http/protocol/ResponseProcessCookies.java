/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.protocol;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieOrigin;
import org.apache.hc.client5.http.cookie.CookieSpec;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.cookie.MalformedCookieException;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
public class ResponseProcessCookies
implements HttpResponseInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(ResponseProcessCookies.class);

    @Override
    public void process(HttpResponse response, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        Args.notNull(response, "HTTP request");
        Args.notNull(context, "HTTP context");
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        CookieSpec cookieSpec = clientContext.getCookieSpec();
        if (cookieSpec == null) {
            LOG.debug("Cookie spec not specified in HTTP context");
            return;
        }
        CookieStore cookieStore = clientContext.getCookieStore();
        if (cookieStore == null) {
            LOG.debug("Cookie store not specified in HTTP context");
            return;
        }
        CookieOrigin cookieOrigin = clientContext.getCookieOrigin();
        if (cookieOrigin == null) {
            LOG.debug("Cookie origin not specified in HTTP context");
            return;
        }
        Iterator<Header> it = response.headerIterator("Set-Cookie");
        this.processCookies(it, cookieSpec, cookieOrigin, cookieStore);
    }

    private void processCookies(Iterator<Header> iterator, CookieSpec cookieSpec, CookieOrigin cookieOrigin, CookieStore cookieStore) {
        while (iterator.hasNext()) {
            Header header = iterator.next();
            try {
                List<Cookie> cookies = cookieSpec.parse(header, cookieOrigin);
                for (Cookie cookie : cookies) {
                    try {
                        cookieSpec.validate(cookie, cookieOrigin);
                        cookieStore.addCookie(cookie);
                        if (!LOG.isDebugEnabled()) continue;
                        LOG.debug("Cookie accepted [{}]", (Object)ResponseProcessCookies.formatCooke(cookie));
                    } catch (MalformedCookieException ex) {
                        if (!LOG.isWarnEnabled()) continue;
                        LOG.warn("Cookie rejected [{}] {}", (Object)ResponseProcessCookies.formatCooke(cookie), (Object)ex.getMessage());
                    }
                }
            } catch (MalformedCookieException ex) {
                if (!LOG.isWarnEnabled()) continue;
                LOG.warn("Invalid cookie header: \"{}\". {}", (Object)header, (Object)ex.getMessage());
            }
        }
    }

    private static String formatCooke(Cookie cookie) {
        StringBuilder buf = new StringBuilder();
        buf.append(cookie.getName());
        buf.append("=\"");
        String v = cookie.getValue();
        if (v != null) {
            if (v.length() > 100) {
                v = v.substring(0, 100) + "...";
            }
            buf.append(v);
        }
        buf.append("\"");
        buf.append(", domain:");
        buf.append(cookie.getDomain());
        buf.append(", path:");
        buf.append(cookie.getPath());
        buf.append(", expiry:");
        buf.append(cookie.getExpiryDate());
        return buf.toString();
    }
}

