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
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.URIAuthority;

@Contract(threading=ThreadingBehavior.STATELESS)
public abstract class AbstractHttpServerAuthFilter<T>
implements HttpFilterHandler {
    private final boolean respondImmediately;

    protected AbstractHttpServerAuthFilter(boolean respondImmediately) {
        this.respondImmediately = respondImmediately;
    }

    protected abstract T parseChallengeResponse(String var1, HttpContext var2) throws HttpException;

    protected abstract boolean authenticate(T var1, URIAuthority var2, String var3, HttpContext var4);

    protected abstract String generateChallenge(T var1, URIAuthority var2, String var3, HttpContext var4);

    protected HttpEntity generateResponseContent(HttpResponse unauthorized) {
        return new StringEntity("Unauthorized");
    }

    @Override
    public final void handle(ClassicHttpRequest request, HttpFilterChain.ResponseTrigger responseTrigger, HttpContext context, HttpFilterChain chain) throws HttpException, IOException {
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
            chain.proceed(request, responseTrigger, context);
        } else {
            BasicClassicHttpResponse unauthorized = new BasicClassicHttpResponse(401);
            unauthorized.addHeader("WWW-Authenticate", this.generateChallenge(challengeResponse, authority, requestUri, context));
            HttpEntity responseContent = this.generateResponseContent(unauthorized);
            unauthorized.setEntity(responseContent);
            if (this.respondImmediately || expectContinue || request.getEntity() == null) {
                responseTrigger.submitResponse(unauthorized);
                EntityUtils.consume(request.getEntity());
            } else {
                EntityUtils.consume(request.getEntity());
                responseTrigger.submitResponse(unauthorized);
            }
        }
    }
}

