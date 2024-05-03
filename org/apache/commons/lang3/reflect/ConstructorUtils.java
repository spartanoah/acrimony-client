/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.lang3.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.MemberUtils;

public class ConstructorUtils {
    public static <T> T invokeConstructor(Class<T> cls, Object ... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        args = ArrayUtils.nullToEmpty(args);
        Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return ConstructorUtils.invokeConstructor(cls, args, parameterTypes);
    }

    public static <T> T invokeConstructor(Class<T> cls, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        args = ArrayUtils.nullToEmpty(args);
        Constructor<T> ctor = ConstructorUtils.getMatchingAccessibleConstructor(cls, parameterTypes = ArrayUtils.nullToEmpty(parameterTypes));
        if (ctor == null) {
            throw new NoSuchMethodException("No such accessible constructor on object: " + cls.getName());
        }
        return ctor.newInstance(args);
    }

    public static <T> T invokeExactConstructor(Class<T> cls, Object ... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        args = ArrayUtils.nullToEmpty(args);
        Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return ConstructorUtils.invokeExactConstructor(cls, args, parameterTypes);
    }

    public static <T> T invokeExactConstructor(Class<T> cls, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        args = ArrayUtils.nullToEmpty(args);
        Constructor<T> ctor = ConstructorUtils.getAccessibleConstructor(cls, parameterTypes = ArrayUtils.nullToEmpty(parameterTypes));
        if (ctor == null) {
            throw new NoSuchMethodException("No such accessible constructor on object: " + cls.getName());
        }
        return ctor.newInstance(args);
    }

    public static <T> Constructor<T> getAccessibleConstructor(Class<T> cls, Class<?> ... parameterTypes) {
        Validate.notNull(cls, "class cannot be null", new Object[0]);
        try {
            return ConstructorUtils.getAccessibleConstructor(cls.getConstructor(parameterTypes));
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static <T> Constructor<T> getAccessibleConstructor(Constructor<T> ctor) {
        Validate.notNull(ctor, "constructor cannot be null", new Object[0]);
        return MemberUtils.isAccessible(ctor) && ConstructorUtils.isAccessible(ctor.getDeclaringClass()) ? ctor : null;
    }

    public static <T> Constructor<T> getMatchingAccessibleConstructor(Class<T> cls, Class<?> ... parameterTypes) {
        Validate.notNull(cls, "class cannot be null", new Object[0]);
        try {
            Constructor<T> ctor = cls.getConstructor(parameterTypes);
            MemberUtils.setAccessibleWorkaround(ctor);
            return ctor;
        } catch (NoSuchMethodException e) {
            Constructor<?>[] ctors;
            Constructor<?> result = null;
            for (Constructor<?> ctor : ctors = cls.getConstructors()) {
                Constructor<?> constructor;
                if (!ClassUtils.isAssignable(parameterTypes, ctor.getParameterTypes(), true) || (ctor = ConstructorUtils.getAccessibleConstructor(ctor)) == null) continue;
                MemberUtils.setAccessibleWorkaround(ctor);
                if (result != null && MemberUtils.compareParameterTypes(ctor.getParameterTypes(), result.getParameterTypes(), parameterTypes) >= 0) continue;
                result = constructor = ctor;
            }
            return result;
        }
    }

    private static boolean isAccessible(Class<?> type) {
        for (Class<?> cls = type; cls != null; cls = cls.getEnclosingClass()) {
            if (Modifier.isPublic(cls.getModifiers())) continue;
            return false;
        }
        return true;
    }
}

