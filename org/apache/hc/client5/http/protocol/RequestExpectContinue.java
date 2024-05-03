/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.protocol;

import java.io.IOException;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.STATELESS)
public class RequestExpectContinue
implements HttpRequestInterceptor {
    @Override
    public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        if (!request.containsHeader("Expect")) {
            HttpClientContext clientContext;
            RequestConfig config;
            ProtocolVersion version;
            ProtocolVersion protocolVersion = version = request.getVersion() != null ? request.getVersion() : HttpVersion.HTTP_1_1;
            if (entity != null && entity.getContentLength() != 0L && !version.lessEquals(HttpVersion.HTTP_1_0) && (config = (clientContext = HttpClientContext.adapt(context)).getRequestConfig()).isExpectContinueEnabled()) {
                request.addHeader("Expect", "100-continue");
            }
        }
    }
}

