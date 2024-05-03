/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.net;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.message.ParserCursor;
import org.apache.hc.core5.http.message.TokenParser;
import org.apache.hc.core5.util.Args;

public class URLEncodedUtils {
    private static final char QP_SEP_A = '&';
    private static final char QP_SEP_S = ';';
    private static final String NAME_VALUE_SEPARATOR = "=";
    private static final char PATH_SEPARATOR = '/';
    private static final BitSet PATH_SEPARATORS;
    private static final BitSet UNRESERVED;
    private static final BitSet PUNCT;
    private static final BitSet USERINFO;
    private static final BitSet PATHSAFE;
    private static final BitSet URIC;
    private static final BitSet RESERVED;
    private static final BitSet URLENCODER;
    private static final BitSet PATH_SPECIAL;
    private static final int RADIX = 16;

    public static List<NameValuePair> parse(URI uri, Charset charset) {
        Args.notNull(uri, "URI");
        String query = uri.getRawQuery();
        if (query != null && !query.isEmpty()) {
            return URLEncodedUtils.parse(query, charset);
        }
        return URLEncodedUtils.createEmptyList();
    }

    public static List<NameValuePair> parse(CharSequence s, Charset charset) {
        if (s == null) {
            return URLEncodedUtils.createEmptyList();
        }
        return URLEncodedUtils.parse(s, charset, '&', ';');
    }

    public static List<NameValuePair> parse(CharSequence s, Charset charset, char ... separators) {
        Args.notNull(s, "Char sequence");
        TokenParser tokenParser = TokenParser.INSTANCE;
        BitSet delimSet = new BitSet();
        for (char separator : separators) {
            delimSet.set(separator);
        }
        ParserCursor cursor = new ParserCursor(0, s.length());
        ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
        while (!cursor.atEnd()) {
            delimSet.set(61);
            String name = tokenParser.parseToken(s, cursor, delimSet);
            String value = null;
            if (!cursor.atEnd()) {
                char delim = s.charAt(cursor.getPos());
                cursor.updatePos(cursor.getPos() + 1);
                if (delim == '=') {
                    delimSet.clear(61);
                    value = tokenParser.parseToken(s, cursor, delimSet);
                    if (!cursor.atEnd()) {
                        cursor.updatePos(cursor.getPos() + 1);
                    }
                }
            }
            if (name.isEmpty()) continue;
            list.add(new BasicNameValuePair(URLEncodedUtils.decodeFormFields(name, charset), URLEncodedUtils.decodeFormFields(value, charset)));
        }
        return list;
    }

    static List<String> splitSegments(CharSequence s, BitSet separators) {
        ParserCursor cursor = new ParserCursor(0, s.length());
        if (cursor.atEnd()) {
            return Collections.emptyList();
        }
        if (separators.get(s.charAt(cursor.getPos()))) {
            cursor.updatePos(cursor.getPos() + 1);
        }
        ArrayList<String> list = new ArrayList<String>();
        StringBuilder buf = new StringBuilder();
        while (true) {
            if (cursor.atEnd()) break;
            char current = s.charAt(cursor.getPos());
            if (separators.get(current)) {
                list.add(buf.toString());
                buf.setLength(0);
            } else {
                buf.append(current);
            }
            cursor.updatePos(cursor.getPos() + 1);
        }
        list.add(buf.toString());
        return list;
    }

    static List<String> splitPathSegments(CharSequence s) {
        return URLEncodedUtils.splitSegments(s, PATH_SEPARATORS);
    }

    public static List<String> parsePathSegments(CharSequence s, Charset charset) {
        Args.notNull(s, "Char sequence");
        List<String> list = URLEncodedUtils.splitPathSegments(s);
        for (int i = 0; i < list.size(); ++i) {
            list.set(i, URLEncodedUtils.urlDecode(list.get(i), charset != null ? charset : StandardCharsets.UTF_8, false));
        }
        return list;
    }

    public static List<String> parsePathSegments(CharSequence s) {
        return URLEncodedUtils.parsePathSegments(s, StandardCharsets.UTF_8);
    }

    static void formatSegments(StringBuilder buf, Iterable<String> segments, Charset charset) {
        for (String segment : segments) {
            buf.append('/');
            URLEncodedUtils.urlEncode(buf, segment, charset, PATHSAFE, false);
        }
    }

    public static String formatSegments(Iterable<String> segments, Charset charset) {
        Args.notNull(segments, "Segments");
        StringBuilder buf = new StringBuilder();
        URLEncodedUtils.formatSegments(buf, segments, charset);
        return buf.toString();
    }

    public static String formatSegments(String ... segments) {
        return URLEncodedUtils.formatSegments(Arrays.asList(segments), StandardCharsets.UTF_8);
    }

    static void formatNameValuePairs(StringBuilder buf, Iterable<? extends NameValuePair> parameters, char parameterSeparator, Charset charset) {
        int i = 0;
        for (NameValuePair nameValuePair : parameters) {
            if (i > 0) {
                buf.append(parameterSeparator);
            }
            URLEncodedUtils.encodeFormFields(buf, nameValuePair.getName(), charset);
            if (nameValuePair.getValue() != null) {
                buf.append(NAME_VALUE_SEPARATOR);
                URLEncodedUtils.encodeFormFields(buf, nameValuePair.getValue(), charset);
            }
            ++i;
        }
    }

    static void formatParameters(StringBuilder buf, Iterable<? extends NameValuePair> parameters, Charset charset) {
        URLEncodedUtils.formatNameValuePairs(buf, parameters, '&', charset);
    }

    public static String format(Iterable<? extends NameValuePair> parameters, char parameterSeparator, Charset charset) {
        Args.notNull(parameters, "Parameters");
        StringBuilder buf = new StringBuilder();
        URLEncodedUtils.formatNameValuePairs(buf, parameters, parameterSeparator, charset);
        return buf.toString();
    }

    public static String format(Iterable<? extends NameValuePair> parameters, Charset charset) {
        return URLEncodedUtils.format(parameters, '&', charset);
    }

    private static List<NameValuePair> createEmptyList() {
        return new ArrayList<NameValuePair>(0);
    }

    private static void urlEncode(StringBuilder buf, String content, Charset charset, BitSet safechars, boolean blankAsPlus) {
        if (content == null) {
            return;
        }
        ByteBuffer bb = charset.encode(content);
        while (bb.hasRemaining()) {
            int b = bb.get() & 0xFF;
            if (safechars.get(b)) {
                buf.append((char)b);
                continue;
            }
            if (blankAsPlus && b == 32) {
                buf.append('+');
                continue;
            }
            buf.append("%");
            char hex1 = Character.toUpperCase(Character.forDigit(b >> 4 & 0xF, 16));
            char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
            buf.append(hex1);
            buf.append(hex2);
        }
    }

    private static String urlDecode(String content, Charset charset, boolean plusAsBlank) {
        if (content == null) {
            return null;
        }
        ByteBuffer bb = ByteBuffer.allocate(content.length());
        CharBuffer cb = CharBuffer.wrap(content);
        while (cb.hasRemaining()) {
            char c = cb.get();
            if (c == '%' && cb.remaining() >= 2) {
                char uc = cb.get();
                char lc = cb.get();
                int u = Character.digit(uc, 16);
                int l = Character.digit(lc, 16);
                if (u != -1 && l != -1) {
                    bb.put((byte)((u << 4) + l));
                    continue;
                }
                bb.put((byte)37);
                bb.put((byte)uc);
                bb.put((byte)lc);
                continue;
            }
            if (plusAsBlank && c == '+') {
                bb.put((byte)32);
                continue;
            }
            bb.put((byte)c);
        }
        bb.flip();
        return charset.decode(bb).toString();
    }

    static String decodeFormFields(String content, Charset charset) {
        if (content == null) {
            return null;
        }
        return URLEncodedUtils.urlDecode(content, charset != null ? charset : StandardCharsets.UTF_8, true);
    }

    static void encodeFormFields(StringBuilder buf, String content, Charset charset) {
        if (content == null) {
            return;
        }
        URLEncodedUtils.urlEncode(buf, content, charset != null ? charset : StandardCharsets.UTF_8, URLENCODER, true);
    }

    static void encUserInfo(StringBuilder buf, String content, Charset charset) {
        URLEncodedUtils.urlEncode(buf, content, charset != null ? charset : StandardCharsets.UTF_8, USERINFO, false);
    }

    static void encUric(StringBuilder buf, String content, Charset charset) {
        URLEncodedUtils.urlEncode(buf, content, charset != null ? charset : StandardCharsets.UTF_8, URIC, false);
    }

    static {
        int i;
        PATH_SEPARATORS = new BitSet(256);
        PATH_SEPARATORS.set(47);
        UNRESERVED = new BitSet(256);
        PUNCT = new BitSet(256);
        USERINFO = new BitSet(256);
        PATHSAFE = new BitSet(256);
        URIC = new BitSet(256);
        RESERVED = new BitSet(256);
        URLENCODER = new BitSet(256);
        PATH_SPECIAL = new BitSet(256);
        for (i = 97; i <= 122; ++i) {
            UNRESERVED.set(i);
        }
        for (i = 65; i <= 90; ++i) {
            UNRESERVED.set(i);
        }
        for (i = 48; i <= 57; ++i) {
            UNRESERVED.set(i);
        }
        UNRESERVED.set(95);
        UNRESERVED.set(45);
        UNRESERVED.set(46);
        UNRESERVED.set(42);
        URLENCODER.or(UNRESERVED);
        UNRESERVED.set(33);
        UNRESERVED.set(126);
        UNRESERVED.set(39);
        UNRESERVED.set(40);
        UNRESERVED.set(41);
        PUNCT.set(44);
        PUNCT.set(59);
        PUNCT.set(58);
        PUNCT.set(36);
        PUNCT.set(38);
        PUNCT.set(43);
        PUNCT.set(61);
        USERINFO.or(UNRESERVED);
        USERINFO.or(PUNCT);
        PATHSAFE.or(UNRESERVED);
        PATHSAFE.set(59);
        PATHSAFE.set(58);
        PATHSAFE.set(64);
        PATHSAFE.set(38);
        PATHSAFE.set(61);
        PATHSAFE.set(43);
        PATHSAFE.set(36);
        PATHSAFE.set(44);
        PATH_SPECIAL.or(PATHSAFE);
        PATH_SPECIAL.set(47);
        RESERVED.set(59);
        RESERVED.set(47);
        RESERVED.set(63);
        RESERVED.set(58);
        RESERVED.set(64);
        RESERVED.set(38);
        RESERVED.set(61);
        RESERVED.set(43);
        RESERVED.set(36);
        RESERVED.set(44);
        RESERVED.set(91);
        RESERVED.set(93);
        URIC.or(RESERVED);
        URIC.or(UNRESERVED);
    }
}

