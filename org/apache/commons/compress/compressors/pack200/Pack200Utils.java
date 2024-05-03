/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.compressors.pack200;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.apache.commons.compress.java.util.jar.Pack200;

public class Pack200Utils {
    private Pack200Utils() {
    }

    public static void normalize(File jar) throws IOException {
        Pack200Utils.normalize(jar, jar, null);
    }

    public static void normalize(File jar, Map<String, String> props) throws IOException {
        Pack200Utils.normalize(jar, jar, props);
    }

    public static void normalize(File from, File to) throws IOException {
        Pack200Utils.normalize(from, to, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void normalize(File from, File to, Map<String, String> props) throws IOException {
        if (props == null) {
            props = new HashMap<String, String>();
        }
        props.put("pack.segment.limit", "-1");
        File tempFile = File.createTempFile("commons-compress", "pack200normalize");
        try {
            try (OutputStream fos = Files.newOutputStream(tempFile.toPath(), new OpenOption[0]);
                 JarFile jarFile = new JarFile(from);){
                Pack200.Packer packer = Pack200.newPacker();
                packer.properties().putAll(props);
                packer.pack(jarFile, fos);
            }
            Pack200.Unpacker unpacker = Pack200.newUnpacker();
            try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(to.toPath(), new OpenOption[0]));){
                unpacker.unpack(tempFile, jos);
            }
        } finally {
            if (!tempFile.delete()) {
                tempFile.deleteOnExit();
            }
        }
    }
}

