/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity.mime;

import java.io.File;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.hc.client5.http.entity.mime.AbstractMultipartFormat;
import org.apache.hc.client5.http.entity.mime.ByteArrayBody;
import org.apache.hc.client5.http.entity.mime.ContentBody;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.FormBodyPart;
import org.apache.hc.client5.http.entity.mime.FormBodyPartBuilder;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.HttpRFC6532Multipart;
import org.apache.hc.client5.http.entity.mime.HttpRFC7578Multipart;
import org.apache.hc.client5.http.entity.mime.HttpStrictMultipart;
import org.apache.hc.client5.http.entity.mime.InputStreamBody;
import org.apache.hc.client5.http.entity.mime.LegacyMultipart;
import org.apache.hc.client5.http.entity.mime.MultipartFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartPart;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.Args;

public class MultipartEntityBuilder {
    private static final char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private ContentType contentType;
    private HttpMultipartMode mode = HttpMultipartMode.STRICT;
    private String boundary = null;
    private Charset charset = null;
    private List<MultipartPart> multipartParts = null;

    public static MultipartEntityBuilder create() {
        return new MultipartEntityBuilder();
    }

    MultipartEntityBuilder() {
    }

    public MultipartEntityBuilder setMode(HttpMultipartMode mode) {
        this.mode = mode;
        return this;
    }

    public MultipartEntityBuilder setLaxMode() {
        this.mode = HttpMultipartMode.LEGACY;
        return this;
    }

    public MultipartEntityBuilder setStrictMode() {
        this.mode = HttpMultipartMode.STRICT;
        return this;
    }

    public MultipartEntityBuilder setBoundary(String boundary) {
        this.boundary = boundary;
        return this;
    }

    public MultipartEntityBuilder setMimeSubtype(String subType) {
        Args.notBlank(subType, "MIME subtype");
        this.contentType = ContentType.create("multipart/" + subType);
        return this;
    }

    public MultipartEntityBuilder setContentType(ContentType contentType) {
        Args.notNull(contentType, "Content type");
        this.contentType = contentType;
        return this;
    }

    public MultipartEntityBuilder setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public MultipartEntityBuilder addPart(MultipartPart multipartPart) {
        if (multipartPart == null) {
            return this;
        }
        if (this.multipartParts == null) {
            this.multipartParts = new ArrayList<MultipartPart>();
        }
        this.multipartParts.add(multipartPart);
        return this;
    }

    public MultipartEntityBuilder addPart(String name, ContentBody contentBody) {
        Args.notNull(name, "Name");
        Args.notNull(contentBody, "Content body");
        return this.addPart(FormBodyPartBuilder.create(name, contentBody).build());
    }

    public MultipartEntityBuilder addTextBody(String name, String text, ContentType contentType) {
        return this.addPart(name, new StringBody(text, contentType));
    }

    public MultipartEntityBuilder addTextBody(String name, String text) {
        return this.addTextBody(name, text, ContentType.DEFAULT_TEXT);
    }

    public MultipartEntityBuilder addBinaryBody(String name, byte[] b, ContentType contentType, String filename) {
        return this.addPart(name, new ByteArrayBody(b, contentType, filename));
    }

    public MultipartEntityBuilder addBinaryBody(String name, byte[] b) {
        return this.addBinaryBody(name, b, ContentType.DEFAULT_BINARY, null);
    }

    public MultipartEntityBuilder addBinaryBody(String name, File file, ContentType contentType, String filename) {
        return this.addPart(name, new FileBody(file, contentType, filename));
    }

    public MultipartEntityBuilder addBinaryBody(String name, File file) {
        return this.addBinaryBody(name, file, ContentType.DEFAULT_BINARY, file != null ? file.getName() : null);
    }

    public MultipartEntityBuilder addBinaryBody(String name, InputStream stream, ContentType contentType, String filename) {
        return this.addPart(name, new InputStreamBody(stream, contentType, filename));
    }

    public MultipartEntityBuilder addBinaryBody(String name, InputStream stream) {
        return this.addBinaryBody(name, stream, ContentType.DEFAULT_BINARY, null);
    }

    private String generateBoundary() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int count = rand.nextInt(30, 41);
        CharBuffer buffer = CharBuffer.allocate(count);
        while (buffer.hasRemaining()) {
            buffer.put(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        buffer.flip();
        return buffer.toString();
    }

    MultipartFormEntity buildEntity() {
        AbstractMultipartFormat form;
        ContentType contentTypeCopy;
        Charset charsetCopy;
        String boundaryCopy = this.boundary;
        if (boundaryCopy == null && this.contentType != null) {
            boundaryCopy = this.contentType.getParameter("boundary");
        }
        if (boundaryCopy == null) {
            boundaryCopy = this.generateBoundary();
        }
        if ((charsetCopy = this.charset) == null && this.contentType != null) {
            charsetCopy = this.contentType.getCharset();
        }
        ArrayList<BasicNameValuePair> paramsList = new ArrayList<BasicNameValuePair>(2);
        paramsList.add(new BasicNameValuePair("boundary", boundaryCopy));
        if (charsetCopy != null) {
            paramsList.add(new BasicNameValuePair("charset", charsetCopy.name()));
        }
        NameValuePair[] params = paramsList.toArray(new NameValuePair[paramsList.size()]);
        if (this.contentType != null) {
            contentTypeCopy = this.contentType.withParameters(params);
        } else {
            boolean formData = false;
            if (this.multipartParts != null) {
                for (MultipartPart multipartPart : this.multipartParts) {
                    if (!(multipartPart instanceof FormBodyPart)) continue;
                    formData = true;
                    break;
                }
            }
            contentTypeCopy = formData ? ContentType.MULTIPART_FORM_DATA.withParameters(params) : ContentType.create("multipart/mixed", params);
        }
        ArrayList<MultipartPart> multipartPartsCopy = this.multipartParts != null ? new ArrayList<MultipartPart>(this.multipartParts) : Collections.emptyList();
        HttpMultipartMode modeCopy = this.mode != null ? this.mode : HttpMultipartMode.STRICT;
        switch (modeCopy) {
            case LEGACY: {
                form = new LegacyMultipart(charsetCopy, boundaryCopy, multipartPartsCopy);
                break;
            }
            case EXTENDED: {
                if (contentTypeCopy.isSameMimeType(ContentType.MULTIPART_FORM_DATA)) {
                    if (charsetCopy == null) {
                        charsetCopy = StandardCharsets.UTF_8;
                    }
                    form = new HttpRFC7578Multipart(charsetCopy, boundaryCopy, multipartPartsCopy);
                    break;
                }
                form = new HttpRFC6532Multipart(charsetCopy, boundaryCopy, multipartPartsCopy);
                break;
            }
            default: {
                form = new HttpStrictMultipart(StandardCharsets.US_ASCII, boundaryCopy, multipartPartsCopy);
            }
        }
        return new MultipartFormEntity(form, contentTypeCopy, form.getTotalLength());
    }

    public HttpEntity build() {
        return this.buildEntity();
    }
}

