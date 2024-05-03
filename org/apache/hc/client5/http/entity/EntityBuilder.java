/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import org.apache.hc.client5.http.entity.GzipCompressingEntity;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.io.entity.SerializableEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class EntityBuilder {
    private String text;
    private byte[] binary;
    private InputStream stream;
    private List<NameValuePair> parameters;
    private Serializable serializable;
    private File file;
    private ContentType contentType;
    private String contentEncoding;
    private boolean chunked;
    private boolean gzipCompressed;

    EntityBuilder() {
    }

    public static EntityBuilder create() {
        return new EntityBuilder();
    }

    private void clearContent() {
        this.text = null;
        this.binary = null;
        this.stream = null;
        this.parameters = null;
        this.serializable = null;
        this.file = null;
    }

    public String getText() {
        return this.text;
    }

    public EntityBuilder setText(String text) {
        this.clearContent();
        this.text = text;
        return this;
    }

    public byte[] getBinary() {
        return this.binary;
    }

    public EntityBuilder setBinary(byte[] binary) {
        this.clearContent();
        this.binary = binary;
        return this;
    }

    public InputStream getStream() {
        return this.stream;
    }

    public EntityBuilder setStream(InputStream stream) {
        this.clearContent();
        this.stream = stream;
        return this;
    }

    public List<NameValuePair> getParameters() {
        return this.parameters;
    }

    public EntityBuilder setParameters(List<NameValuePair> parameters) {
        this.clearContent();
        this.parameters = parameters;
        return this;
    }

    public EntityBuilder setParameters(NameValuePair ... parameters) {
        return this.setParameters(Arrays.asList(parameters));
    }

    public Serializable getSerializable() {
        return this.serializable;
    }

    public EntityBuilder setSerializable(Serializable serializable) {
        this.clearContent();
        this.serializable = serializable;
        return this;
    }

    public File getFile() {
        return this.file;
    }

    public EntityBuilder setFile(File file) {
        this.clearContent();
        this.file = file;
        return this;
    }

    public ContentType getContentType() {
        return this.contentType;
    }

    public EntityBuilder setContentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getContentEncoding() {
        return this.contentEncoding;
    }

    public EntityBuilder setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    public boolean isChunked() {
        return this.chunked;
    }

    public EntityBuilder chunked() {
        this.chunked = true;
        return this;
    }

    public boolean isGzipCompressed() {
        return this.gzipCompressed;
    }

    public EntityBuilder gzipCompressed() {
        this.gzipCompressed = true;
        return this;
    }

    private ContentType getContentOrDefault(ContentType def) {
        return this.contentType != null ? this.contentType : def;
    }

    public HttpEntity build() {
        AbstractHttpEntity e;
        if (this.text != null) {
            e = new StringEntity(this.text, this.getContentOrDefault(ContentType.DEFAULT_TEXT), this.contentEncoding, this.chunked);
        } else if (this.binary != null) {
            e = new ByteArrayEntity(this.binary, this.getContentOrDefault(ContentType.DEFAULT_BINARY), this.contentEncoding, this.chunked);
        } else if (this.stream != null) {
            e = new InputStreamEntity(this.stream, -1L, this.getContentOrDefault(ContentType.DEFAULT_BINARY), this.contentEncoding);
        } else if (this.parameters != null) {
            e = new UrlEncodedFormEntity(this.parameters, this.contentType != null ? this.contentType.getCharset() : null);
        } else if (this.serializable != null) {
            e = new SerializableEntity(this.serializable, ContentType.DEFAULT_BINARY, this.contentEncoding);
        } else if (this.file != null) {
            e = new FileEntity(this.file, this.getContentOrDefault(ContentType.DEFAULT_BINARY), this.contentEncoding);
        } else {
            throw new IllegalStateException("No entity set");
        }
        if (this.gzipCompressed) {
            return new GzipCompressingEntity(e);
        }
        return e;
    }
}

