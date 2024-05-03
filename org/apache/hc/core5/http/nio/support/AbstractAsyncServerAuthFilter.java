/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.support;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncFilterChain;
import org.apache.hc.core5.http.nio.AsyncFilterHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.entity.AsyncEntityProducers;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIAuthority;

@Contract(threading=ThreadingBehavior.STATELESS)
public abstract class AbstractAsyncServerAuthFilter<T>
implements AsyncFilterHandler {
    private final boolean respondImmediately;

    protected AbstractAsyncServerAuthFilter(boolean respondImmediately) {
        this.respondImmediately = respondImmediately;
    }

    protected abstract T parseChallengeResponse(String var1, HttpContext var2) throws HttpException;

    protected abstract boolean authenticate(T var1, URIAuthority var2, String var3, HttpContext var4);

    protected abstract String generateChallenge(T var1, URIAuthority var2, String var3, HttpContext var4);

    protected AsyncEntityProducer generateResponseContent(HttpResponse unauthorized) {
        return AsyncEntityProducers.create("Unauthorized");
    }

    @Override
    public final AsyncDataConsumer handle(HttpRequest request, EntityDetails entityDetails, HttpContext context, final AsyncFilterChain.ResponseTrigger responseTrigger, AsyncFilterChain chain) throws HttpException, IOException {
        boolean expectContinue;
        Header h = request.getFirstHeader("Authorization");
        T challengeResponse = h != null ? (T)this.parseChallengeResponse(h.getValue(), context) : null;
        URIAuthority authority = request.getAuthority();
        String requestUri = request.getRequestUri();
        boolean authenticated = this.authenticate(challengeResponse, authority, requestUri, context);
        Header expect = request.getFirstHeader("Expect");
        boolean bl = expectContinue = expect != null && "100-continue".equalsIgnoreCase(expect.getValue());
        if (authenticated) {
            if (expectContinue) {
                responseTrigger.sendInformation(new BasicClassicHttpResponse(100));
            }
            return chain.proceed(request, entityDetails, context, responseTrigger);
        }
        final BasicHttpResponse unauthorized = new BasicHttpResponse(401);
        unauthorized.addHeader("WWW-Authenticate", this.generateChallenge(challengeResponse, authority, requestUri, context));
        final AsyncEntityProducer responseContentProducer = this.generateResponseContent(unauthorized);
        if (this.respondImmediately || expectContinue || entityDetails == null) {
            responseTrigger.submitResponse(unauthorized, responseContentProducer);
            return null;
        }
        return new AsyncDataConsumer(){

            @Override
            public void updateCapacity(CapacityChannel capacityChannel) throws IOException {
                capacityChannel.update(Integer.MAX_VALUE);
            }

            @Override
            public void consume(ByteBuffer src) throws IOException {
            }

            @Override
            public void streamEnd(List<? extends Header> trailers) throws HttpException, IOException {
                responseTrigger.submitResponse(unauthorized, responseContentProducer);
            }

            @Override
            public void releaseResources() {
                if (responseContentProducer != null) {
                    responseContentProducer.releaseResources();
                }
            }
        };
    }
}

