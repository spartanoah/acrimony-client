/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.IOException;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

@Contract(threading=ThreadingBehavior.STATELESS)
public abstract class AbstractHttpClientResponseHandler<T>
implements HttpClientResponseHandler<T> {
    @Override
    public T handleResponse(ClassicHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if (response.getCode() >= 300) {
            EntityUtils.consume(entity);
            throw new HttpResponseException(response.getCode(), response.getReasonPhrase());
        }
        return entity == null ? null : (T)this.handleEntity(entity);
    }

    public abstract T handleEntity(HttpEntity var1) throws IOException;
}

