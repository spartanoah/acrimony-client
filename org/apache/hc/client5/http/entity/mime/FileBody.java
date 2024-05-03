/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity.mime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.hc.client5.http.entity.mime.AbstractContentBody;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.util.Args;

public class FileBody
extends AbstractContentBody {
    private final File file;
    private final String filename;

    public FileBody(File file) {
        this(file, ContentType.DEFAULT_BINARY, file != null ? file.getName() : null);
    }

    public FileBody(File file, ContentType contentType, String filename) {
        super(contentType);
        Args.notNull(file, "File");
        this.file = file;
        this.filename = filename == null ? file.getName() : filename;
    }

    public FileBody(File file, ContentType contentType) {
        this(file, contentType, file != null ? file.getName() : null);
    }

    public InputStream getInputStream() throws IOException {
        return new FileInputStream(this.file);
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        Args.notNull(out, "Output stream");
        try (FileInputStream in = new FileInputStream(this.file);){
            int l;
            byte[] tmp = new byte[4096];
            while ((l = ((InputStream)in).read(tmp)) != -1) {
                out.write(tmp, 0, l);
            }
            out.flush();
        }
    }

    @Override
    public long getContentLength() {
        return this.file.length();
    }

    @Override
    public String getFilename() {
        return this.filename;
    }

    public File getFile() {
        return this.file;
    }
}

