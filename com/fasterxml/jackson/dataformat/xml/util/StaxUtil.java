/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.codehaus.stax2.typed.Base64Variant
 *  org.codehaus.stax2.typed.Base64Variants
 */
package com.fasterxml.jackson.dataformat.xml.util;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.codehaus.stax2.typed.Base64Variant;
import org.codehaus.stax2.typed.Base64Variants;

public class StaxUtil {
    @Deprecated
    public static <T> T throwXmlAsIOException(XMLStreamException e) throws IOException {
        Throwable t = StaxUtil._unwrap(e);
        throw new IOException(t);
    }

    public static <T> T throwAsParseException(XMLStreamException e, JsonParser p) throws IOException {
        Throwable t = StaxUtil._unwrap(e);
        throw new JsonParseException(p, StaxUtil._message(t, e), t);
    }

    public static <T> T throwAsGenerationException(XMLStreamException e, JsonGenerator g) throws IOException {
        Throwable t = StaxUtil._unwrap(e);
        throw new JsonGenerationException(StaxUtil._message(t, e), t, g);
    }

    private static Throwable _unwrap(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        if (t instanceof Error) {
            throw (Error)t;
        }
        if (t instanceof RuntimeException) {
            throw (RuntimeException)t;
        }
        return t;
    }

    private static String _message(Throwable t1, Throwable t2) {
        String msg = t1.getMessage();
        if (msg == null) {
            msg = t2.getMessage();
        }
        return msg;
    }

    public static String sanitizeXmlTypeName(String name) {
        StringBuilder sb;
        if (name == null) {
            return null;
        }
        int changes = 0;
        if (name.endsWith("[]")) {
            do {
                name = name.substring(0, name.length() - 2);
                ++changes;
            } while (name.endsWith("[]"));
            sb = new StringBuilder(name);
            if (name.endsWith("s")) {
                sb.append("es");
            } else {
                sb.append('s');
            }
        } else {
            sb = new StringBuilder(name);
        }
        int len = name.length();
        for (int i = 0; i < len; ++i) {
            char c = name.charAt(i);
            if (c > '\u007f' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' || c == '.' || c == '-') continue;
            ++changes;
            if (c == '$') {
                sb.setCharAt(i, '.');
                continue;
            }
            sb.setCharAt(i, '_');
        }
        if (changes == 0) {
            return name;
        }
        return sb.toString();
    }

    public static Base64Variant toStax2Base64Variant(com.fasterxml.jackson.core.Base64Variant j64b) {
        return Base64Mapper.instance.map(j64b);
    }

    private static class Base64Mapper {
        public static final Base64Mapper instance = new Base64Mapper();
        private final Map<String, Base64Variant> j2stax2 = new HashMap<String, Base64Variant>();

        private Base64Mapper() {
            this.j2stax2.put(com.fasterxml.jackson.core.Base64Variants.MIME.getName(), Base64Variants.MIME);
            this.j2stax2.put(com.fasterxml.jackson.core.Base64Variants.MIME_NO_LINEFEEDS.getName(), Base64Variants.MIME_NO_LINEFEEDS);
            this.j2stax2.put(com.fasterxml.jackson.core.Base64Variants.MODIFIED_FOR_URL.getName(), Base64Variants.MODIFIED_FOR_URL);
            this.j2stax2.put(com.fasterxml.jackson.core.Base64Variants.PEM.getName(), Base64Variants.PEM);
        }

        public Base64Variant map(com.fasterxml.jackson.core.Base64Variant j64b) {
            Base64Variant result = this.j2stax2.get(j64b.getName());
            if (result == null) {
                result = Base64Variants.getDefaultVariant();
            }
            return result;
        }
    }
}

