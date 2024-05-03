/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.ssl;

import java.util.BitSet;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.http.message.TokenParser;

final class TlsVersionParser {
    public static final TlsVersionParser INSTANCE = new TlsVersionParser();
    private final TokenParser tokenParser = TokenParser.INSTANCE;

    TlsVersionParser() {
    }

    ProtocolVersion parse(CharSequence buffer, ParserCursor cursor, BitSet delimiters) throws ParseException {
        int minor;
        int major;
        int lowerBound = cursor.getLowerBound();
        int upperBound = cursor.getUpperBound();
        int pos = cursor.getPos();
        if (pos + 4 > cursor.getUpperBound()) {
            throw new ParseException("Invalid TLS protocol version", buffer, lowerBound, upperBound, pos);
        }
        if (buffer.charAt(pos) != 'T' || buffer.charAt(pos + 1) != 'L' || buffer.charAt(pos + 2) != 'S' || buffer.charAt(pos + 3) != 'v') {
            throw new ParseException("Invalid TLS protocol version", buffer, lowerBound, upperBound, pos);
        }
        cursor.updatePos(pos += 4);
        if (cursor.atEnd()) {
            throw new ParseException("Invalid TLS version", buffer, lowerBound, upperBound, pos);
        }
        String s = this.tokenParser.parseToken(buffer, cursor, delimiters);
        int idx = s.indexOf(46);
        if (idx == -1) {
            int major2;
            try {
                major2 = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new ParseException("Invalid TLS major version", buffer, lowerBound, upperBound, pos);
            }
            return new ProtocolVersion("TLS", major2, 0);
        }
        String s1 = s.substring(0, idx);
        try {
            major = Integer.parseInt(s1);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid TLS major version", buffer, lowerBound, upperBound, pos);
        }
        String s2 = s.substring(idx + 1);
        try {
            minor = Integer.parseInt(s2);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid TLS minor version", buffer, lowerBound, upperBound, pos);
        }
        return new ProtocolVersion("TLS", major, minor);
    }

    ProtocolVersion parse(String s) throws ParseException {
        if (s == null) {
            return null;
        }
        ParserCursor cursor = new ParserCursor(0, s.length());
        return this.parse(s, cursor, null);
    }
}

