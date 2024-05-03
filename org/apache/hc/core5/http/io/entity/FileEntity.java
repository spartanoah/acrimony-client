/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class FileEntity
extends AbstractHttpEntity {
    private final File file;

    public FileEntity(File file, ContentType contentType, String contentEncoding) {
        super(contentType, contentEncoding);
        this.file = Args.notNull(file, "File");
    }

    public FileEntity(File file, ContentType contentType) {
        super(contentType, null);
        this.file = Args.notNull(file, "File");
    }

    @Override
    public final boolean isRepeatable() {
        return true;
    }

    @Override
    public final long getContentLength() {
        return this.file.length();
    }

    @Override
    public final InputStream getContent() throws IOException {
        return new FileInputStream(this.file);
    }

    @Override
    public final boolean isStreaming() {
        return false;
    }

    @Override
    public final void close() throws IOException {
    }
}

