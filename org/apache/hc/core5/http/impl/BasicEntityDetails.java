/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl;

import java.util.Set;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;

public final class BasicEntityDetails
implements EntityDetails {
    private final long len;
    private final ContentType contentType;

    public BasicEntityDetails(long len, ContentType contentType) {
        this.len = len;
        this.contentType = contentType;
    }

    @Override
    public long getContentLength() {
        return this.len;
    }

    @Override
    public String getContentType() {
        return this.contentType != null ? this.contentType.toString() : null;
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public boolean isChunked() {
        return false;
    }

    @Override
    public Set<String> getTrailerNames() {
        return null;
    }
}

