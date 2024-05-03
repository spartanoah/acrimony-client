/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.io.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.AbstractHttpEntity;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class SerializableEntity
extends AbstractHttpEntity {
    private final Serializable serializable;

    public SerializableEntity(Serializable serializable, ContentType contentType, String contentEncoding) {
        super(contentType, contentEncoding);
        this.serializable = Args.notNull(serializable, "Source object");
    }

    public SerializableEntity(Serializable serializable, ContentType contentType) {
        this(serializable, contentType, null);
    }

    @Override
    public final InputStream getContent() throws IOException, IllegalStateException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        this.writeTo(buf);
        return new ByteArrayInputStream(buf.toByteArray());
    }

    @Override
    public final long getContentLength() {
        return -1L;
    }

    @Override
    public final boolean isRepeatable() {
        return true;
    }

    @Override
    public final boolean isStreaming() {
        return false;
    }

    @Override
    public final void writeTo(OutputStream outStream) throws IOException {
        Args.notNull(outStream, "Output stream");
        ObjectOutputStream out = new ObjectOutputStream(outStream);
        out.writeObject(this.serializable);
        out.flush();
    }

    @Override
    public final void close() throws IOException {
    }
}

