/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl;

import java.util.Iterator;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.BasicTokenIterator;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class DefaultConnectionReuseStrategy
implements ConnectionReuseStrategy {
    public static final DefaultConnectionReuseStrategy INSTANCE = new DefaultConnectionReuseStrategy();

    @Override
    public boolean keepAlive(HttpRequest request, HttpResponse response, HttpContext context) {
        Iterator<Header> headerIterator;
        Header teh;
        Args.notNull(response, "HTTP response");
        if (request != null) {
            BasicTokenIterator ti = new BasicTokenIterator(request.headerIterator("Connection"));
            while (ti.hasNext()) {
                String token = (String)ti.next();
                if (!"close".equalsIgnoreCase(token)) continue;
                return false;
            }
        }
        if (response.getCode() == 204) {
            Header clh = response.getFirstHeader("Content-Length");
            if (clh != null) {
                try {
                    long contentLen = Long.parseLong(clh.getValue());
                    if (contentLen > 0L) {
                        return false;
                    }
                } catch (NumberFormatException ex) {
                    // empty catch block
                }
            }
            if (response.containsHeader("Transfer-Encoding")) {
                return false;
            }
        }
        if ((teh = response.getFirstHeader("Transfer-Encoding")) != null) {
            if (!"chunked".equalsIgnoreCase(teh.getValue())) {
                return false;
            }
        } else {
            String method;
            String string = method = request != null ? request.getMethod() : null;
            if (MessageSupport.canResponseHaveBody(method, response) && response.countHeaders("Content-Length") != 1) {
                return false;
            }
        }
        if (!(headerIterator = response.headerIterator("Connection")).hasNext()) {
            headerIterator = response.headerIterator("Proxy-Connection");
        }
        ProtocolVersion ver = context.getProtocolVersion();
        if (headerIterator.hasNext()) {
            if (ver.greaterEquals(HttpVersion.HTTP_1_1)) {
                BasicTokenIterator it = new BasicTokenIterator(headerIterator);
                while (it.hasNext()) {
                    String token = (String)it.next();
                    if (!"close".equalsIgnoreCase(token)) continue;
                    return false;
                }
                return true;
            }
            BasicTokenIterator it = new BasicTokenIterator(headerIterator);
            while (it.hasNext()) {
                String token = (String)it.next();
                if (!"keep-alive".equalsIgnoreCase(token)) continue;
                return true;
            }
            return false;
        }
        return ver.greaterEquals(HttpVersion.HTTP_1_1);
    }
}

