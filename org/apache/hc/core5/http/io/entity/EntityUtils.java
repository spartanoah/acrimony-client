/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.io.Closer;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.ByteArrayBuffer;
import org.apache.hc.core5.util.CharArrayBuffer;

public final class EntityUtils {
    private static final int DEFAULT_ENTITY_RETURN_MAX_LENGTH = Integer.MAX_VALUE;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
    private static final int DEFAULT_CHAR_BUFFER_SIZE = 1024;
    private static final int DEFAULT_BYTE_BUFFER_SIZE = 4096;
    private static final Map<String, ContentType> CONTENT_TYPE_MAP;

    private EntityUtils() {
    }

    public static void consumeQuietly(HttpEntity entity) {
        try {
            EntityUtils.consume(entity);
        } catch (IOException iOException) {
            // empty catch block
        }
    }

    public static void consume(HttpEntity entity) throws IOException {
        if (entity == null) {
            return;
        }
        if (entity.isStreaming()) {
            Closer.close(entity.getContent());
        }
    }

    private static int toContentLength(int contentLength) {
        return contentLength < 0 ? 4096 : contentLength;
    }

    public static byte[] toByteArray(HttpEntity entity) throws IOException {
        Args.notNull(entity, "HttpEntity");
        int contentLength = EntityUtils.toContentLength((int)Args.checkContentLength(entity));
        try (InputStream inStream = entity.getContent();){
            int l;
            if (inStream == null) {
                byte[] byArray = null;
                return byArray;
            }
            ByteArrayBuffer buffer = new ByteArrayBuffer(contentLength);
            byte[] tmp = new byte[4096];
            while ((l = inStream.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
            }
            byte[] byArray = buffer.toByteArray();
            return byArray;
        }
    }

    public static byte[] toByteArray(HttpEntity entity, int maxResultLength) throws IOException {
        Args.notNull(entity, "HttpEntity");
        int contentLength = EntityUtils.toContentLength((int)Args.checkContentLength(entity));
        try (InputStream inStream = entity.getContent();){
            int l;
            if (inStream == null) {
                byte[] byArray = null;
                return byArray;
            }
            ByteArrayBuffer buffer = new ByteArrayBuffer(Math.min(maxResultLength, contentLength));
            byte[] tmp = new byte[4096];
            while ((l = inStream.read(tmp, 0, Math.min(4096, buffer.capacity() - buffer.length()))) > 0) {
                buffer.append(tmp, 0, l);
            }
            byte[] byArray = buffer.toByteArray();
            return byArray;
        }
    }

    private static CharArrayBuffer toCharArrayBuffer(InputStream inStream, long contentLength, Charset charset, int maxResultLength) throws IOException {
        int chReadCount;
        Args.notNull(inStream, "InputStream");
        Args.positive(maxResultLength, "maxResultLength");
        Charset actualCharset = charset == null ? DEFAULT_CHARSET : charset;
        CharArrayBuffer buf = new CharArrayBuffer(Math.min(maxResultLength, contentLength > 0L ? (int)contentLength : 1024));
        InputStreamReader reader = new InputStreamReader(inStream, actualCharset);
        char[] tmp = new char[1024];
        while ((chReadCount = reader.read(tmp)) != -1) {
            buf.append(tmp, 0, chReadCount);
        }
        return buf;
    }

    private static String toString(HttpEntity entity, ContentType contentType, int maxResultLength) throws IOException {
        Args.notNull(entity, "HttpEntity");
        int contentLength = EntityUtils.toContentLength((int)Args.checkContentLength(entity));
        try (InputStream inStream = entity.getContent();){
            if (inStream == null) {
                String string = null;
                return string;
            }
            Charset charset = null;
            if (contentType != null && (charset = contentType.getCharset()) == null) {
                ContentType defaultContentType = CONTENT_TYPE_MAP.get(contentType.getMimeType());
                charset = defaultContentType != null ? defaultContentType.getCharset() : null;
            }
            String string = EntityUtils.toCharArrayBuffer(inStream, contentLength, charset, maxResultLength).toString();
            return string;
        }
    }

    public static String toString(HttpEntity entity, Charset defaultCharset) throws IOException, ParseException {
        return EntityUtils.toString(entity, defaultCharset, Integer.MAX_VALUE);
    }

    public static String toString(HttpEntity entity, Charset defaultCharset, int maxResultLength) throws IOException, ParseException {
        ContentType contentType;
        block5: {
            Args.notNull(entity, "HttpEntity");
            contentType = null;
            try {
                contentType = ContentType.parse(entity.getContentType());
            } catch (UnsupportedCharsetException ex) {
                if (defaultCharset != null) break block5;
                throw new UnsupportedEncodingException(ex.getMessage());
            }
        }
        if (contentType != null) {
            if (contentType.getCharset() == null) {
                contentType = contentType.withCharset(defaultCharset);
            }
        } else {
            contentType = ContentType.DEFAULT_TEXT.withCharset(defaultCharset);
        }
        return EntityUtils.toString(entity, contentType, maxResultLength);
    }

    public static String toString(HttpEntity entity, String defaultCharset) throws IOException, ParseException {
        return EntityUtils.toString(entity, defaultCharset, Integer.MAX_VALUE);
    }

    public static String toString(HttpEntity entity, String defaultCharset, int maxResultLength) throws IOException, ParseException {
        return EntityUtils.toString(entity, defaultCharset != null ? Charset.forName(defaultCharset) : null, maxResultLength);
    }

    public static String toString(HttpEntity entity) throws IOException, ParseException {
        return EntityUtils.toString(entity, Integer.MAX_VALUE);
    }

    public static String toString(HttpEntity entity, int maxResultLength) throws IOException, ParseException {
        Args.notNull(entity, "HttpEntity");
        return EntityUtils.toString(entity, ContentType.parse(entity.getContentType()), maxResultLength);
    }

    public static List<NameValuePair> parse(HttpEntity entity) throws IOException {
        return EntityUtils.parse(entity, Integer.MAX_VALUE);
    }

    public static List<NameValuePair> parse(HttpEntity entity, int maxStreamLength) throws IOException {
        CharArrayBuffer buf;
        Args.notNull(entity, "HttpEntity");
        int contentLength = EntityUtils.toContentLength((int)Args.checkContentLength(entity));
        ContentType contentType = ContentType.parse(entity.getContentType());
        if (!ContentType.APPLICATION_FORM_URLENCODED.isSameMimeType(contentType)) {
            return Collections.emptyList();
        }
        Charset charset = contentType.getCharset() != null ? contentType.getCharset() : DEFAULT_CHARSET;
        try (InputStream inStream = entity.getContent();){
            if (inStream == null) {
                List<NameValuePair> list = Collections.emptyList();
                return list;
            }
            buf = EntityUtils.toCharArrayBuffer(inStream, contentLength, charset, maxStreamLength);
        }
        if (buf.isEmpty()) {
            return Collections.emptyList();
        }
        return URLEncodedUtils.parse(buf, charset, '&');
    }

    static {
        ContentType[] contentTypes = new ContentType[]{ContentType.APPLICATION_ATOM_XML, ContentType.APPLICATION_FORM_URLENCODED, ContentType.APPLICATION_JSON, ContentType.APPLICATION_SVG_XML, ContentType.APPLICATION_XHTML_XML, ContentType.APPLICATION_XML, ContentType.MULTIPART_FORM_DATA, ContentType.TEXT_HTML, ContentType.TEXT_PLAIN, ContentType.TEXT_XML};
        HashMap<String, ContentType> map = new HashMap<String, ContentType>();
        for (ContentType contentType : contentTypes) {
            map.put(contentType.getMimeType(), contentType);
        }
        CONTENT_TYPE_MAP = Collections.unmodifiableMap(map);
    }
}

