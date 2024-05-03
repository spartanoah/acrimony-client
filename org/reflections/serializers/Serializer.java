/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.reflections.serializers;

import java.io.File;
import java.io.InputStream;
import org.reflections.Reflections;

public interface Serializer {
    public Reflections read(InputStream var1);

    public File save(Reflections var1, String var2);

    public static File prepareFile(String filename) {
        File file = new File(filename);
        File parent = file.getAbsoluteFile().getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        return file;
    }
}

