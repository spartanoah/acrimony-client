/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.client.entity;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.DecompressingEntity;

public class GzipDecompressingEntity
extends DecompressingEntity {
    public GzipDecompressingEntity(HttpEntity entity) {
        super(entity);
    }

    InputStream decorate(InputStream wrapped) throws IOException {
        return new GZIPInputStream(wrapped);
    }

    public Header getContentEncoding() {
        return null;
    }

    public long getContentLength() {
        return -1L;
    }
}

