/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.entity;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.StreamChannel;
import org.apache.hc.core5.http.nio.entity.AbstractBinAsyncEntityProducer;
import org.apache.hc.core5.http.nio.entity.AbstractCharAsyncEntityProducer;
import org.apache.hc.core5.http.nio.entity.AsyncEntityProducerWrapper;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityProducer;
import org.apache.hc.core5.http.nio.entity.FileEntityProducer;
import org.apache.hc.core5.net.URLEncodedUtils;

public final class AsyncEntityProducers {
    private AsyncEntityProducers() {
    }

    public static AsyncEntityProducer create(String content, ContentType contentType) {
        return new BasicAsyncEntityProducer(content, contentType);
    }

    public static AsyncEntityProducer create(String content, Charset charset) {
        return new BasicAsyncEntityProducer(content, ContentType.TEXT_PLAIN.withCharset(charset));
    }

    public static AsyncEntityProducer create(String content) {
        return new BasicAsyncEntityProducer(content, ContentType.TEXT_PLAIN);
    }

    public static AsyncEntityProducer create(byte[] content, ContentType contentType) {
        return new BasicAsyncEntityProducer(content, contentType);
    }

    public static AsyncEntityProducer create(File content, ContentType contentType) {
        return new FileEntityProducer(content, contentType);
    }

    public static AsyncEntityProducer createUrlEncoded(Iterable<? extends NameValuePair> parameters, Charset charset) {
        ContentType contentType = charset != null ? ContentType.APPLICATION_FORM_URLENCODED.withCharset(charset) : ContentType.APPLICATION_FORM_URLENCODED;
        return AsyncEntityProducers.create(URLEncodedUtils.format(parameters, contentType.getCharset()), contentType);
    }

    public static AsyncEntityProducer createBinary(final Callback<StreamChannel<ByteBuffer>> callback, ContentType contentType) {
        return new AbstractBinAsyncEntityProducer(0, contentType){

            @Override
            protected int availableData() {
                return Integer.MAX_VALUE;
            }

            @Override
            protected void produceData(StreamChannel<ByteBuffer> channel) throws IOException {
                callback.execute(channel);
            }

            @Override
            public boolean isRepeatable() {
                return false;
            }

            @Override
            public void failed(Exception cause) {
            }
        };
    }

    public static AsyncEntityProducer createText(final Callback<StreamChannel<CharBuffer>> callback, ContentType contentType) {
        return new AbstractCharAsyncEntityProducer(4096, 2048, contentType){

            @Override
            protected int availableData() {
                return Integer.MAX_VALUE;
            }

            @Override
            protected void produceData(StreamChannel<CharBuffer> channel) throws IOException {
                callback.execute(channel);
            }

            @Override
            public boolean isRepeatable() {
                return false;
            }

            @Override
            public void failed(Exception cause) {
            }
        };
    }

    public static AsyncEntityProducer withTrailers(AsyncEntityProducer entity, final Header ... trailers) {
        return new AsyncEntityProducerWrapper(entity){

            @Override
            public boolean isChunked() {
                return true;
            }

            @Override
            public long getContentLength() {
                return -1L;
            }

            @Override
            public Set<String> getTrailerNames() {
                LinkedHashSet<String> names = new LinkedHashSet<String>();
                for (Header trailer : trailers) {
                    names.add(trailer.getName());
                }
                return names;
            }

            @Override
            public void produce(final DataStreamChannel channel) throws IOException {
                super.produce(new DataStreamChannel(){

                    @Override
                    public void requestOutput() {
                        channel.requestOutput();
                    }

                    @Override
                    public int write(ByteBuffer src) throws IOException {
                        return channel.write(src);
                    }

                    @Override
                    public void endStream(List<? extends Header> p) throws IOException {
                        List<Header> allTrailers;
                        if (p != null && !p.isEmpty()) {
                            allTrailers = new ArrayList<Header>(p);
                            allTrailers.addAll(Arrays.asList(trailers));
                        } else {
                            allTrailers = Arrays.asList(trailers);
                        }
                        channel.endStream(allTrailers);
                    }

                    @Override
                    public void endStream() throws IOException {
                        channel.endStream();
                    }
                });
            }
        };
    }

    public static AsyncEntityProducer create(String content, ContentType contentType, Header ... trailers) {
        return AsyncEntityProducers.withTrailers(AsyncEntityProducers.create(content, contentType), trailers);
    }

    public static AsyncEntityProducer create(String content, Charset charset, Header ... trailers) {
        return AsyncEntityProducers.withTrailers(AsyncEntityProducers.create(content, charset), trailers);
    }

    public static AsyncEntityProducer create(String content, Header ... trailers) {
        return AsyncEntityProducers.withTrailers(AsyncEntityProducers.create(content), trailers);
    }

    public static AsyncEntityProducer create(byte[] content, ContentType contentType, Header ... trailers) {
        return AsyncEntityProducers.withTrailers(AsyncEntityProducers.create(content, contentType), trailers);
    }

    public static AsyncEntityProducer create(File content, ContentType contentType, Header ... trailers) {
        return AsyncEntityProducers.withTrailers(AsyncEntityProducers.create(content, contentType), trailers);
    }

    public static AsyncEntityProducer createBinary(Callback<StreamChannel<ByteBuffer>> callback, ContentType contentType, Header ... trailers) {
        return AsyncEntityProducers.withTrailers(AsyncEntityProducers.createBinary(callback, contentType), trailers);
    }

    public static AsyncEntityProducer createText(Callback<StreamChannel<CharBuffer>> callback, ContentType contentType, Header ... trailers) {
        return AsyncEntityProducers.withTrailers(AsyncEntityProducers.createText(callback, contentType), trailers);
    }
}

