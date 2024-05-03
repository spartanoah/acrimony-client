/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.java.util.jar;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.SortedMap;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import org.apache.commons.compress.harmony.archive.internal.nls.Messages;

public abstract class Pack200 {
    private static final String SYSTEM_PROPERTY_PACKER = "java.util.jar.Pack200.Packer";
    private static final String SYSTEM_PROPERTY_UNPACKER = "java.util.jar.Pack200.Unpacker";

    private Pack200() {
    }

    public static Packer newPacker() {
        return (Packer)AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                String className = System.getProperty(Pack200.SYSTEM_PROPERTY_PACKER, "org.apache.commons.compress.harmony.pack200.Pack200PackerAdapter");
                try {
                    return ClassLoader.getSystemClassLoader().loadClass(className).newInstance();
                } catch (Exception e) {
                    throw new Error(Messages.getString("archive.3E", className), e);
                }
            }
        });
    }

    public static Unpacker newUnpacker() {
        return (Unpacker)AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                String className = System.getProperty(Pack200.SYSTEM_PROPERTY_UNPACKER, "org.apache.commons.compress.harmony.unpack200.Pack200UnpackerAdapter");
                try {
                    return ClassLoader.getSystemClassLoader().loadClass(className).newInstance();
                } catch (Exception e) {
                    throw new Error(Messages.getString("archive.3E", className), e);
                }
            }
        });
    }

    public static interface Unpacker {
        public static final String DEFLATE_HINT = "unpack.deflate.hint";
        public static final String FALSE = "false";
        public static final String KEEP = "keep";
        public static final String PROGRESS = "unpack.progress";
        public static final String TRUE = "true";

        public SortedMap<String, String> properties();

        public void unpack(InputStream var1, JarOutputStream var2) throws IOException;

        public void unpack(File var1, JarOutputStream var2) throws IOException;

        public void addPropertyChangeListener(PropertyChangeListener var1);

        public void removePropertyChangeListener(PropertyChangeListener var1);
    }

    public static interface Packer {
        public static final String CLASS_ATTRIBUTE_PFX = "pack.class.attribute.";
        public static final String CODE_ATTRIBUTE_PFX = "pack.code.attribute.";
        public static final String DEFLATE_HINT = "pack.deflate.hint";
        public static final String EFFORT = "pack.effort";
        public static final String ERROR = "error";
        public static final String FALSE = "false";
        public static final String FIELD_ATTRIBUTE_PFX = "pack.field.attribute.";
        public static final String KEEP = "keep";
        public static final String KEEP_FILE_ORDER = "pack.keep.file.order";
        public static final String LATEST = "latest";
        public static final String METHOD_ATTRIBUTE_PFX = "pack.method.attribute.";
        public static final String MODIFICATION_TIME = "pack.modification.time";
        public static final String PASS = "pass";
        public static final String PASS_FILE_PFX = "pack.pass.file.";
        public static final String PROGRESS = "pack.progress";
        public static final String SEGMENT_LIMIT = "pack.segment.limit";
        public static final String STRIP = "strip";
        public static final String TRUE = "true";
        public static final String UNKNOWN_ATTRIBUTE = "pack.unknown.attribute";

        public SortedMap<String, String> properties();

        public void pack(JarFile var1, OutputStream var2) throws IOException;

        public void pack(JarInputStream var1, OutputStream var2) throws IOException;

        public void addPropertyChangeListener(PropertyChangeListener var1);

        public void removePropertyChangeListener(PropertyChangeListener var1);
    }
}

