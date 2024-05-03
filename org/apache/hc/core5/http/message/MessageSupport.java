/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.FormattedHeader;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpMessage;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.MessageHeaders;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHeaderElementIterator;
import org.apache.hc.core5.http.message.BasicHeaderValueParser;
import org.apache.hc.core5.http.message.BufferedHeader;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.http.message.TokenParser;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;
import org.apache.hc.core5.util.TextUtils;

public class MessageSupport {
    private static final BitSet COMMA = TokenParser.INIT_BITSET(44);

    private MessageSupport() {
    }

    public static void formatTokens(CharArrayBuffer dst, String ... tokens) {
        Args.notNull(dst, "Destination");
        for (int i = 0; i < tokens.length; ++i) {
            String element = tokens[i];
            if (i > 0) {
                dst.append(", ");
            }
            dst.append(element);
        }
    }

    public static void formatTokens(CharArrayBuffer dst, Set<String> tokens) {
        Args.notNull(dst, "Destination");
        if (tokens == null || tokens.isEmpty()) {
            return;
        }
        MessageSupport.formatTokens(dst, tokens.toArray(new String[tokens.size()]));
    }

    public static Header format(String name, Set<String> tokens) {
        Args.notBlank(name, "Header name");
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }
        CharArrayBuffer buffer = new CharArrayBuffer(256);
        buffer.append(name);
        buffer.append(": ");
        MessageSupport.formatTokens(buffer, tokens);
        return BufferedHeader.create(buffer);
    }

    public static Header format(String name, String ... tokens) {
        Args.notBlank(name, "Header name");
        if (tokens == null || tokens.length == 0) {
            return null;
        }
        CharArrayBuffer buffer = new CharArrayBuffer(256);
        buffer.append(name);
        buffer.append(": ");
        MessageSupport.formatTokens(buffer, tokens);
        return BufferedHeader.create(buffer);
    }

    public static Set<String> parseTokens(CharSequence src, ParserCursor cursor) {
        Args.notNull(src, "Source");
        Args.notNull(cursor, "Cursor");
        LinkedHashSet<String> tokens = new LinkedHashSet<String>();
        while (!cursor.atEnd()) {
            String token;
            int pos = cursor.getPos();
            if (src.charAt(pos) == ',') {
                cursor.updatePos(pos + 1);
            }
            if (TextUtils.isBlank(token = TokenParser.INSTANCE.parseToken(src, cursor, COMMA))) continue;
            tokens.add(token);
        }
        return tokens;
    }

    public static Set<String> parseTokens(Header header) {
        Args.notNull(header, "Header");
        if (header instanceof FormattedHeader) {
            CharArrayBuffer buf = ((FormattedHeader)header).getBuffer();
            ParserCursor cursor = new ParserCursor(0, buf.length());
            cursor.updatePos(((FormattedHeader)header).getValuePos());
            return MessageSupport.parseTokens(buf, cursor);
        }
        String value = header.getValue();
        ParserCursor cursor = new ParserCursor(0, value.length());
        return MessageSupport.parseTokens(value, cursor);
    }

    public static void addContentTypeHeader(HttpMessage message, EntityDetails entity) {
        if (entity != null && entity.getContentType() != null && !message.containsHeader("Content-Type")) {
            message.addHeader(new BasicHeader("Content-Type", entity.getContentType()));
        }
    }

    public static void addContentEncodingHeader(HttpMessage message, EntityDetails entity) {
        if (entity != null && entity.getContentEncoding() != null && !message.containsHeader("Content-Encoding")) {
            message.addHeader(new BasicHeader("Content-Encoding", entity.getContentEncoding()));
        }
    }

    public static void addTrailerHeader(HttpMessage message, EntityDetails entity) {
        Set<String> trailerNames;
        if (entity != null && !message.containsHeader("Trailer") && (trailerNames = entity.getTrailerNames()) != null && !trailerNames.isEmpty()) {
            message.setHeader(MessageSupport.format("Trailer", trailerNames));
        }
    }

    public static Iterator<HeaderElement> iterate(MessageHeaders headers, String name) {
        Args.notNull(headers, "Message headers");
        Args.notBlank(name, "Header name");
        return new BasicHeaderElementIterator(headers.headerIterator(name));
    }

    public static HeaderElement[] parse(Header header) {
        Args.notNull(header, "Headers");
        String value = header.getValue();
        if (value == null) {
            return new HeaderElement[0];
        }
        ParserCursor cursor = new ParserCursor(0, value.length());
        return BasicHeaderValueParser.INSTANCE.parseElements(value, cursor);
    }

    public static boolean canResponseHaveBody(String method, HttpResponse response) {
        if (Method.HEAD.isSame(method)) {
            return false;
        }
        int status = response.getCode();
        if (Method.CONNECT.isSame(method) && status == 200) {
            return false;
        }
        return status >= 200 && status != 204 && status != 304;
    }
}

