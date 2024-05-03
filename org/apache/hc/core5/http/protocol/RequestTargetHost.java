/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import java.io.IOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class RequestTargetHost
implements HttpRequestInterceptor {
    @Override
    public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        Args.notNull(context, "HTTP context");
        ProtocolVersion ver = context.getProtocolVersion();
        String method = request.getMethod();
        if (Method.CONNECT.isSame(method) && ver.lessEquals(HttpVersion.HTTP_1_0)) {
            return;
        }
        if (!request.containsHeader("Host")) {
            URIAuthority authority = request.getAuthority();
            if (authority == null) {
                if (ver.lessEquals(HttpVersion.HTTP_1_0)) {
                    return;
                }
                throw new ProtocolException("Target host is unknown");
            }
            if (authority.getUserInfo() != null) {
                authority = new URIAuthority(authority.getHostName(), authority.getPort());
            }
            request.addHeader("Host", authority);
        }
    }
}

