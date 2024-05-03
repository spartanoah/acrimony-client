/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.async.methods;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.entity.AbstractBinDataConsumer;
import org.apache.hc.core5.http.protocol.HttpContext;

public abstract class AbstractBinPushConsumer
extends AbstractBinDataConsumer
implements AsyncPushConsumer {
    protected abstract void start(HttpRequest var1, HttpResponse var2, ContentType var3) throws HttpException, IOException;

    @Override
    public final void consumePromise(HttpRequest promise, HttpResponse response, EntityDetails entityDetails, HttpContext context) throws HttpException, IOException {
        if (entityDetails != null) {
            ContentType contentType;
            try {
                contentType = ContentType.parse(entityDetails.getContentType());
            } catch (UnsupportedCharsetException ex) {
                throw new UnsupportedEncodingException(ex.getMessage());
            }
            this.start(promise, response, contentType != null ? contentType : ContentType.DEFAULT_BINARY);
        } else {
            this.start(promise, response, null);
            this.completed();
        }
    }

    @Override
    public void failed(Exception cause) {
    }
}

