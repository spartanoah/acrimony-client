/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.entity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.nio.AsyncEntityConsumer;
import org.apache.hc.core5.http.nio.entity.AbstractBinDataConsumer;
import org.apache.hc.core5.util.Args;

public abstract class AbstractBinAsyncEntityConsumer<T>
extends AbstractBinDataConsumer
implements AsyncEntityConsumer<T> {
    private volatile FutureCallback<T> resultCallback;
    private volatile T content;

    protected abstract void streamStart(ContentType var1) throws HttpException, IOException;

    protected abstract T generateContent() throws IOException;

    @Override
    public final void streamStart(EntityDetails entityDetails, FutureCallback<T> resultCallback) throws IOException, HttpException {
        Args.notNull(resultCallback, "Result callback");
        this.resultCallback = resultCallback;
        try {
            ContentType contentType = entityDetails != null ? ContentType.parse(entityDetails.getContentType()) : null;
            this.streamStart(contentType);
        } catch (UnsupportedCharsetException ex) {
            throw new UnsupportedEncodingException(ex.getMessage());
        }
    }

    @Override
    protected final void completed() throws IOException {
        this.content = this.generateContent();
        if (this.resultCallback != null) {
            this.resultCallback.completed(this.content);
        }
        this.releaseResources();
    }

    @Override
    public final void failed(Exception cause) {
        if (this.resultCallback != null) {
            this.resultCallback.failed(cause);
        }
        this.releaseResources();
    }

    @Override
    public final T getContent() {
        return this.content;
    }
}

