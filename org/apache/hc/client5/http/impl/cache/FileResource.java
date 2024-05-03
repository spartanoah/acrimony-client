/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.hc.client5.http.cache.Resource;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.ByteArrayBuffer;

@Contract(threading=ThreadingBehavior.SAFE)
public class FileResource
extends Resource {
    private static final long serialVersionUID = 4132244415919043397L;
    private final AtomicReference<File> fileRef;
    private final long len;

    public FileResource(File file) {
        Args.notNull(file, "File");
        this.fileRef = new AtomicReference<File>(file);
        this.len = file.length();
    }

    File getFile() {
        return this.fileRef.get();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public byte[] get() throws ResourceIOException {
        File file = this.fileRef.get();
        if (file == null) {
            throw new ResourceIOException("Resouce already dispoased");
        }
        try (FileInputStream in = new FileInputStream(file);){
            int len;
            ByteArrayBuffer buf = new ByteArrayBuffer(1024);
            byte[] tmp = new byte[2048];
            while ((len = ((InputStream)in).read(tmp)) != -1) {
                buf.append(tmp, 0, len);
            }
            byte[] byArray = buf.toByteArray();
            return byArray;
        } catch (IOException ex) {
            throw new ResourceIOException(ex.getMessage(), ex);
        }
    }

    @Override
    public InputStream getInputStream() throws ResourceIOException {
        File file = this.fileRef.get();
        if (file != null) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                throw new ResourceIOException(ex.getMessage(), ex);
            }
        }
        throw new ResourceIOException("Resouce already dispoased");
    }

    @Override
    public long length() {
        return this.len;
    }

    @Override
    public void dispose() {
        File file = this.fileRef.getAndSet(null);
        if (file != null) {
            file.delete();
        }
    }
}

