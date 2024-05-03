/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.util.BitSet;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.LineParser;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.http.message.RequestLine;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.message.TokenParser;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;
import org.apache.hc.core5.util.TextUtils;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class BasicLineParser
implements LineParser {
    public static final BasicLineParser INSTANCE = new BasicLineParser();
    private static final BitSet FULL_STOP = TokenParser.INIT_BITSET(46);
    private static final BitSet BLANKS = TokenParser.INIT_BITSET(32, 9);
    private static final BitSet COLON = TokenParser.INIT_BITSET(58);
    private final ProtocolVersion protocol;
    private final TokenParser tokenParser;

    public BasicLineParser(ProtocolVersion proto) {
        this.protocol = proto != null ? proto : HttpVersion.HTTP_1_1;
        this.tokenParser = TokenParser.INSTANCE;
    }

    public BasicLineParser() {
        this(null);
    }

    ProtocolVersion parseProtocolVersion(CharArrayBuffer buffer, ParserCursor cursor) throws ParseException {
        int minor;
        int major;
        String protoname = this.protocol.getProtocol();
        int protolength = protoname.length();
        this.tokenParser.skipWhiteSpace(buffer, cursor);
        int pos = cursor.getPos();
        if (pos + protolength + 4 > cursor.getUpperBound()) {
            throw new ParseException("Invalid protocol version", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());
        }
        boolean ok = true;
        for (int i = 0; ok && i < protolength; ++i) {
            ok = buffer.charAt(pos + i) == protoname.charAt(i);
        }
        if (ok) {
            boolean bl = ok = buffer.charAt(pos + protolength) == '/';
        }
        if (!ok) {
            throw new ParseException("Invalid protocol version", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());
        }
        cursor.updatePos(pos + protolength + 1);
        String token1 = this.tokenParser.parseToken(buffer, cursor, FULL_STOP);
        try {
            major = Integer.parseInt(token1);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid protocol major version number", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());
        }
        if (cursor.atEnd()) {
            throw new ParseException("Invalid protocol version", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());
        }
        cursor.updatePos(cursor.getPos() + 1);
        String token2 = this.tokenParser.parseToken(buffer, cursor, BLANKS);
        try {
            minor = Integer.parseInt(token2);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid protocol minor version number", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());
        }
        return HttpVersion.get(major, minor);
    }

    @Override
    public RequestLine parseRequestLine(CharArrayBuffer buffer) throws ParseException {
        Args.notNull(buffer, "Char array buffer");
        ParserCursor cursor = new ParserCursor(0, buffer.length());
        this.tokenParser.skipWhiteSpace(buffer, cursor);
        String method = this.tokenParser.parseToken(buffer, cursor, BLANKS);
        if (TextUtils.isEmpty(method)) {
            throw new ParseException("Invalid request line", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());
        }
        this.tokenParser.skipWhiteSpace(buffer, cursor);
        String uri = this.tokenParser.parseToken(buffer, cursor, BLANKS);
        if (TextUtils.isEmpty(uri)) {
            throw new ParseException("Invalid request line", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());
        }
        ProtocolVersion ver = this.parseProtocolVersion(buffer, cursor);
        this.tokenParser.skipWhiteSpace(buffer, cursor);
        if (!cursor.atEnd()) {
            throw new ParseException("Invalid request line", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());
        }
        return new RequestLine(method, uri, ver);
    }

    @Override
    public StatusLine parseStatusLine(CharArrayBuffer buffer) throws ParseException {
        int statusCode;
        Args.notNull(buffer, "Char array buffer");
        ParserCursor cursor = new ParserCursor(0, buffer.length());
        this.tokenParser.skipWhiteSpace(buffer, cursor);
        ProtocolVersion ver = this.parseProtocolVersion(buffer, cursor);
        this.tokenParser.skipWhiteSpace(buffer, cursor);
        String s = this.tokenParser.parseToken(buffer, cursor, BLANKS);
        for (int i = 0; i < s.length(); ++i) {
            if (Character.isDigit(s.charAt(i))) continue;
            throw new ParseException("Status line contains invalid status code", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());
        }
        try {
            statusCode = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new ParseException("Status line contains invalid status code", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());
        }
        String text = buffer.substringTrimmed(cursor.getPos(), cursor.getUpperBound());
        return new StatusLine(ver, statusCode, text);
    }

    @Override
    public Header parseHeader(CharArrayBuffer buffer) throws ParseException {
        Args.notNull(buffer, "Char array buffer");
        ParserCursor cursor = new ParserCursor(0, buffer.length());
        this.tokenParser.skipWhiteSpace(buffer, cursor);
        String name = this.tokenParser.parseToken(buffer, cursor, COLON);
        if (cursor.getPos() == cursor.getLowerBound() || cursor.getPos() == cursor.getUpperBound() || buffer.charAt(cursor.getPos()) != ':' || TextUtils.isEmpty(name) || TokenParser.isWhitespace(buffer.charAt(cursor.getPos() - 1))) {
            throw new ParseException("Invalid header", buffer, cursor.getLowerBound(), cursor.getUpperBound(), cursor.getPos());
        }
        String value = buffer.substringTrimmed(cursor.getPos() + 1, cursor.getUpperBound());
        return new BasicHeader(name, value);
    }
}

