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
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class RequestContent
implements HttpRequestInterceptor {
    private final boolean overwrite;

    public RequestContent() {
        this(false);
    }

    public RequestContent(boolean overwrite) {
        this.overwrite = overwrite;
    }

    @Override
    public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        Args.notNull(request, "HTTP request");
        String method = request.getMethod();
        if (Method.TRACE.isSame(method) && entity != null) {
            throw new ProtocolException("TRACE request may not enclose an entity");
        }
        if (this.overwrite) {
            request.removeHeaders("Transfer-Encoding");
            request.removeHeaders("Content-Length");
        } else {
            if (request.containsHeader("Transfer-Encoding")) {
                throw new ProtocolException("Transfer-encoding header already present");
            }
            if (request.containsHeader("Content-Length")) {
                throw new ProtocolException("Content-Length header already present");
            }
        }
        if (entity != null) {
            ProtocolVersion ver = context.getProtocolVersion();
            if (entity.isChunked() || entity.getContentLength() < 0L) {
                if (ver.lessEquals(HttpVersion.HTTP_1_0)) {
                    throw new ProtocolException("Chunked transfer encoding not allowed for " + ver);
                }
                request.addHeader("Transfer-Encoding", "chunked");
                MessageSupport.addTrailerHeader(request, entity);
            } else {
                request.addHeader("Content-Length", Long.toString(entity.getContentLength()));
            }
            MessageSupport.addContentTypeHeader(request, entity);
            MessageSupport.addContentEncodingHeader(request, entity);
        }
    }
}

