/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.utils;

public class OsgiUtils {
    private static final boolean inOsgiEnvironment;

    private static boolean isBundleReference(Class<?> clazz) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            if (c.getName().equals("org.osgi.framework.BundleReference")) {
                return true;
            }
            for (Class<?> ifc : c.getInterfaces()) {
                if (!OsgiUtils.isBundleReference(ifc)) continue;
                return true;
            }
        }
        return false;
    }

    public static boolean isRunningInOsgiEnvironment() {
        return inOsgiEnvironment;
    }

    static {
        Class<?> classloaderClass = OsgiUtils.class.getClassLoader().getClass();
        inOsgiEnvironment = OsgiUtils.isBundleReference(classloaderClass);
    }
}

