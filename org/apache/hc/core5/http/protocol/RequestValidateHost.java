/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class RequestValidateHost
implements HttpRequestInterceptor {
    @Override
    public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        Header header = request.getHeader("Host");
        if (header != null) {
            URIAuthority authority;
            try {
                authority = URIAuthority.create(header.getValue());
            } catch (URISyntaxException ex) {
                throw new ProtocolException(ex.getMessage(), ex);
            }
            request.setAuthority(authority);
        } else {
            ProtocolVersion version;
            ProtocolVersion protocolVersion = version = request.getVersion() != null ? request.getVersion() : HttpVersion.HTTP_1_1;
            if (version.greaterEquals(HttpVersion.HTTP_1_1)) {
                throw new ProtocolException("Host header is absent");
            }
        }
    }
}

