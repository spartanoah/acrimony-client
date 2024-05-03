/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.entity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class PathEntity
extends AbstractHttpEntity {
    private final Path path;

    public PathEntity(Path path, ContentType contentType, String contentEncoding) {
        super(contentType, contentEncoding);
        this.path = Args.notNull(path, "Path");
    }

    public PathEntity(Path path, ContentType contentType) {
        super(contentType, null);
        this.path = Args.notNull(path, "Path");
    }

    @Override
    public final boolean isRepeatable() {
        return true;
    }

    @Override
    public final long getContentLength() {
        try {
            return Files.size(this.path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public final InputStream getContent() throws IOException {
        return Files.newInputStream(this.path, new OpenOption[0]);
    }

    @Override
    public final boolean isStreaming() {
        return false;
    }

    @Override
    public final void close() throws IOException {
    }
}

