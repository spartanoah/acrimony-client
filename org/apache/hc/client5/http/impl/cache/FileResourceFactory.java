/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import org.apache.hc.client5.http.cache.Resource;
import org.apache.hc.client5.http.cache.ResourceFactory;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.cache.BasicIdGenerator;
import org.apache.hc.client5.http.impl.cache.FileResource;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.STATELESS)
public class FileResourceFactory
implements ResourceFactory {
    private final File cacheDir;
    private final BasicIdGenerator idgen;

    public FileResourceFactory(File cacheDir) {
        this.cacheDir = cacheDir;
        this.idgen = new BasicIdGenerator();
    }

    private File generateUniqueCacheFile(String requestId) {
        StringBuilder buffer = new StringBuilder();
        this.idgen.generate(buffer);
        buffer.append('.');
        int len = Math.min(requestId.length(), 100);
        for (int i = 0; i < len; ++i) {
            char ch = requestId.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '.') {
                buffer.append(ch);
                continue;
            }
            buffer.append('-');
        }
        return new File(this.cacheDir, buffer.toString());
    }

    @Override
    public Resource generate(String requestId, byte[] content, int off, int len) throws ResourceIOException {
        Args.notNull(requestId, "Request id");
        File file = this.generateUniqueCacheFile(requestId);
        try (FileOutputStream outStream = new FileOutputStream(file);){
            if (content != null) {
                outStream.write(content, off, len);
            }
        } catch (IOException ex) {
            throw new ResourceIOException(ex.getMessage(), ex);
        }
        return new FileResource(file);
    }

    @Override
    public Resource generate(String requestId, byte[] content) throws ResourceIOException {
        Args.notNull(content, "Content");
        return this.generate(requestId, content, 0, content.length);
    }

    @Override
    public Resource copy(String requestId, Resource resource) throws ResourceIOException {
        File file;
        block76: {
            file = this.generateUniqueCacheFile(requestId);
            try {
                if (resource instanceof FileResource) {
                    try (RandomAccessFile srcFile = new RandomAccessFile(((FileResource)resource).getFile(), "r");
                         RandomAccessFile dstFile = new RandomAccessFile(file, "rw");
                         FileChannel src = srcFile.getChannel();
                         FileChannel dst = dstFile.getChannel();){
                        src.transferTo(0L, srcFile.length(), dst);
                        break block76;
                    }
                }
                try (FileOutputStream out = new FileOutputStream(file);
                     InputStream in = resource.getInputStream();){
                    int len;
                    byte[] buf = new byte[2048];
                    while ((len = in.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                }
            } catch (IOException ex) {
                throw new ResourceIOException(ex.getMessage(), ex);
            }
        }
        return new FileResource(file);
    }
}

