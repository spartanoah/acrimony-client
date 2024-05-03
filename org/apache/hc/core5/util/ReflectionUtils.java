/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.util;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.hc.core5.annotation.Internal;

@Internal
public final class ReflectionUtils {
    public static void callSetter(Object object, String setterName, Class<?> type, Object value) {
        try {
            Class<?> clazz = object.getClass();
            Method method = clazz.getMethod("set" + setterName, type);
            ReflectionUtils.setAccessible(method);
            method.invoke(object, value);
        } catch (Exception ignore) {
            // empty catch block
        }
    }

    public static <T> T callGetter(Object object, String getterName, Class<T> resultType) {
        try {
            Class<?> clazz = object.getClass();
            Method method = clazz.getMethod("get" + getterName, new Class[0]);
            ReflectionUtils.setAccessible(method);
            return resultType.cast(method.invoke(object, new Object[0]));
        } catch (Exception ignore) {
            return null;
        }
    }

    private static void setAccessible(final Method method) {
        AccessController.doPrivileged(new PrivilegedAction<Object>(){

            @Override
            public Object run() {
                method.setAccessible(true);
                return null;
            }
        });
    }

    public static int determineJRELevel() {
        String s = System.getProperty("java.version");
        String[] parts = s.split("\\.");
        if (parts.length > 0) {
            try {
                int majorVersion = Integer.parseInt(parts[0]);
                if (majorVersion > 1) {
                    return majorVersion;
                }
                if (majorVersion == 1 && parts.length > 1) {
                    return Integer.parseInt(parts[1]);
                }
            } catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return 7;
    }
}

