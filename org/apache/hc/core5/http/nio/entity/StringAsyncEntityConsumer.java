/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.entity;

import java.io.IOException;
import java.nio.CharBuffer;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.nio.entity.AbstractCharAsyncEntityConsumer;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

public class StringAsyncEntityConsumer
extends AbstractCharAsyncEntityConsumer<String> {
    private final int capacityIncrement;
    private final CharArrayBuffer content;

    public StringAsyncEntityConsumer(int bufSize, int capacityIncrement, CharCodingConfig charCodingConfig) {
        super(bufSize, charCodingConfig);
        this.capacityIncrement = Args.positive(capacityIncrement, "Capacity increment");
        this.content = new CharArrayBuffer(1024);
    }

    public StringAsyncEntityConsumer(int capacityIncrement) {
        this(8192, capacityIncrement, CharCodingConfig.DEFAULT);
    }

    public StringAsyncEntityConsumer(CharCodingConfig charCodingConfig) {
        this(8192, Integer.MAX_VALUE, charCodingConfig);
    }

    public StringAsyncEntityConsumer() {
        this(Integer.MAX_VALUE);
    }

    @Override
    protected final void streamStart(ContentType contentType) throws HttpException, IOException {
    }

    @Override
    protected int capacityIncrement() {
        return this.capacityIncrement;
    }

    @Override
    protected final void data(CharBuffer src, boolean endOfStream) {
        Args.notNull(src, "CharBuffer");
        int chunk = src.remaining();
        this.content.ensureCapacity(chunk);
        src.get(this.content.array(), this.content.length(), chunk);
        this.content.setLength(this.content.length() + chunk);
    }

    @Override
    public String generateContent() {
        return this.content.toString();
    }

    @Override
    public void releaseResources() {
        this.content.clear();
    }
}

