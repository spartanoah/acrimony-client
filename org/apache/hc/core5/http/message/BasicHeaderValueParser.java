/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.util.ArrayList;
import java.util.BitSet;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicHeaderElement;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.message.HeaderValueParser;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.http.message.TokenParser;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public class BasicHeaderValueParser
implements HeaderValueParser {
    public static final BasicHeaderValueParser INSTANCE = new BasicHeaderValueParser();
    private static final char PARAM_DELIMITER = ';';
    private static final char ELEM_DELIMITER = ',';
    private static final BitSet TOKEN_DELIMS = TokenParser.INIT_BITSET(61, 59, 44);
    private static final BitSet VALUE_DELIMS = TokenParser.INIT_BITSET(59, 44);
    private final TokenParser tokenParser = TokenParser.INSTANCE;

    @Override
    public HeaderElement[] parseElements(CharSequence buffer, ParserCursor cursor) {
        Args.notNull(buffer, "Char sequence");
        Args.notNull(cursor, "Parser cursor");
        ArrayList<HeaderElement> elements = new ArrayList<HeaderElement>();
        while (!cursor.atEnd()) {
            HeaderElement element = this.parseHeaderElement(buffer, cursor);
            if (element.getName().isEmpty() && element.getValue() == null) continue;
            elements.add(element);
        }
        return elements.toArray(new HeaderElement[elements.size()]);
    }

    @Override
    public HeaderElement parseHeaderElement(CharSequence buffer, ParserCursor cursor) {
        char ch;
        Args.notNull(buffer, "Char sequence");
        Args.notNull(cursor, "Parser cursor");
        NameValuePair nvp = this.parseNameValuePair(buffer, cursor);
        NameValuePair[] params = null;
        if (!cursor.atEnd() && (ch = buffer.charAt(cursor.getPos() - 1)) != ',') {
            params = this.parseParameters(buffer, cursor);
        }
        return new BasicHeaderElement(nvp.getName(), nvp.getValue(), params);
    }

    @Override
    public NameValuePair[] parseParameters(CharSequence buffer, ParserCursor cursor) {
        Args.notNull(buffer, "Char sequence");
        Args.notNull(cursor, "Parser cursor");
        this.tokenParser.skipWhiteSpace(buffer, cursor);
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        while (!cursor.atEnd()) {
            NameValuePair param = this.parseNameValuePair(buffer, cursor);
            params.add(param);
            char ch = buffer.charAt(cursor.getPos() - 1);
            if (ch != ',') continue;
            break;
        }
        return params.toArray(new NameValuePair[params.size()]);
    }

    @Override
    public NameValuePair parseNameValuePair(CharSequence buffer, ParserCursor cursor) {
        Args.notNull(buffer, "Char sequence");
        Args.notNull(cursor, "Parser cursor");
        String name = this.tokenParser.parseToken(buffer, cursor, TOKEN_DELIMS);
        if (cursor.atEnd()) {
            return new BasicNameValuePair(name, null);
        }
        char delim = buffer.charAt(cursor.getPos());
        cursor.updatePos(cursor.getPos() + 1);
        if (delim != '=') {
            return new BasicNameValuePair(name, null);
        }
        String value = this.tokenParser.parseValue(buffer, cursor, VALUE_DELIMS);
        if (!cursor.atEnd()) {
            cursor.updatePos(cursor.getPos() + 1);
        }
        return new BasicNameValuePair(name, value);
    }
}

