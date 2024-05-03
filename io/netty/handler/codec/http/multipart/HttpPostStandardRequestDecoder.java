/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http.multipart;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.CaseIgnoringComparator;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostBodyUtil;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import io.netty.util.ByteProcessor;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HttpPostStandardRequestDecoder
implements InterfaceHttpPostRequestDecoder {
    private final HttpDataFactory factory;
    private final HttpRequest request;
    private final Charset charset;
    private boolean isLastChunk;
    private final List<InterfaceHttpData> bodyListHttpData = new ArrayList<InterfaceHttpData>();
    private final Map<String, List<InterfaceHttpData>> bodyMapHttpData = new TreeMap<CharSequence, List<InterfaceHttpData>>(CaseIgnoringComparator.INSTANCE);
    private ByteBuf undecodedChunk;
    private int bodyListHttpDataRank;
    private HttpPostRequestDecoder.MultiPartStatus currentStatus = HttpPostRequestDecoder.MultiPartStatus.NOTSTARTED;
    private Attribute currentAttribute;
    private boolean destroyed;
    private int discardThreshold = 0xA00000;

    public HttpPostStandardRequestDecoder(HttpRequest request) {
        this(new DefaultHttpDataFactory(16384L), request, HttpConstants.DEFAULT_CHARSET);
    }

    public HttpPostStandardRequestDecoder(HttpDataFactory factory, HttpRequest request) {
        this(factory, request, HttpConstants.DEFAULT_CHARSET);
    }

    public HttpPostStandardRequestDecoder(HttpDataFactory factory, HttpRequest request, Charset charset) {
        this.request = ObjectUtil.checkNotNull(request, "request");
        this.charset = ObjectUtil.checkNotNull(charset, "charset");
        this.factory = ObjectUtil.checkNotNull(factory, "factory");
        try {
            if (request instanceof HttpContent) {
                this.offer((HttpContent)((Object)request));
            } else {
                this.parseBody();
            }
        } catch (Throwable e) {
            this.destroy();
            PlatformDependent.throwException(e);
        }
    }

    private void checkDestroyed() {
        if (this.destroyed) {
            throw new IllegalStateException(HttpPostStandardRequestDecoder.class.getSimpleName() + " was destroyed already");
        }
    }

    @Override
    public boolean isMultipart() {
        this.checkDestroyed();
        return false;
    }

    @Override
    public void setDiscardThreshold(int discardThreshold) {
        this.discardThreshold = ObjectUtil.checkPositiveOrZero(discardThreshold, "discardThreshold");
    }

    @Override
    public int getDiscardThreshold() {
        return this.discardThreshold;
    }

    @Override
    public List<InterfaceHttpData> getBodyHttpDatas() {
        this.checkDestroyed();
        if (!this.isLastChunk) {
            throw new HttpPostRequestDecoder.NotEnoughDataDecoderException();
        }
        return this.bodyListHttpData;
    }

    @Override
    public List<InterfaceHttpData> getBodyHttpDatas(String name) {
        this.checkDestroyed();
        if (!this.isLastChunk) {
            throw new HttpPostRequestDecoder.NotEnoughDataDecoderException();
        }
        return this.bodyMapHttpData.get(name);
    }

    @Override
    public InterfaceHttpData getBodyHttpData(String name) {
        this.checkDestroyed();
        if (!this.isLastChunk) {
            throw new HttpPostRequestDecoder.NotEnoughDataDecoderException();
        }
        List<InterfaceHttpData> list = this.bodyMapHttpData.get(name);
        if (list != null) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public HttpPostStandardRequestDecoder offer(HttpContent content) {
        this.checkDestroyed();
        if (content instanceof LastHttpContent) {
            this.isLastChunk = true;
        }
        ByteBuf buf = content.content();
        if (this.undecodedChunk == null) {
            this.undecodedChunk = buf.alloc().buffer(buf.readableBytes()).writeBytes(buf);
        } else {
            this.undecodedChunk.writeBytes(buf);
        }
        this.parseBody();
        if (this.undecodedChunk != null && this.undecodedChunk.writerIndex() > this.discardThreshold) {
            if (this.undecodedChunk.refCnt() == 1) {
                this.undecodedChunk.discardReadBytes();
            } else {
                ByteBuf buffer = this.undecodedChunk.alloc().buffer(this.undecodedChunk.readableBytes());
                buffer.writeBytes(this.undecodedChunk);
                this.undecodedChunk.release();
                this.undecodedChunk = buffer;
            }
        }
        return this;
    }

    @Override
    public boolean hasNext() {
        this.checkDestroyed();
        if (this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.EPILOGUE && this.bodyListHttpDataRank >= this.bodyListHttpData.size()) {
            throw new HttpPostRequestDecoder.EndOfDataDecoderException();
        }
        return !this.bodyListHttpData.isEmpty() && this.bodyListHttpDataRank < this.bodyListHttpData.size();
    }

    @Override
    public InterfaceHttpData next() {
        this.checkDestroyed();
        if (this.hasNext()) {
            return this.bodyListHttpData.get(this.bodyListHttpDataRank++);
        }
        return null;
    }

    @Override
    public InterfaceHttpData currentPartialHttpData() {
        return this.currentAttribute;
    }

    private void parseBody() {
        if (this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.PREEPILOGUE || this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.EPILOGUE) {
            if (this.isLastChunk) {
                this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.EPILOGUE;
            }
            return;
        }
        this.parseBodyAttributes();
    }

    protected void addHttpData(InterfaceHttpData data) {
        if (data == null) {
            return;
        }
        List<InterfaceHttpData> datas = this.bodyMapHttpData.get(data.getName());
        if (datas == null) {
            datas = new ArrayList<InterfaceHttpData>(1);
            this.bodyMapHttpData.put(data.getName(), datas);
        }
        datas.add(data);
        this.bodyListHttpData.add(data);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void parseBodyAttributesStandard() {
        int firstpos;
        int currentpos = firstpos = this.undecodedChunk.readerIndex();
        if (this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.NOTSTARTED) {
            this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.DISPOSITION;
        }
        boolean contRead = true;
        try {
            int ampersandpos;
            block8: while (this.undecodedChunk.isReadable() && contRead) {
                char read = (char)this.undecodedChunk.readUnsignedByte();
                ++currentpos;
                switch (this.currentStatus) {
                    case DISPOSITION: {
                        String key;
                        if (read == '=') {
                            this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.FIELD;
                            int equalpos = currentpos - 1;
                            key = HttpPostStandardRequestDecoder.decodeAttribute(this.undecodedChunk.toString(firstpos, equalpos - firstpos, this.charset), this.charset);
                            this.currentAttribute = this.factory.createAttribute(this.request, key);
                            firstpos = currentpos;
                            continue block8;
                        }
                        if (read != '&') continue block8;
                        this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.DISPOSITION;
                        ampersandpos = currentpos - 1;
                        key = HttpPostStandardRequestDecoder.decodeAttribute(this.undecodedChunk.toString(firstpos, ampersandpos - firstpos, this.charset), this.charset);
                        this.currentAttribute = this.factory.createAttribute(this.request, key);
                        this.currentAttribute.setValue("");
                        this.addHttpData(this.currentAttribute);
                        this.currentAttribute = null;
                        firstpos = currentpos;
                        contRead = true;
                        continue block8;
                    }
                    case FIELD: {
                        if (read == '&') {
                            this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.DISPOSITION;
                            ampersandpos = currentpos - 1;
                            this.setFinalBuffer(this.undecodedChunk.retainedSlice(firstpos, ampersandpos - firstpos));
                            firstpos = currentpos;
                            contRead = true;
                            continue block8;
                        }
                        if (read == '\r') {
                            if (this.undecodedChunk.isReadable()) {
                                read = (char)this.undecodedChunk.readUnsignedByte();
                                if (read != '\n') throw new HttpPostRequestDecoder.ErrorDataDecoderException("Bad end of line");
                                this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.PREEPILOGUE;
                                ampersandpos = ++currentpos - 2;
                                this.setFinalBuffer(this.undecodedChunk.retainedSlice(firstpos, ampersandpos - firstpos));
                                firstpos = currentpos;
                                contRead = false;
                                continue block8;
                            }
                            --currentpos;
                            continue block8;
                        }
                        if (read != '\n') continue block8;
                        this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.PREEPILOGUE;
                        ampersandpos = currentpos - 1;
                        this.setFinalBuffer(this.undecodedChunk.retainedSlice(firstpos, ampersandpos - firstpos));
                        firstpos = currentpos;
                        contRead = false;
                        continue block8;
                    }
                }
                contRead = false;
            }
            if (this.isLastChunk && this.currentAttribute != null) {
                ampersandpos = currentpos;
                if (ampersandpos > firstpos) {
                    this.setFinalBuffer(this.undecodedChunk.retainedSlice(firstpos, ampersandpos - firstpos));
                } else if (!this.currentAttribute.isCompleted()) {
                    this.setFinalBuffer(Unpooled.EMPTY_BUFFER);
                }
                firstpos = currentpos;
                this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.EPILOGUE;
            } else if (contRead && this.currentAttribute != null && this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.FIELD) {
                this.currentAttribute.addContent(this.undecodedChunk.retainedSlice(firstpos, currentpos - firstpos), false);
                firstpos = currentpos;
            }
            this.undecodedChunk.readerIndex(firstpos);
            return;
        } catch (HttpPostRequestDecoder.ErrorDataDecoderException e) {
            this.undecodedChunk.readerIndex(firstpos);
            throw e;
        } catch (IOException e) {
            this.undecodedChunk.readerIndex(firstpos);
            throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
        } catch (IllegalArgumentException e) {
            this.undecodedChunk.readerIndex(firstpos);
            throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void parseBodyAttributes() {
        int firstpos;
        if (this.undecodedChunk == null) {
            return;
        }
        if (!this.undecodedChunk.hasArray()) {
            this.parseBodyAttributesStandard();
            return;
        }
        HttpPostBodyUtil.SeekAheadOptimize sao = new HttpPostBodyUtil.SeekAheadOptimize(this.undecodedChunk);
        int currentpos = firstpos = this.undecodedChunk.readerIndex();
        if (this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.NOTSTARTED) {
            this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.DISPOSITION;
        }
        boolean contRead = true;
        try {
            int ampersandpos;
            block8: while (sao.pos < sao.limit) {
                char read = (char)(sao.bytes[sao.pos++] & 0xFF);
                ++currentpos;
                switch (this.currentStatus) {
                    case DISPOSITION: {
                        String key;
                        if (read == '=') {
                            this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.FIELD;
                            int equalpos = currentpos - 1;
                            key = HttpPostStandardRequestDecoder.decodeAttribute(this.undecodedChunk.toString(firstpos, equalpos - firstpos, this.charset), this.charset);
                            this.currentAttribute = this.factory.createAttribute(this.request, key);
                            firstpos = currentpos;
                            continue block8;
                        }
                        if (read != '&') continue block8;
                        this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.DISPOSITION;
                        ampersandpos = currentpos - 1;
                        key = HttpPostStandardRequestDecoder.decodeAttribute(this.undecodedChunk.toString(firstpos, ampersandpos - firstpos, this.charset), this.charset);
                        this.currentAttribute = this.factory.createAttribute(this.request, key);
                        this.currentAttribute.setValue("");
                        this.addHttpData(this.currentAttribute);
                        this.currentAttribute = null;
                        firstpos = currentpos;
                        contRead = true;
                        continue block8;
                    }
                    case FIELD: {
                        if (read == '&') {
                            this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.DISPOSITION;
                            ampersandpos = currentpos - 1;
                            this.setFinalBuffer(this.undecodedChunk.retainedSlice(firstpos, ampersandpos - firstpos));
                            firstpos = currentpos;
                            contRead = true;
                            continue block8;
                        }
                        if (read == '\r') {
                            if (sao.pos < sao.limit) {
                                read = (char)(sao.bytes[sao.pos++] & 0xFF);
                                ++currentpos;
                                if (read != '\n') {
                                    sao.setReadPosition(0);
                                    throw new HttpPostRequestDecoder.ErrorDataDecoderException("Bad end of line");
                                }
                                this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.PREEPILOGUE;
                                ampersandpos = currentpos - 2;
                                sao.setReadPosition(0);
                                this.setFinalBuffer(this.undecodedChunk.retainedSlice(firstpos, ampersandpos - firstpos));
                                firstpos = currentpos;
                                contRead = false;
                                break block8;
                            }
                            if (sao.limit <= 0) continue block8;
                            --currentpos;
                            continue block8;
                        }
                        if (read != '\n') continue block8;
                        this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.PREEPILOGUE;
                        ampersandpos = currentpos - 1;
                        sao.setReadPosition(0);
                        this.setFinalBuffer(this.undecodedChunk.retainedSlice(firstpos, ampersandpos - firstpos));
                        firstpos = currentpos;
                        contRead = false;
                        break block8;
                    }
                }
                sao.setReadPosition(0);
                contRead = false;
                break;
            }
            if (this.isLastChunk && this.currentAttribute != null) {
                ampersandpos = currentpos;
                if (ampersandpos > firstpos) {
                    this.setFinalBuffer(this.undecodedChunk.retainedSlice(firstpos, ampersandpos - firstpos));
                } else if (!this.currentAttribute.isCompleted()) {
                    this.setFinalBuffer(Unpooled.EMPTY_BUFFER);
                }
                firstpos = currentpos;
                this.currentStatus = HttpPostRequestDecoder.MultiPartStatus.EPILOGUE;
            } else if (contRead && this.currentAttribute != null && this.currentStatus == HttpPostRequestDecoder.MultiPartStatus.FIELD) {
                this.currentAttribute.addContent(this.undecodedChunk.retainedSlice(firstpos, currentpos - firstpos), false);
                firstpos = currentpos;
            }
            this.undecodedChunk.readerIndex(firstpos);
            return;
        } catch (HttpPostRequestDecoder.ErrorDataDecoderException e) {
            this.undecodedChunk.readerIndex(firstpos);
            throw e;
        } catch (IOException e) {
            this.undecodedChunk.readerIndex(firstpos);
            throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
        } catch (IllegalArgumentException e) {
            this.undecodedChunk.readerIndex(firstpos);
            throw new HttpPostRequestDecoder.ErrorDataDecoderException(e);
        }
    }

    private void setFinalBuffer(ByteBuf buffer) throws IOException {
        this.currentAttribute.addContent(buffer, true);
        ByteBuf decodedBuf = HttpPostStandardRequestDecoder.decodeAttribute(this.currentAttribute.getByteBuf(), this.charset);
        if (decodedBuf != null) {
            this.currentAttribute.setContent(decodedBuf);
        }
        this.addHttpData(this.currentAttribute);
        this.currentAttribute = null;
    }

    private static String decodeAttribute(String s, Charset charset) {
        try {
            return QueryStringDecoder.decodeComponent(s, charset);
        } catch (IllegalArgumentException e) {
            throw new HttpPostRequestDecoder.ErrorDataDecoderException("Bad string: '" + s + '\'', e);
        }
    }

    private static ByteBuf decodeAttribute(ByteBuf b, Charset charset) {
        int firstEscaped = b.forEachByte(new UrlEncodedDetector());
        if (firstEscaped == -1) {
            return null;
        }
        ByteBuf buf = b.alloc().buffer(b.readableBytes());
        UrlDecoder urlDecode = new UrlDecoder(buf);
        int idx = b.forEachByte(urlDecode);
        if (urlDecode.nextEscapedIdx != 0) {
            if (idx == -1) {
                idx = b.readableBytes() - 1;
            }
            buf.release();
            throw new HttpPostRequestDecoder.ErrorDataDecoderException(String.format("Invalid hex byte at index '%d' in string: '%s'", idx -= urlDecode.nextEscapedIdx - 1, b.toString(charset)));
        }
        return buf;
    }

    @Override
    public void destroy() {
        this.cleanFiles();
        for (InterfaceHttpData httpData : this.bodyListHttpData) {
            if (httpData.refCnt() <= 0) continue;
            httpData.release();
        }
        this.destroyed = true;
        if (this.undecodedChunk != null && this.undecodedChunk.refCnt() > 0) {
            this.undecodedChunk.release();
            this.undecodedChunk = null;
        }
    }

    @Override
    public void cleanFiles() {
        this.checkDestroyed();
        this.factory.cleanRequestHttpData(this.request);
    }

    @Override
    public void removeHttpDataFromClean(InterfaceHttpData data) {
        this.checkDestroyed();
        this.factory.removeHttpDataFromClean(this.request, data);
    }

    private static final class UrlDecoder
    implements ByteProcessor {
        private final ByteBuf output;
        private int nextEscapedIdx;
        private byte hiByte;

        UrlDecoder(ByteBuf output) {
            this.output = output;
        }

        @Override
        public boolean process(byte value) {
            if (this.nextEscapedIdx != 0) {
                if (this.nextEscapedIdx == 1) {
                    this.hiByte = value;
                    ++this.nextEscapedIdx;
                } else {
                    int hi = StringUtil.decodeHexNibble((char)((char)this.hiByte));
                    int lo = StringUtil.decodeHexNibble((char)((char)value));
                    if (hi == -1 || lo == -1) {
                        ++this.nextEscapedIdx;
                        return false;
                    }
                    this.output.writeByte((hi << 4) + lo);
                    this.nextEscapedIdx = 0;
                }
            } else if (value == 37) {
                this.nextEscapedIdx = 1;
            } else if (value == 43) {
                this.output.writeByte(32);
            } else {
                this.output.writeByte(value);
            }
            return true;
        }
    }

    private static final class UrlEncodedDetector
    implements ByteProcessor {
        private UrlEncodedDetector() {
        }

        @Override
        public boolean process(byte value) throws Exception {
            return value != 37 && value != 43;
        }
    }
}

