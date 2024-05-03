/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity.mime;

public interface ContentDescriptor {
    public String getMimeType();

    public String getMediaType();

    public String getSubType();

    public String getCharset();

    public long getContentLength();
}

