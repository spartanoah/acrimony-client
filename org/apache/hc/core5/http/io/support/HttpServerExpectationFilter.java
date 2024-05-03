/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.support;

import java.io.IOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.HttpFilterChain;
import org.apache.hc.core5.http.io.HttpFilterHandler;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.STATELESS)
public class HttpServerExpectationFilter
implements HttpFilterHandler {
    protected boolean verify(ClassicHttpRequest request, HttpContext context) throws HttpException {
        return true;
    }

    protected HttpEntity generateResponseContent(HttpResponse expectationFailed) throws HttpException {
        return null;
    }

    @Override
    public final void handle(ClassicHttpRequest request, HttpFilterChain.ResponseTrigger responseTrigger, HttpContext context, HttpFilterChain chain) throws HttpException, IOException {
        boolean expectContinue;
        Header expect = request.getFirstHeader("Expect");
        boolean bl = expectContinue = expect != null && "100-continue".equalsIgnoreCase(expect.getValue());
        if (expectContinue) {
            boolean verified = this.verify(request, context);
            if (verified) {
                responseTrigger.sendInformation(new BasicClassicHttpResponse(100));
            } else {
                BasicClassicHttpResponse expectationFailed = new BasicClassicHttpResponse(417);
                HttpEntity responseContent = this.generateResponseContent(expectationFailed);
                expectationFailed.setEntity(responseContent);
                responseTrigger.submitResponse(expectationFailed);
                return;
            }
        }
        chain.proceed(request, responseTrigger, context);
    }
}

