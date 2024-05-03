/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import java.io.IOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class ResponseContent
implements HttpResponseInterceptor {
    private final boolean overwrite;

    public ResponseContent() {
        this(false);
    }

    public ResponseContent(boolean overwrite) {
        this.overwrite = overwrite;
    }

    @Override
    public void process(HttpResponse response, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        Args.notNull(response, "HTTP response");
        if (this.overwrite) {
            response.removeHeaders("Transfer-Encoding");
            response.removeHeaders("Content-Length");
        } else {
            if (response.containsHeader("Transfer-Encoding")) {
                throw new ProtocolException("Transfer-encoding header already present");
            }
            if (response.containsHeader("Content-Length")) {
                throw new ProtocolException("Content-Length header already present");
            }
        }
        ProtocolVersion ver = context.getProtocolVersion();
        if (entity != null) {
            long len = entity.getContentLength();
            if (len >= 0L && !entity.isChunked()) {
                response.addHeader("Content-Length", Long.toString(entity.getContentLength()));
            } else if (ver.greaterEquals(HttpVersion.HTTP_1_1)) {
                response.addHeader("Transfer-Encoding", "chunked");
                MessageSupport.addTrailerHeader(response, entity);
            }
            MessageSupport.addContentTypeHeader(response, entity);
            MessageSupport.addContentEncodingHeader(response, entity);
        } else {
            int status = response.getCode();
            if (status != 204 && status != 304) {
                response.addHeader("Content-Length", "0");
            }
        }
    }
}

