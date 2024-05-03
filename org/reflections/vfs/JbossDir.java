/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.jboss.vfs.VirtualFile
 */
package org.reflections.vfs;

import java.net.URL;
import java.util.Iterator;
import java.util.Stack;
import java.util.jar.JarFile;
import org.jboss.vfs.VirtualFile;
import org.reflections.vfs.JbossFile;
import org.reflections.vfs.Vfs;
import org.reflections.vfs.ZipDir;

public class JbossDir
implements Vfs.Dir {
    private final VirtualFile virtualFile;

    private JbossDir(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
    }

    public static Vfs.Dir createDir(URL url) throws Exception {
        VirtualFile virtualFile = (VirtualFile)url.openConnection().getContent();
        if (virtualFile.isFile()) {
            return new ZipDir(new JarFile(virtualFile.getPhysicalFile()));
        }
        return new JbossDir(virtualFile);
    }

    @Override
    public String getPath() {
        return this.virtualFile.getPathName();
    }

    @Override
    public Iterable<Vfs.File> getFiles() {
        return () -> new Iterator<Vfs.File>(){
            Vfs.File entry = null;
            final Stack stack = new Stack();
            {
                this.stack.addAll(JbossDir.this.virtualFile.getChildren());
            }

            @Override
            public boolean hasNext() {
                return this.entry != null || (this.entry = this.computeNext()) != null;
            }

            @Override
            public Vfs.File next() {
                Vfs.File next = this.entry;
                this.entry = null;
                return next;
            }

            private Vfs.File computeNext() {
                while (!this.stack.isEmpty()) {
                    VirtualFile file = (VirtualFile)this.stack.pop();
                    if (file.isDirectory()) {
                        this.stack.addAll(file.getChildren());
                        continue;
                    }
                    return new JbossFile(JbossDir.this, file);
                }
                return null;
            }
        };
    }
}

