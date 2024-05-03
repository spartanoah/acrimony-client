/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.utils;

import java.io.File;

public class FileNameUtils {
    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        String name = new File(filename).getName();
        int extensionPosition = name.lastIndexOf(46);
        if (extensionPosition < 0) {
            return "";
        }
        return name.substring(extensionPosition + 1);
    }

    public static String getBaseName(String filename) {
        if (filename == null) {
            return null;
        }
        String name = new File(filename).getName();
        int extensionPosition = name.lastIndexOf(46);
        if (extensionPosition < 0) {
            return name;
        }
        return name.substring(0, extensionPosition);
    }
}

