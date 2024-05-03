/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.HpackDecoder;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersDecoder;
import io.netty.util.internal.ObjectUtil;

public class DefaultHttp2HeadersDecoder
implements Http2HeadersDecoder,
Http2HeadersDecoder.Configuration {
    private static final float HEADERS_COUNT_WEIGHT_NEW = 0.2f;
    private static final float HEADERS_COUNT_WEIGHT_HISTORICAL = 0.8f;
    private final HpackDecoder hpackDecoder;
    private final boolean validateHeaders;
    private long maxHeaderListSizeGoAway;
    private float headerArraySizeAccumulator = 8.0f;

    public DefaultHttp2HeadersDecoder() {
        this(true);
    }

    public DefaultHttp2HeadersDecoder(boolean validateHeaders) {
        this(validateHeaders, 8192L);
    }

    public DefaultHttp2HeadersDecoder(boolean validateHeaders, long maxHeaderListSize) {
        this(validateHeaders, maxHeaderListSize, -1);
    }

    public DefaultHttp2HeadersDecoder(boolean validateHeaders, long maxHeaderListSize, @Deprecated int initialHuffmanDecodeCapacity) {
        this(validateHeaders, new HpackDecoder(maxHeaderListSize));
    }

    DefaultHttp2HeadersDecoder(boolean validateHeaders, HpackDecoder hpackDecoder) {
        this.hpackDecoder = ObjectUtil.checkNotNull(hpackDecoder, "hpackDecoder");
        this.validateHeaders = validateHeaders;
        this.maxHeaderListSizeGoAway = Http2CodecUtil.calculateMaxHeaderListSizeGoAway(hpackDecoder.getMaxHeaderListSize());
    }

    @Override
    public void maxHeaderTableSize(long max) throws Http2Exception {
        this.hpackDecoder.setMaxHeaderTableSize(max);
    }

    @Override
    public long maxHeaderTableSize() {
        return this.hpackDecoder.getMaxHeaderTableSize();
    }

    @Override
    public void maxHeaderListSize(long max, long goAwayMax) throws Http2Exception {
        if (goAwayMax < max || goAwayMax < 0L) {
            throw Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, "Header List Size GO_AWAY %d must be non-negative and >= %d", goAwayMax, max);
        }
        this.hpackDecoder.setMaxHeaderListSize(max);
        this.maxHeaderListSizeGoAway = goAwayMax;
    }

    @Override
    public long maxHeaderListSize() {
        return this.hpackDecoder.getMaxHeaderListSize();
    }

    @Override
    public long maxHeaderListSizeGoAway() {
        return this.maxHeaderListSizeGoAway;
    }

    @Override
    public Http2HeadersDecoder.Configuration configuration() {
        return this;
    }

    @Override
    public Http2Headers decodeHeaders(int streamId, ByteBuf headerBlock) throws Http2Exception {
        try {
            Http2Headers headers = this.newHeaders();
            this.hpackDecoder.decode(streamId, headerBlock, headers, this.validateHeaders);
            this.headerArraySizeAccumulator = 0.2f * (float)headers.size() + 0.8f * this.headerArraySizeAccumulator;
            return headers;
        } catch (Http2Exception e) {
            throw e;
        } catch (Throwable e) {
            throw Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, e, e.getMessage(), new Object[0]);
        }
    }

    protected final int numberOfHeadersGuess() {
        return (int)this.headerArraySizeAccumulator;
    }

    protected final boolean validateHeaders() {
        return this.validateHeaders;
    }

    protected Http2Headers newHeaders() {
        return new DefaultHttp2Headers(this.validateHeaders, (int)this.headerArraySizeAccumulator);
    }
}

