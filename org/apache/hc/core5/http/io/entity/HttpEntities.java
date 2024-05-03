/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.entity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityTemplate;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.hc.core5.http.io.entity.HttpEntityWrapper;
import org.apache.hc.core5.http.io.entity.PathEntity;
import org.apache.hc.core5.http.io.entity.SerializableEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.io.IOCallback;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.apache.hc.core5.util.Args;

public final class HttpEntities {
    private HttpEntities() {
    }

    public static HttpEntity create(String content, ContentType contentType) {
        return new StringEntity(content, contentType);
    }

    public static HttpEntity create(String content, Charset charset) {
        return new StringEntity(content, ContentType.TEXT_PLAIN.withCharset(charset));
    }

    public static HttpEntity create(String content) {
        return new StringEntity(content, ContentType.TEXT_PLAIN);
    }

    public static HttpEntity create(byte[] content, ContentType contentType) {
        return new ByteArrayEntity(content, contentType);
    }

    public static HttpEntity create(File content, ContentType contentType) {
        return new FileEntity(content, contentType);
    }

    public static HttpEntity create(Serializable serializable, ContentType contentType) {
        return new SerializableEntity(serializable, contentType);
    }

    public static HttpEntity createUrlEncoded(Iterable<? extends NameValuePair> parameters, Charset charset) {
        ContentType contentType = charset != null ? ContentType.APPLICATION_FORM_URLENCODED.withCharset(charset) : ContentType.APPLICATION_FORM_URLENCODED;
        return HttpEntities.create(URLEncodedUtils.format(parameters, contentType.getCharset()), contentType);
    }

    public static HttpEntity create(IOCallback<OutputStream> callback, ContentType contentType) {
        return new EntityTemplate(-1L, contentType, null, callback);
    }

    public static HttpEntity gzip(HttpEntity entity) {
        return new HttpEntityWrapper(entity){

            @Override
            public String getContentEncoding() {
                return "gzip";
            }

            @Override
            public long getContentLength() {
                return -1L;
            }

            @Override
            public InputStream getContent() throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void writeTo(OutputStream outStream) throws IOException {
                Args.notNull(outStream, "Output stream");
                GZIPOutputStream gzip = new GZIPOutputStream(outStream);
                super.writeTo(gzip);
                gzip.close();
            }
        };
    }

    public static HttpEntity createGzipped(String content, ContentType contentType) {
        return HttpEntities.gzip(HttpEntities.create(content, contentType));
    }

    public static HttpEntity createGzipped(String content, Charset charset) {
        return HttpEntities.gzip(HttpEntities.create(content, charset));
    }

    public static HttpEntity createGzipped(String content) {
        return HttpEntities.gzip(HttpEntities.create(content));
    }

    public static HttpEntity createGzipped(byte[] content, ContentType contentType) {
        return HttpEntities.gzip(HttpEntities.create(content, contentType));
    }

    public static HttpEntity createGzipped(File content, ContentType contentType) {
        return HttpEntities.gzip(HttpEntities.create(content, contentType));
    }

    public static HttpEntity createGzipped(Serializable serializable, ContentType contentType) {
        return HttpEntities.gzip(HttpEntities.create(serializable, contentType));
    }

    public static HttpEntity createGzipped(IOCallback<OutputStream> callback, ContentType contentType) {
        return HttpEntities.gzip(HttpEntities.create(callback, contentType));
    }

    public static HttpEntity createGzipped(Path content, ContentType contentType) {
        return HttpEntities.gzip(HttpEntities.create(content, contentType));
    }

    public static HttpEntity withTrailers(HttpEntity entity, final Header ... trailers) {
        return new HttpEntityWrapper(entity){

            @Override
            public boolean isChunked() {
                return true;
            }

            @Override
            public long getContentLength() {
                return -1L;
            }

            @Override
            public Supplier<List<? extends Header>> getTrailers() {
                return new Supplier<List<? extends Header>>(){

                    @Override
                    public List<? extends Header> get() {
                        return Arrays.asList(trailers);
                    }
                };
            }

            @Override
            public Set<String> getTrailerNames() {
                LinkedHashSet<String> names = new LinkedHashSet<String>();
                for (Header trailer : trailers) {
                    names.add(trailer.getName());
                }
                return names;
            }
        };
    }

    public static HttpEntity create(String content, ContentType contentType, Header ... trailers) {
        return HttpEntities.withTrailers(HttpEntities.create(content, contentType), trailers);
    }

    public static HttpEntity create(String content, Charset charset, Header ... trailers) {
        return HttpEntities.withTrailers(HttpEntities.create(content, charset), trailers);
    }

    public static HttpEntity create(String content, Header ... trailers) {
        return HttpEntities.withTrailers(HttpEntities.create(content), trailers);
    }

    public static HttpEntity create(byte[] content, ContentType contentType, Header ... trailers) {
        return HttpEntities.withTrailers(HttpEntities.create(content, contentType), trailers);
    }

    public static HttpEntity create(File content, ContentType contentType, Header ... trailers) {
        return HttpEntities.withTrailers(HttpEntities.create(content, contentType), trailers);
    }

    public static HttpEntity create(Serializable serializable, ContentType contentType, Header ... trailers) {
        return HttpEntities.withTrailers(HttpEntities.create(serializable, contentType), trailers);
    }

    public static HttpEntity create(IOCallback<OutputStream> callback, ContentType contentType, Header ... trailers) {
        return HttpEntities.withTrailers(HttpEntities.create(callback, contentType), trailers);
    }

    public static HttpEntity create(Path content, ContentType contentType) {
        return new PathEntity(content, contentType);
    }

    public static HttpEntity create(Path content, ContentType contentType, Header ... trailers) {
        return HttpEntities.withTrailers(HttpEntities.create(content, contentType), trailers);
    }
}

