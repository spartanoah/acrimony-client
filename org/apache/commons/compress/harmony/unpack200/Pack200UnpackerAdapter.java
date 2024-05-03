/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarOutputStream;
import org.apache.commons.compress.harmony.pack200.Pack200Adapter;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.Archive;
import org.apache.commons.compress.java.util.jar.Pack200;

public class Pack200UnpackerAdapter
extends Pack200Adapter
implements Pack200.Unpacker {
    @Override
    public void unpack(InputStream in, JarOutputStream out) throws IOException {
        if (in == null || out == null) {
            throw new IllegalArgumentException("Must specify both input and output streams");
        }
        this.completed(0.0);
        try {
            new Archive(in, out).unpack();
        } catch (Pack200Exception e) {
            throw new IOException("Failed to unpack Jar:" + String.valueOf(e));
        }
        this.completed(1.0);
        in.close();
    }

    @Override
    public void unpack(File file, JarOutputStream out) throws IOException {
        if (file == null || out == null) {
            throw new IllegalArgumentException("Must specify both input and output streams");
        }
        int size = (int)file.length();
        int bufferSize = size > 0 && size < 8192 ? size : 8192;
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file), bufferSize);
        this.unpack(in, out);
    }
}

