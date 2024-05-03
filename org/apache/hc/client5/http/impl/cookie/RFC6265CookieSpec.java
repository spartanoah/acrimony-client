/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cookie;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.hc.client5.http.cookie.CommonCookieAttributeHandler;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieAttributeHandler;
import org.apache.hc.client5.http.cookie.CookieOrigin;
import org.apache.hc.client5.http.cookie.CookiePriorityComparator;
import org.apache.hc.client5.http.cookie.CookieSpec;
import org.apache.hc.client5.http.cookie.MalformedCookieException;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.FormattedHeader;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.message.BufferedHeader;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.http.message.TokenParser;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.CharArrayBuffer;

@Contract(threading=ThreadingBehavior.SAFE)
public class RFC6265CookieSpec
implements CookieSpec {
    private static final char PARAM_DELIMITER = ';';
    private static final char COMMA_CHAR = ',';
    private static final char EQUAL_CHAR = '=';
    private static final char DQUOTE_CHAR = '\"';
    private static final char ESCAPE_CHAR = '\\';
    private static final BitSet TOKEN_DELIMS = TokenParser.INIT_BITSET(61, 59);
    private static final BitSet VALUE_DELIMS = TokenParser.INIT_BITSET(59);
    private static final BitSet SPECIAL_CHARS = TokenParser.INIT_BITSET(32, 34, 44, 59, 92);
    private final CookieAttributeHandler[] attribHandlers;
    private final Map<String, CookieAttributeHandler> attribHandlerMap;
    private final TokenParser tokenParser;

    protected RFC6265CookieSpec(CommonCookieAttributeHandler ... handlers) {
        this.attribHandlers = (CookieAttributeHandler[])handlers.clone();
        this.attribHandlerMap = new ConcurrentHashMap<String, CookieAttributeHandler>(handlers.length);
        for (CommonCookieAttributeHandler handler : handlers) {
            this.attribHandlerMap.put(handler.getAttributeName().toLowerCase(Locale.ROOT), handler);
        }
        this.tokenParser = TokenParser.INSTANCE;
    }

    static String getDefaultPath(CookieOrigin origin) {
        String defaultPath = origin.getPath();
        int lastSlashIndex = defaultPath.lastIndexOf(47);
        if (lastSlashIndex >= 0) {
            if (lastSlashIndex == 0) {
                lastSlashIndex = 1;
            }
            defaultPath = defaultPath.substring(0, lastSlashIndex);
        }
        return defaultPath;
    }

    static String getDefaultDomain(CookieOrigin origin) {
        return origin.getHost();
    }

    @Override
    public final List<Cookie> parse(Header header, CookieOrigin origin) throws MalformedCookieException {
        ParserCursor cursor;
        CharArrayBuffer buffer;
        Args.notNull(header, "Header");
        Args.notNull(origin, "Cookie origin");
        if (!header.getName().equalsIgnoreCase("Set-Cookie")) {
            throw new MalformedCookieException("Unrecognized cookie header: '" + header + "'");
        }
        if (header instanceof FormattedHeader) {
            buffer = ((FormattedHeader)header).getBuffer();
            cursor = new ParserCursor(((FormattedHeader)header).getValuePos(), buffer.length());
        } else {
            String s = header.getValue();
            if (s == null) {
                throw new MalformedCookieException("Header value is null");
            }
            buffer = new CharArrayBuffer(s.length());
            buffer.append(s);
            cursor = new ParserCursor(0, buffer.length());
        }
        String name = this.tokenParser.parseToken(buffer, cursor, TOKEN_DELIMS);
        if (name.isEmpty()) {
            return Collections.emptyList();
        }
        if (cursor.atEnd()) {
            return Collections.emptyList();
        }
        char valueDelim = buffer.charAt(cursor.getPos());
        cursor.updatePos(cursor.getPos() + 1);
        if (valueDelim != '=') {
            throw new MalformedCookieException("Cookie value is invalid: '" + header + "'");
        }
        String value = this.tokenParser.parseValue(buffer, cursor, VALUE_DELIMS);
        if (!cursor.atEnd()) {
            cursor.updatePos(cursor.getPos() + 1);
        }
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setPath(RFC6265CookieSpec.getDefaultPath(origin));
        cookie.setDomain(RFC6265CookieSpec.getDefaultDomain(origin));
        cookie.setCreationDate(new Date());
        LinkedHashMap<String, String> attribMap = new LinkedHashMap<String, String>();
        while (!cursor.atEnd()) {
            String paramName = this.tokenParser.parseToken(buffer, cursor, TOKEN_DELIMS).toLowerCase(Locale.ROOT);
            String paramValue = null;
            if (!cursor.atEnd()) {
                char paramDelim = buffer.charAt(cursor.getPos());
                cursor.updatePos(cursor.getPos() + 1);
                if (paramDelim == '=') {
                    paramValue = this.tokenParser.parseToken(buffer, cursor, VALUE_DELIMS);
                    if (!cursor.atEnd()) {
                        cursor.updatePos(cursor.getPos() + 1);
                    }
                }
            }
            cookie.setAttribute(paramName, paramValue);
            attribMap.put(paramName, paramValue);
        }
        if (attribMap.containsKey("max-age")) {
            attribMap.remove("expires");
        }
        for (Map.Entry entry : attribMap.entrySet()) {
            String paramName = (String)entry.getKey();
            String paramValue = (String)entry.getValue();
            CookieAttributeHandler handler = this.attribHandlerMap.get(paramName);
            if (handler == null) continue;
            handler.parse(cookie, paramValue);
        }
        return Collections.singletonList(cookie);
    }

    @Override
    public final void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        for (CookieAttributeHandler handler : this.attribHandlers) {
            handler.validate(cookie, origin);
        }
    }

    @Override
    public final boolean match(Cookie cookie, CookieOrigin origin) {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        for (CookieAttributeHandler handler : this.attribHandlers) {
            if (handler.match(cookie, origin)) continue;
            return false;
        }
        return true;
    }

    @Override
    public List<Header> formatCookies(List<Cookie> cookies) {
        List<Cookie> sortedCookies;
        Args.notEmpty(cookies, "List of cookies");
        if (cookies.size() > 1) {
            sortedCookies = new ArrayList<Cookie>(cookies);
            Collections.sort(sortedCookies, CookiePriorityComparator.INSTANCE);
        } else {
            sortedCookies = cookies;
        }
        CharArrayBuffer buffer = new CharArrayBuffer(20 * sortedCookies.size());
        buffer.append("Cookie");
        buffer.append(": ");
        for (int n = 0; n < sortedCookies.size(); ++n) {
            Cookie cookie = sortedCookies.get(n);
            if (n > 0) {
                buffer.append(';');
                buffer.append(' ');
            }
            buffer.append(cookie.getName());
            String s = cookie.getValue();
            if (s == null) continue;
            buffer.append('=');
            if (this.containsSpecialChar(s)) {
                buffer.append('\"');
                for (int i = 0; i < s.length(); ++i) {
                    char ch = s.charAt(i);
                    if (ch == '\"' || ch == '\\') {
                        buffer.append('\\');
                    }
                    buffer.append(ch);
                }
                buffer.append('\"');
                continue;
            }
            buffer.append(s);
        }
        ArrayList<Header> headers = new ArrayList<Header>(1);
        try {
            headers.add(new BufferedHeader(buffer));
        } catch (ParseException ignore) {
            // empty catch block
        }
        return headers;
    }

    boolean containsSpecialChar(CharSequence s) {
        return this.containsChars(s, SPECIAL_CHARS);
    }

    boolean containsChars(CharSequence s, BitSet chars) {
        for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            if (!chars.get(ch)) continue;
            return true;
        }
        return false;
    }
}

