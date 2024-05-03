/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.io.IOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncFilterChain;
import org.apache.hc.core5.http.nio.AsyncFilterHandler;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.STATELESS)
public class AsyncServerExpectationFilter
implements AsyncFilterHandler {
    protected boolean verify(HttpRequest request, HttpContext context) throws HttpException {
        return true;
    }

    protected AsyncEntityProducer generateResponseContent(HttpResponse expectationFailed) throws HttpException {
        return null;
    }

    @Override
    public final AsyncDataConsumer handle(HttpRequest request, EntityDetails entityDetails, HttpContext context, AsyncFilterChain.ResponseTrigger responseTrigger, AsyncFilterChain chain) throws HttpException, IOException {
        Header h;
        if (entityDetails != null && (h = request.getFirstHeader("Expect")) != null && "100-continue".equalsIgnoreCase(h.getValue())) {
            boolean verified = this.verify(request, context);
            if (verified) {
                responseTrigger.sendInformation(new BasicHttpResponse(100));
            } else {
                BasicHttpResponse expectationFailed = new BasicHttpResponse(417);
                AsyncEntityProducer responseContentProducer = this.generateResponseContent(expectationFailed);
                responseTrigger.submitResponse(expectationFailed, responseContentProducer);
                return null;
            }
        }
        return chain.proceed(request, entityDetails, context, responseTrigger);
    }
}

