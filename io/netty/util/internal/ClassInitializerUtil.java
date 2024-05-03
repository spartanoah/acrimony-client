/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal;

import io.netty.util.internal.PlatformDependent;

public final class ClassInitializerUtil {
    private ClassInitializerUtil() {
    }

    public static void tryLoadClasses(Class<?> loadingClass, Class<?> ... classes) {
        ClassLoader loader = PlatformDependent.getClassLoader(loadingClass);
        for (Class<?> clazz : classes) {
            ClassInitializerUtil.tryLoadClass(loader, clazz.getName());
        }
    }

    private static void tryLoadClass(ClassLoader classLoader, String className) {
        try {
            Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException classNotFoundException) {
        } catch (SecurityException securityException) {
            // empty catch block
        }
    }
}

