/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.reflections.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import org.reflections.vfs.JarInputDir;
import org.reflections.vfs.Vfs;

public class JarInputFile
implements Vfs.File {
    private final ZipEntry entry;
    private final JarInputDir jarInputDir;
    private final long fromIndex;
    private final long endIndex;

    public JarInputFile(ZipEntry entry, JarInputDir jarInputDir, long cursor, long nextCursor) {
        this.entry = entry;
        this.jarInputDir = jarInputDir;
        this.fromIndex = cursor;
        this.endIndex = nextCursor;
    }

    @Override
    public String getName() {
        String name = this.entry.getName();
        return name.substring(name.lastIndexOf("/") + 1);
    }

    @Override
    public String getRelativePath() {
        return this.entry.getName();
    }

    @Override
    public InputStream openInputStream() {
        return new InputStream(){

            @Override
            public int read() throws IOException {
                if (((JarInputFile)JarInputFile.this).jarInputDir.cursor >= JarInputFile.this.fromIndex && ((JarInputFile)JarInputFile.this).jarInputDir.cursor <= JarInputFile.this.endIndex) {
                    int read = ((JarInputFile)JarInputFile.this).jarInputDir.jarInputStream.read();
                    ++((JarInputFile)JarInputFile.this).jarInputDir.cursor;
                    return read;
                }
                return -1;
            }
        };
    }
}

