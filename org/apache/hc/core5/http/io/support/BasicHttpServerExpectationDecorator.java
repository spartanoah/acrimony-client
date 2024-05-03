/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.support;

import java.io.IOException;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpServerRequestHandler;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

public class BasicHttpServerExpectationDecorator
implements HttpServerRequestHandler {
    private final HttpServerRequestHandler requestHandler;

    public BasicHttpServerExpectationDecorator(HttpServerRequestHandler requestHandler) {
        this.requestHandler = Args.notNull(requestHandler, "Request handler");
    }

    protected ClassicHttpResponse verify(ClassicHttpRequest request, HttpContext context) {
        return null;
    }

    @Override
    public final void handle(ClassicHttpRequest request, HttpServerRequestHandler.ResponseTrigger responseTrigger, HttpContext context) throws HttpException, IOException {
        Header expect = request.getFirstHeader("Expect");
        if (expect != null && "100-continue".equalsIgnoreCase(expect.getValue())) {
            ClassicHttpResponse response = this.verify(request, context);
            if (response == null) {
                responseTrigger.sendInformation(new BasicClassicHttpResponse(100));
            } else {
                responseTrigger.submitResponse(response);
                return;
            }
        }
        this.requestHandler.handle(request, responseTrigger, context);
    }
}

