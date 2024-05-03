/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import java.io.IOException;
import java.util.Iterator;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class ResponseConnControl
implements HttpResponseInterceptor {
    @Override
    public void process(HttpResponse response, EntityDetails entity, HttpContext context) throws HttpException, IOException {
        Args.notNull(response, "HTTP response");
        Args.notNull(context, "HTTP context");
        int status = response.getCode();
        if (status == 400 || status == 408 || status == 411 || status == 413 || status == 414 || status == 503 || status == 501) {
            response.setHeader("Connection", "close");
            return;
        }
        if (!response.containsHeader("Connection")) {
            ProtocolVersion ver = context.getProtocolVersion();
            if (entity != null && entity.getContentLength() < 0L && ver.lessEquals(HttpVersion.HTTP_1_0)) {
                response.setHeader("Connection", "close");
            } else {
                HttpCoreContext coreContext = HttpCoreContext.adapt(context);
                HttpRequest request = coreContext.getRequest();
                boolean closeRequested = false;
                boolean keepAliveRequested = false;
                if (request != null) {
                    Iterator<HeaderElement> it = MessageSupport.iterate(request, "Connection");
                    while (it.hasNext()) {
                        HeaderElement he = it.next();
                        if (he.getName().equalsIgnoreCase("close")) {
                            closeRequested = true;
                            break;
                        }
                        if (!he.getName().equalsIgnoreCase("keep-alive")) continue;
                        keepAliveRequested = true;
                    }
                }
                if (closeRequested) {
                    response.addHeader("Connection", "close");
                } else if (response.containsHeader("Upgrade")) {
                    response.addHeader("Connection", "upgrade");
                } else if (keepAliveRequested) {
                    response.addHeader("Connection", "keep-alive");
                } else if (ver.lessEquals(HttpVersion.HTTP_1_0)) {
                    response.addHeader("Connection", "close");
                }
            }
        }
    }
}

