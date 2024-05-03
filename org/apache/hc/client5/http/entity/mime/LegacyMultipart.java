/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity.mime;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.hc.client5.http.entity.mime.AbstractMultipartFormat;
import org.apache.hc.client5.http.entity.mime.Header;
import org.apache.hc.client5.http.entity.mime.MimeField;
import org.apache.hc.client5.http.entity.mime.MultipartPart;

class LegacyMultipart
extends AbstractMultipartFormat {
    private final List<MultipartPart> parts;

    public LegacyMultipart(Charset charset, String boundary, List<MultipartPart> parts) {
        super(charset, boundary);
        this.parts = parts;
    }

    @Override
    public List<MultipartPart> getParts() {
        return this.parts;
    }

    @Override
    protected void formatMultipartHeader(MultipartPart part, OutputStream out) throws IOException {
        String filename;
        Header header = part.getHeader();
        MimeField cd = header.getField("Content-Disposition");
        if (cd != null) {
            LegacyMultipart.writeField(cd, this.charset, out);
        }
        if ((filename = part.getBody().getFilename()) != null) {
            MimeField ct = header.getField("Content-Type");
            LegacyMultipart.writeField(ct, this.charset, out);
        }
    }
}

