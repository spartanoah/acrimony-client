/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal.shaded.org.jctools.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import sun.misc.Unsafe;

public class UnsafeAccess {
    public static final boolean SUPPORTS_GET_AND_SET_REF;
    public static final boolean SUPPORTS_GET_AND_ADD_LONG;
    public static final Unsafe UNSAFE;

    private static Unsafe getUnsafe() {
        Unsafe instance;
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            instance = (Unsafe)field.get(null);
        } catch (Exception ignored) {
            try {
                Constructor c = Unsafe.class.getDeclaredConstructor(new Class[0]);
                c.setAccessible(true);
                instance = (Unsafe)c.newInstance(new Object[0]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    private static boolean hasGetAndSetSupport() {
        try {
            Unsafe.class.getMethod("getAndSetObject", Object.class, Long.TYPE, Object.class);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private static boolean hasGetAndAddLongSupport() {
        try {
            Unsafe.class.getMethod("getAndAddLong", Object.class, Long.TYPE, Long.TYPE);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public static long fieldOffset(Class clz, String fieldName) throws RuntimeException {
        try {
            return UNSAFE.objectFieldOffset(clz.getDeclaredField(fieldName));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        UNSAFE = UnsafeAccess.getUnsafe();
        SUPPORTS_GET_AND_SET_REF = UnsafeAccess.hasGetAndSetSupport();
        SUPPORTS_GET_AND_ADD_LONG = UnsafeAccess.hasGetAndAddLongSupport();
    }
}

