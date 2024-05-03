/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.reflections.vfs;

import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.reflections.Reflections;
import org.reflections.vfs.Vfs;
import org.reflections.vfs.ZipFile;

public class ZipDir
implements Vfs.Dir {
    final java.util.zip.ZipFile jarFile;

    public ZipDir(JarFile jarFile) {
        this.jarFile = jarFile;
    }

    @Override
    public String getPath() {
        return this.jarFile != null ? this.jarFile.getName().replace("\\", "/") : "/NO-SUCH-DIRECTORY/";
    }

    @Override
    public Iterable<Vfs.File> getFiles() {
        return () -> this.jarFile.stream().filter(entry -> !entry.isDirectory()).map(entry -> new ZipFile(this, (ZipEntry)entry)).iterator();
    }

    @Override
    public void close() {
        block2: {
            try {
                this.jarFile.close();
            } catch (IOException e) {
                if (Reflections.log == null) break block2;
                Reflections.log.warn("Could not close JarFile", e);
            }
        }
    }

    public String toString() {
        return this.jarFile.getName();
    }
}

