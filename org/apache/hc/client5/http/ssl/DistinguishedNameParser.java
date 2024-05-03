/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.ssl;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.http.message.TokenParser;
import org.apache.hc.core5.util.CharArrayBuffer;

final class DistinguishedNameParser {
    public static final DistinguishedNameParser INSTANCE = new DistinguishedNameParser();
    private static final BitSet EQUAL_OR_COMMA_OR_PLUS = TokenParser.INIT_BITSET(61, 44, 43);
    private static final BitSet COMMA_OR_PLUS = TokenParser.INIT_BITSET(44, 43);
    private final TokenParser tokenParser = new InternalTokenParser();

    DistinguishedNameParser() {
    }

    private String parseToken(CharArrayBuffer buf, ParserCursor cursor, BitSet delimiters) {
        return this.tokenParser.parseToken(buf, cursor, delimiters);
    }

    private String parseValue(CharArrayBuffer buf, ParserCursor cursor, BitSet delimiters) {
        return this.tokenParser.parseValue(buf, cursor, delimiters);
    }

    private NameValuePair parseParameter(CharArrayBuffer buf, ParserCursor cursor) {
        String name = this.parseToken(buf, cursor, EQUAL_OR_COMMA_OR_PLUS);
        if (cursor.atEnd()) {
            return new BasicNameValuePair(name, null);
        }
        char delim = buf.charAt(cursor.getPos());
        cursor.updatePos(cursor.getPos() + 1);
        if (delim == ',') {
            return new BasicNameValuePair(name, null);
        }
        String value = this.parseValue(buf, cursor, COMMA_OR_PLUS);
        if (!cursor.atEnd()) {
            cursor.updatePos(cursor.getPos() + 1);
        }
        return new BasicNameValuePair(name, value);
    }

    List<NameValuePair> parse(CharArrayBuffer buf, ParserCursor cursor) {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        this.tokenParser.skipWhiteSpace(buf, cursor);
        while (!cursor.atEnd()) {
            NameValuePair param = this.parseParameter(buf, cursor);
            params.add(param);
        }
        return params;
    }

    List<NameValuePair> parse(String s) {
        if (s == null) {
            return null;
        }
        CharArrayBuffer buffer = new CharArrayBuffer(s.length());
        buffer.append(s);
        ParserCursor cursor = new ParserCursor(0, s.length());
        return this.parse(buffer, cursor);
    }

    static class InternalTokenParser
    extends TokenParser {
        InternalTokenParser() {
        }

        @Override
        public void copyUnquotedContent(CharSequence buf, ParserCursor cursor, BitSet delimiters, StringBuilder dst) {
            int pos = cursor.getPos();
            int indexFrom = cursor.getPos();
            int indexTo = cursor.getUpperBound();
            boolean escaped = false;
            int i = indexFrom;
            while (i < indexTo) {
                char current = buf.charAt(i);
                if (escaped) {
                    dst.append(current);
                    escaped = false;
                } else {
                    if (delimiters != null && delimiters.get(current) || TokenParser.isWhitespace(current) || current == '\"') break;
                    if (current == '\\') {
                        escaped = true;
                    } else {
                        dst.append(current);
                    }
                }
                ++i;
                ++pos;
            }
            cursor.updatePos(pos);
        }
    }
}

