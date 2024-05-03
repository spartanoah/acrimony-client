/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http2.HpackDynamicTable;
import io.netty.handler.codec.http2.HpackHeaderField;
import io.netty.handler.codec.http2.HpackHuffmanDecoder;
import io.netty.handler.codec.http2.HpackStaticTable;
import io.netty.handler.codec.http2.HpackUtil;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.util.AsciiString;
import io.netty.util.internal.ObjectUtil;

final class HpackDecoder {
    private static final Http2Exception DECODE_ULE_128_DECOMPRESSION_EXCEPTION = Http2Exception.newStatic(Http2Error.COMPRESSION_ERROR, "HPACK - decompression failure", Http2Exception.ShutdownHint.HARD_SHUTDOWN, HpackDecoder.class, "decodeULE128(..)");
    private static final Http2Exception DECODE_ULE_128_TO_LONG_DECOMPRESSION_EXCEPTION = Http2Exception.newStatic(Http2Error.COMPRESSION_ERROR, "HPACK - long overflow", Http2Exception.ShutdownHint.HARD_SHUTDOWN, HpackDecoder.class, "decodeULE128(..)");
    private static final Http2Exception DECODE_ULE_128_TO_INT_DECOMPRESSION_EXCEPTION = Http2Exception.newStatic(Http2Error.COMPRESSION_ERROR, "HPACK - int overflow", Http2Exception.ShutdownHint.HARD_SHUTDOWN, HpackDecoder.class, "decodeULE128ToInt(..)");
    private static final Http2Exception DECODE_ILLEGAL_INDEX_VALUE = Http2Exception.newStatic(Http2Error.COMPRESSION_ERROR, "HPACK - illegal index value", Http2Exception.ShutdownHint.HARD_SHUTDOWN, HpackDecoder.class, "decode(..)");
    private static final Http2Exception INDEX_HEADER_ILLEGAL_INDEX_VALUE = Http2Exception.newStatic(Http2Error.COMPRESSION_ERROR, "HPACK - illegal index value", Http2Exception.ShutdownHint.HARD_SHUTDOWN, HpackDecoder.class, "indexHeader(..)");
    private static final Http2Exception READ_NAME_ILLEGAL_INDEX_VALUE = Http2Exception.newStatic(Http2Error.COMPRESSION_ERROR, "HPACK - illegal index value", Http2Exception.ShutdownHint.HARD_SHUTDOWN, HpackDecoder.class, "readName(..)");
    private static final Http2Exception INVALID_MAX_DYNAMIC_TABLE_SIZE = Http2Exception.newStatic(Http2Error.COMPRESSION_ERROR, "HPACK - invalid max dynamic table size", Http2Exception.ShutdownHint.HARD_SHUTDOWN, HpackDecoder.class, "setDynamicTableSize(..)");
    private static final Http2Exception MAX_DYNAMIC_TABLE_SIZE_CHANGE_REQUIRED = Http2Exception.newStatic(Http2Error.COMPRESSION_ERROR, "HPACK - max dynamic table size change required", Http2Exception.ShutdownHint.HARD_SHUTDOWN, HpackDecoder.class, "decode(..)");
    private static final byte READ_HEADER_REPRESENTATION = 0;
    private static final byte READ_MAX_DYNAMIC_TABLE_SIZE = 1;
    private static final byte READ_INDEXED_HEADER = 2;
    private static final byte READ_INDEXED_HEADER_NAME = 3;
    private static final byte READ_LITERAL_HEADER_NAME_LENGTH_PREFIX = 4;
    private static final byte READ_LITERAL_HEADER_NAME_LENGTH = 5;
    private static final byte READ_LITERAL_HEADER_NAME = 6;
    private static final byte READ_LITERAL_HEADER_VALUE_LENGTH_PREFIX = 7;
    private static final byte READ_LITERAL_HEADER_VALUE_LENGTH = 8;
    private static final byte READ_LITERAL_HEADER_VALUE = 9;
    private final HpackHuffmanDecoder huffmanDecoder = new HpackHuffmanDecoder();
    private final HpackDynamicTable hpackDynamicTable;
    private long maxHeaderListSize;
    private long maxDynamicTableSize;
    private long encoderMaxDynamicTableSize;
    private boolean maxDynamicTableSizeChangeRequired;

    HpackDecoder(long maxHeaderListSize) {
        this(maxHeaderListSize, 4096);
    }

    HpackDecoder(long maxHeaderListSize, int maxHeaderTableSize) {
        this.maxHeaderListSize = ObjectUtil.checkPositive(maxHeaderListSize, "maxHeaderListSize");
        this.maxDynamicTableSize = this.encoderMaxDynamicTableSize = (long)maxHeaderTableSize;
        this.maxDynamicTableSizeChangeRequired = false;
        this.hpackDynamicTable = new HpackDynamicTable(maxHeaderTableSize);
    }

    public void decode(int streamId, ByteBuf in, Http2Headers headers, boolean validateHeaders) throws Http2Exception {
        Http2HeadersSink sink = new Http2HeadersSink(streamId, headers, this.maxHeaderListSize, validateHeaders);
        this.decode(in, sink);
        sink.finish();
    }

    private void decode(ByteBuf in, Sink sink) throws Http2Exception {
        int index = 0;
        int nameLength = 0;
        int valueLength = 0;
        int state = 0;
        boolean huffmanEncoded = false;
        CharSequence name = null;
        HpackUtil.IndexType indexType = HpackUtil.IndexType.NONE;
        block28: while (in.isReadable()) {
            switch (state) {
                case 0: {
                    HpackHeaderField indexedHeader;
                    byte b = in.readByte();
                    if (this.maxDynamicTableSizeChangeRequired && (b & 0xE0) != 32) {
                        throw MAX_DYNAMIC_TABLE_SIZE_CHANGE_REQUIRED;
                    }
                    if (b < 0) {
                        index = b & 0x7F;
                        switch (index) {
                            case 0: {
                                throw DECODE_ILLEGAL_INDEX_VALUE;
                            }
                            case 127: {
                                state = 2;
                                continue block28;
                            }
                        }
                        indexedHeader = this.getIndexedHeader(index);
                        sink.appendToHeaderList(indexedHeader.name, indexedHeader.value);
                        continue block28;
                    }
                    if ((b & 0x40) == 64) {
                        indexType = HpackUtil.IndexType.INCREMENTAL;
                        index = b & 0x3F;
                        switch (index) {
                            case 0: {
                                state = 4;
                                continue block28;
                            }
                            case 63: {
                                state = 3;
                                continue block28;
                            }
                        }
                        name = this.readName(index);
                        nameLength = name.length();
                        state = 7;
                        continue block28;
                    }
                    if ((b & 0x20) == 32) {
                        index = b & 0x1F;
                        if (index == 31) {
                            state = 1;
                            continue block28;
                        }
                        this.setDynamicTableSize(index);
                        state = 0;
                        continue block28;
                    }
                    indexType = (b & 0x10) == 16 ? HpackUtil.IndexType.NEVER : HpackUtil.IndexType.NONE;
                    index = b & 0xF;
                    switch (index) {
                        case 0: {
                            state = 4;
                            continue block28;
                        }
                        case 15: {
                            state = 3;
                            continue block28;
                        }
                    }
                    name = this.readName(index);
                    nameLength = name.length();
                    state = 7;
                    continue block28;
                }
                case 1: {
                    this.setDynamicTableSize(HpackDecoder.decodeULE128(in, (long)index));
                    state = 0;
                    continue block28;
                }
                case 2: {
                    HpackHeaderField indexedHeader = this.getIndexedHeader(HpackDecoder.decodeULE128(in, index));
                    sink.appendToHeaderList(indexedHeader.name, indexedHeader.value);
                    state = 0;
                    continue block28;
                }
                case 3: {
                    name = this.readName(HpackDecoder.decodeULE128(in, index));
                    nameLength = name.length();
                    state = 7;
                    continue block28;
                }
                case 4: {
                    byte b = in.readByte();
                    huffmanEncoded = (b & 0x80) == 128;
                    index = b & 0x7F;
                    if (index == 127) {
                        state = 5;
                        continue block28;
                    }
                    nameLength = index;
                    state = 6;
                    continue block28;
                }
                case 5: {
                    nameLength = HpackDecoder.decodeULE128(in, index);
                    state = 6;
                    continue block28;
                }
                case 6: {
                    if (in.readableBytes() < nameLength) {
                        throw HpackDecoder.notEnoughDataException(in);
                    }
                    name = this.readStringLiteral(in, nameLength, huffmanEncoded);
                    state = 7;
                    continue block28;
                }
                case 7: {
                    byte b = in.readByte();
                    huffmanEncoded = (b & 0x80) == 128;
                    index = b & 0x7F;
                    switch (index) {
                        case 127: {
                            state = 8;
                            continue block28;
                        }
                        case 0: {
                            this.insertHeader(sink, name, AsciiString.EMPTY_STRING, indexType);
                            state = 0;
                            continue block28;
                        }
                    }
                    valueLength = index;
                    state = 9;
                    continue block28;
                }
                case 8: {
                    valueLength = HpackDecoder.decodeULE128(in, index);
                    state = 9;
                    continue block28;
                }
                case 9: {
                    if (in.readableBytes() < valueLength) {
                        throw HpackDecoder.notEnoughDataException(in);
                    }
                    CharSequence value = this.readStringLiteral(in, valueLength, huffmanEncoded);
                    this.insertHeader(sink, name, value, indexType);
                    state = 0;
                    continue block28;
                }
            }
            throw new Error("should not reach here state: " + state);
        }
        if (state != 0) {
            throw Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, "Incomplete header block fragment.", new Object[0]);
        }
    }

    public void setMaxHeaderTableSize(long maxHeaderTableSize) throws Http2Exception {
        if (maxHeaderTableSize < 0L || maxHeaderTableSize > 0xFFFFFFFFL) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Header Table Size must be >= %d and <= %d but was %d", 0L, 0xFFFFFFFFL, maxHeaderTableSize);
        }
        this.maxDynamicTableSize = maxHeaderTableSize;
        if (this.maxDynamicTableSize < this.encoderMaxDynamicTableSize) {
            this.maxDynamicTableSizeChangeRequired = true;
            this.hpackDynamicTable.setCapacity(this.maxDynamicTableSize);
        }
    }

    @Deprecated
    public void setMaxHeaderListSize(long maxHeaderListSize, long maxHeaderListSizeGoAway) throws Http2Exception {
        this.setMaxHeaderListSize(maxHeaderListSize);
    }

    public void setMaxHeaderListSize(long maxHeaderListSize) throws Http2Exception {
        if (maxHeaderListSize < 0L || maxHeaderListSize > 0xFFFFFFFFL) {
            throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Header List Size must be >= %d and <= %d but was %d", 0L, 0xFFFFFFFFL, maxHeaderListSize);
        }
        this.maxHeaderListSize = maxHeaderListSize;
    }

    public long getMaxHeaderListSize() {
        return this.maxHeaderListSize;
    }

    public long getMaxHeaderTableSize() {
        return this.hpackDynamicTable.capacity();
    }

    int length() {
        return this.hpackDynamicTable.length();
    }

    long size() {
        return this.hpackDynamicTable.size();
    }

    HpackHeaderField getHeaderField(int index) {
        return this.hpackDynamicTable.getEntry(index + 1);
    }

    private void setDynamicTableSize(long dynamicTableSize) throws Http2Exception {
        if (dynamicTableSize > this.maxDynamicTableSize) {
            throw INVALID_MAX_DYNAMIC_TABLE_SIZE;
        }
        this.encoderMaxDynamicTableSize = dynamicTableSize;
        this.maxDynamicTableSizeChangeRequired = false;
        this.hpackDynamicTable.setCapacity(dynamicTableSize);
    }

    private static HeaderType validate(int streamId, CharSequence name, HeaderType previousHeaderType) throws Http2Exception {
        if (Http2Headers.PseudoHeaderName.hasPseudoHeaderFormat(name)) {
            HeaderType currentHeaderType;
            if (previousHeaderType == HeaderType.REGULAR_HEADER) {
                throw Http2Exception.streamError(streamId, Http2Error.PROTOCOL_ERROR, "Pseudo-header field '%s' found after regular header.", name);
            }
            Http2Headers.PseudoHeaderName pseudoHeader = Http2Headers.PseudoHeaderName.getPseudoHeader(name);
            if (pseudoHeader == null) {
                throw Http2Exception.streamError(streamId, Http2Error.PROTOCOL_ERROR, "Invalid HTTP/2 pseudo-header '%s' encountered.", name);
            }
            HeaderType headerType = currentHeaderType = pseudoHeader.isRequestOnly() ? HeaderType.REQUEST_PSEUDO_HEADER : HeaderType.RESPONSE_PSEUDO_HEADER;
            if (previousHeaderType != null && currentHeaderType != previousHeaderType) {
                throw Http2Exception.streamError(streamId, Http2Error.PROTOCOL_ERROR, "Mix of request and response pseudo-headers.", new Object[0]);
            }
            return currentHeaderType;
        }
        return HeaderType.REGULAR_HEADER;
    }

    private CharSequence readName(int index) throws Http2Exception {
        if (index <= HpackStaticTable.length) {
            HpackHeaderField hpackHeaderField = HpackStaticTable.getEntry(index);
            return hpackHeaderField.name;
        }
        if (index - HpackStaticTable.length <= this.hpackDynamicTable.length()) {
            HpackHeaderField hpackHeaderField = this.hpackDynamicTable.getEntry(index - HpackStaticTable.length);
            return hpackHeaderField.name;
        }
        throw READ_NAME_ILLEGAL_INDEX_VALUE;
    }

    private HpackHeaderField getIndexedHeader(int index) throws Http2Exception {
        if (index <= HpackStaticTable.length) {
            return HpackStaticTable.getEntry(index);
        }
        if (index - HpackStaticTable.length <= this.hpackDynamicTable.length()) {
            return this.hpackDynamicTable.getEntry(index - HpackStaticTable.length);
        }
        throw INDEX_HEADER_ILLEGAL_INDEX_VALUE;
    }

    private void insertHeader(Sink sink, CharSequence name, CharSequence value, HpackUtil.IndexType indexType) {
        sink.appendToHeaderList(name, value);
        switch (indexType) {
            case NONE: 
            case NEVER: {
                break;
            }
            case INCREMENTAL: {
                this.hpackDynamicTable.add(new HpackHeaderField(name, value));
                break;
            }
            default: {
                throw new Error("should not reach here");
            }
        }
    }

    private CharSequence readStringLiteral(ByteBuf in, int length, boolean huffmanEncoded) throws Http2Exception {
        if (huffmanEncoded) {
            return this.huffmanDecoder.decode(in, length);
        }
        byte[] buf = new byte[length];
        in.readBytes(buf);
        return new AsciiString(buf, false);
    }

    private static IllegalArgumentException notEnoughDataException(ByteBuf in) {
        return new IllegalArgumentException("decode only works with an entire header block! " + in);
    }

    static int decodeULE128(ByteBuf in, int result) throws Http2Exception {
        int readerIndex = in.readerIndex();
        long v = HpackDecoder.decodeULE128(in, (long)result);
        if (v > Integer.MAX_VALUE) {
            in.readerIndex(readerIndex);
            throw DECODE_ULE_128_TO_INT_DECOMPRESSION_EXCEPTION;
        }
        return (int)v;
    }

    static long decodeULE128(ByteBuf in, long result) throws Http2Exception {
        assert (result <= 127L && result >= 0L);
        boolean resultStartedAtZero = result == 0L;
        int writerIndex = in.writerIndex();
        int readerIndex = in.readerIndex();
        int shift = 0;
        while (readerIndex < writerIndex) {
            byte b = in.getByte(readerIndex);
            if (shift == 56 && ((b & 0x80) != 0 || b == 127 && !resultStartedAtZero)) {
                throw DECODE_ULE_128_TO_LONG_DECOMPRESSION_EXCEPTION;
            }
            if ((b & 0x80) == 0) {
                in.readerIndex(readerIndex + 1);
                return result + (((long)b & 0x7FL) << shift);
            }
            result += ((long)b & 0x7FL) << shift;
            ++readerIndex;
            shift += 7;
        }
        throw DECODE_ULE_128_DECOMPRESSION_EXCEPTION;
    }

    private static final class Http2HeadersSink
    implements Sink {
        private final Http2Headers headers;
        private final long maxHeaderListSize;
        private final int streamId;
        private final boolean validate;
        private long headersLength;
        private boolean exceededMaxLength;
        private HeaderType previousType;
        private Http2Exception validationException;

        Http2HeadersSink(int streamId, Http2Headers headers, long maxHeaderListSize, boolean validate) {
            this.headers = headers;
            this.maxHeaderListSize = maxHeaderListSize;
            this.streamId = streamId;
            this.validate = validate;
        }

        @Override
        public void finish() throws Http2Exception {
            if (this.exceededMaxLength) {
                Http2CodecUtil.headerListSizeExceeded(this.streamId, this.maxHeaderListSize, true);
            } else if (this.validationException != null) {
                throw this.validationException;
            }
        }

        @Override
        public void appendToHeaderList(CharSequence name, CharSequence value) {
            this.headersLength += HpackHeaderField.sizeOf(name, value);
            this.exceededMaxLength |= this.headersLength > this.maxHeaderListSize;
            if (this.exceededMaxLength || this.validationException != null) {
                return;
            }
            if (this.validate) {
                try {
                    this.previousType = HpackDecoder.validate(this.streamId, name, this.previousType);
                } catch (Http2Exception ex) {
                    this.validationException = ex;
                    return;
                }
            }
            this.headers.add(name, value);
        }
    }

    private static interface Sink {
        public void appendToHeaderList(CharSequence var1, CharSequence var2);

        public void finish() throws Http2Exception;
    }

    private static enum HeaderType {
        REGULAR_HEADER,
        REQUEST_PSEUDO_HEADER,
        RESPONSE_PSEUDO_HEADER;

    }
}

