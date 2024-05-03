/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.async.methods;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.entity.AbstractBinDataConsumer;
import org.apache.hc.core5.http.protocol.HttpContext;

public abstract class AbstractBinResponseConsumer<T>
extends AbstractBinDataConsumer
implements AsyncResponseConsumer<T> {
    private volatile FutureCallback<T> resultCallback;

    protected abstract void start(HttpResponse var1, ContentType var2) throws HttpException, IOException;

    protected abstract T buildResult();

    @Override
    public void informationResponse(HttpResponse response, HttpContext context) throws HttpException, IOException {
    }

    @Override
    public final void consumeResponse(HttpResponse response, EntityDetails entityDetails, HttpContext context, FutureCallback<T> resultCallback) throws HttpException, IOException {
        this.resultCallback = resultCallback;
        if (entityDetails != null) {
            try {
                ContentType contentType = ContentType.parse(entityDetails.getContentType());
                this.start(response, contentType != null ? contentType : ContentType.DEFAULT_BINARY);
            } catch (UnsupportedCharsetException ex) {
                throw new UnsupportedEncodingException(ex.getMessage());
            }
        } else {
            this.start(response, null);
            this.completed();
        }
    }

    @Override
    protected final void completed() {
        this.resultCallback.completed(this.buildResult());
    }

    @Override
    public void failed(Exception cause) {
    }
}

