/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.IOException;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

@Contract(threading=ThreadingBehavior.STATELESS)
public class BasicHttpClientResponseHandler
extends AbstractHttpClientResponseHandler<String> {
    @Override
    public String handleEntity(HttpEntity entity) throws IOException {
        try {
            return EntityUtils.toString(entity);
        } catch (ParseException ex) {
            throw new ClientProtocolException(ex);
        }
    }

    @Override
    public String handleResponse(ClassicHttpResponse response) throws IOException {
        return (String)super.handleResponse(response);
    }
}

