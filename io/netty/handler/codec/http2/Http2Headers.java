/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import io.netty.handler.codec.Headers;
import io.netty.handler.codec.http2.CharSequenceMap;
import io.netty.util.AsciiString;
import java.util.Iterator;
import java.util.Map;

public interface Http2Headers
extends Headers<CharSequence, CharSequence, Http2Headers> {
    @Override
    public Iterator<Map.Entry<CharSequence, CharSequence>> iterator();

    public Iterator<CharSequence> valueIterator(CharSequence var1);

    public Http2Headers method(CharSequence var1);

    public Http2Headers scheme(CharSequence var1);

    public Http2Headers authority(CharSequence var1);

    public Http2Headers path(CharSequence var1);

    public Http2Headers status(CharSequence var1);

    public CharSequence method();

    public CharSequence scheme();

    public CharSequence authority();

    public CharSequence path();

    public CharSequence status();

    public boolean contains(CharSequence var1, CharSequence var2, boolean var3);

    public static enum PseudoHeaderName {
        METHOD(":method", true),
        SCHEME(":scheme", true),
        AUTHORITY(":authority", true),
        PATH(":path", true),
        STATUS(":status", false),
        PROTOCOL(":protocol", true);

        private static final char PSEUDO_HEADER_PREFIX = ':';
        private static final byte PSEUDO_HEADER_PREFIX_BYTE = 58;
        private final AsciiString value;
        private final boolean requestOnly;
        private static final CharSequenceMap<PseudoHeaderName> PSEUDO_HEADERS;

        private PseudoHeaderName(String value, boolean requestOnly) {
            this.value = AsciiString.cached(value);
            this.requestOnly = requestOnly;
        }

        public AsciiString value() {
            return this.value;
        }

        public static boolean hasPseudoHeaderFormat(CharSequence headerName) {
            if (headerName instanceof AsciiString) {
                AsciiString asciiHeaderName = (AsciiString)headerName;
                return asciiHeaderName.length() > 0 && asciiHeaderName.byteAt(0) == 58;
            }
            return headerName.length() > 0 && headerName.charAt(0) == ':';
        }

        public static boolean isPseudoHeader(CharSequence header) {
            return PSEUDO_HEADERS.contains(header);
        }

        public static PseudoHeaderName getPseudoHeader(CharSequence header) {
            return (PseudoHeaderName)((Object)PSEUDO_HEADERS.get(header));
        }

        public boolean isRequestOnly() {
            return this.requestOnly;
        }

        static {
            PSEUDO_HEADERS = new CharSequenceMap();
            for (PseudoHeaderName pseudoHeader : PseudoHeaderName.values()) {
                PSEUDO_HEADERS.add(pseudoHeader.value(), pseudoHeader);
            }
        }
    }
}

