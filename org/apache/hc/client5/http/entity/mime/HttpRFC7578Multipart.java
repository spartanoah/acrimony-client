/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity.mime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.List;
import org.apache.commons.codec.DecoderException;
import org.apache.hc.client5.http.entity.mime.AbstractMultipartFormat;
import org.apache.hc.client5.http.entity.mime.MimeField;
import org.apache.hc.client5.http.entity.mime.MultipartPart;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.util.ByteArrayBuffer;

class HttpRFC7578Multipart
extends AbstractMultipartFormat {
    private static final PercentCodec PERCENT_CODEC = new PercentCodec();
    private final List<MultipartPart> parts;
    private static final int RADIX = 16;

    public HttpRFC7578Multipart(Charset charset, String boundary, List<MultipartPart> parts) {
        super(charset, boundary);
        this.parts = parts;
    }

    @Override
    public List<MultipartPart> getParts() {
        return this.parts;
    }

    @Override
    protected void formatMultipartHeader(MultipartPart part, OutputStream out) throws IOException {
        for (MimeField field : part.getHeader()) {
            if ("Content-Disposition".equalsIgnoreCase(field.getName())) {
                HttpRFC7578Multipart.writeBytes(field.getName(), this.charset, out);
                HttpRFC7578Multipart.writeBytes(FIELD_SEP, out);
                HttpRFC7578Multipart.writeBytes(field.getValue(), out);
                List<NameValuePair> parameters = field.getParameters();
                for (int i = 0; i < parameters.size(); ++i) {
                    NameValuePair parameter = parameters.get(i);
                    String name = parameter.getName();
                    String value = parameter.getValue();
                    HttpRFC7578Multipart.writeBytes("; ", out);
                    HttpRFC7578Multipart.writeBytes(name, out);
                    HttpRFC7578Multipart.writeBytes("=\"", out);
                    if (value != null) {
                        if (name.equalsIgnoreCase("filename")) {
                            out.write(PERCENT_CODEC.encode(value.getBytes(this.charset)));
                        } else {
                            HttpRFC7578Multipart.writeBytes(value, out);
                        }
                    }
                    HttpRFC7578Multipart.writeBytes("\"", out);
                }
                HttpRFC7578Multipart.writeBytes(CR_LF, out);
                continue;
            }
            HttpRFC7578Multipart.writeField(field, this.charset, out);
        }
    }

    static int digit16(byte b) throws DecoderException {
        int i = Character.digit((char)b, 16);
        if (i == -1) {
            throw new DecoderException("Invalid URL encoding: not a valid digit (radix 16): " + b);
        }
        return i;
    }

    static char hexDigit(int b) {
        return Character.toUpperCase(Character.forDigit(b & 0xF, 16));
    }

    static class PercentCodec {
        private static final byte ESCAPE_CHAR = 37;
        private static final BitSet ALWAYSENCODECHARS = new BitSet();

        PercentCodec() {
        }

        public byte[] encode(byte[] bytes) {
            if (bytes == null) {
                return null;
            }
            CharsetEncoder characterSetEncoder = StandardCharsets.US_ASCII.newEncoder();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            for (int n : bytes) {
                int b = n;
                if (b < 0) {
                    b = 256 + b;
                }
                if (characterSetEncoder.canEncode((char)b) && !ALWAYSENCODECHARS.get(n)) {
                    buffer.write(b);
                    continue;
                }
                buffer.write(37);
                char hex1 = HttpRFC7578Multipart.hexDigit(b >> 4);
                char hex2 = HttpRFC7578Multipart.hexDigit(b);
                buffer.write(hex1);
                buffer.write(hex2);
            }
            return buffer.toByteArray();
        }

        public byte[] decode(byte[] bytes) throws DecoderException {
            if (bytes == null) {
                return null;
            }
            ByteArrayBuffer buffer = new ByteArrayBuffer(bytes.length);
            for (int i = 0; i < bytes.length; ++i) {
                byte b = bytes[i];
                if (b == 37) {
                    try {
                        int u = HttpRFC7578Multipart.digit16(bytes[++i]);
                        int l = HttpRFC7578Multipart.digit16(bytes[++i]);
                        buffer.append((char)((u << 4) + l));
                        continue;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new DecoderException("Invalid URL encoding: ", e);
                    }
                }
                buffer.append(b);
            }
            return buffer.toByteArray();
        }

        static {
            ALWAYSENCODECHARS.set(32);
            ALWAYSENCODECHARS.set(37);
        }
    }
}

