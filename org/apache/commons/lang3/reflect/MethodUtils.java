/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.lang3.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.MemberUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

public class MethodUtils {
    public static Object invokeMethod(Object object, String methodName, Object ... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return MethodUtils.invokeMethod(object, methodName, args, parameterTypes);
    }

    public static Object invokeMethod(Object object, String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
        args = ArrayUtils.nullToEmpty(args);
        Method method = MethodUtils.getMatchingAccessibleMethod(object.getClass(), methodName, parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: " + methodName + "() on object: " + object.getClass().getName());
        }
        return method.invoke(object, args);
    }

    public static Object invokeExactMethod(Object object, String methodName, Object ... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return MethodUtils.invokeExactMethod(object, methodName, args, parameterTypes);
    }

    public static Object invokeExactMethod(Object object, String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
        Method method = MethodUtils.getAccessibleMethod(object.getClass(), methodName, parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: " + methodName + "() on object: " + object.getClass().getName());
        }
        return method.invoke(object, args);
    }

    public static Object invokeExactStaticMethod(Class<?> cls, String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        Method method = MethodUtils.getAccessibleMethod(cls, methodName, parameterTypes = ArrayUtils.nullToEmpty(parameterTypes));
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: " + methodName + "() on class: " + cls.getName());
        }
        return method.invoke(null, args);
    }

    public static Object invokeStaticMethod(Class<?> cls, String methodName, Object ... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return MethodUtils.invokeStaticMethod(cls, methodName, args, parameterTypes);
    }

    public static Object invokeStaticMethod(Class<?> cls, String methodName, Object[] args, Class<?>[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        Method method = MethodUtils.getMatchingAccessibleMethod(cls, methodName, parameterTypes = ArrayUtils.nullToEmpty(parameterTypes));
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: " + methodName + "() on class: " + cls.getName());
        }
        return method.invoke(null, args);
    }

    public static Object invokeExactStaticMethod(Class<?> cls, String methodName, Object ... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return MethodUtils.invokeExactStaticMethod(cls, methodName, args, parameterTypes);
    }

    public static Method getAccessibleMethod(Class<?> cls, String methodName, Class<?> ... parameterTypes) {
        try {
            return MethodUtils.getAccessibleMethod(cls.getMethod(methodName, parameterTypes));
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static Method getAccessibleMethod(Method method) {
        Class<?>[] parameterTypes;
        if (!MemberUtils.isAccessible(method)) {
            return null;
        }
        Class<?> cls = method.getDeclaringClass();
        if (Modifier.isPublic(cls.getModifiers())) {
            return method;
        }
        String methodName = method.getName();
        if ((method = MethodUtils.getAccessibleMethodFromInterfaceNest(cls, methodName, parameterTypes = method.getParameterTypes())) == null) {
            method = MethodUtils.getAccessibleMethodFromSuperclass(cls, methodName, parameterTypes);
        }
        return method;
    }

    private static Method getAccessibleMethodFromSuperclass(Class<?> cls, String methodName, Class<?> ... parameterTypes) {
        for (Class<?> parentClass = cls.getSuperclass(); parentClass != null; parentClass = parentClass.getSuperclass()) {
            if (!Modifier.isPublic(parentClass.getModifiers())) continue;
            try {
                return parentClass.getMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }
        return null;
    }

    private static Method getAccessibleMethodFromInterfaceNest(Class<?> cls, String methodName, Class<?> ... parameterTypes) {
        while (cls != null) {
            Class<?>[] interfaces = cls.getInterfaces();
            for (int i = 0; i < interfaces.length; ++i) {
                if (!Modifier.isPublic(interfaces[i].getModifiers())) continue;
                try {
                    return interfaces[i].getDeclaredMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException e) {
                    Method method = MethodUtils.getAccessibleMethodFromInterfaceNest(interfaces[i], methodName, parameterTypes);
                    if (method == null) continue;
                    return method;
                }
            }
            cls = cls.getSuperclass();
        }
        return null;
    }

    public static Method getMatchingAccessibleMethod(Class<?> cls, String methodName, Class<?> ... parameterTypes) {
        try {
            Method method = cls.getMethod(methodName, parameterTypes);
            MemberUtils.setAccessibleWorkaround(method);
            return method;
        } catch (NoSuchMethodException e) {
            Method[] methods;
            Method bestMatch = null;
            for (Method method : methods = cls.getMethods()) {
                Method accessibleMethod;
                if (!method.getName().equals(methodName) || !ClassUtils.isAssignable(parameterTypes, method.getParameterTypes(), true) || (accessibleMethod = MethodUtils.getAccessibleMethod(method)) == null || bestMatch != null && MemberUtils.compareParameterTypes(accessibleMethod.getParameterTypes(), bestMatch.getParameterTypes(), parameterTypes) >= 0) continue;
                bestMatch = accessibleMethod;
            }
            if (bestMatch != null) {
                MemberUtils.setAccessibleWorkaround(bestMatch);
            }
            return bestMatch;
        }
    }

    public static Set<Method> getOverrideHierarchy(Method method, ClassUtils.Interfaces interfacesBehavior) {
        Validate.notNull(method);
        LinkedHashSet<Method> result = new LinkedHashSet<Method>();
        result.add(method);
        Object[] parameterTypes = method.getParameterTypes();
        Class<?> declaringClass = method.getDeclaringClass();
        Iterator<Class<?>> hierarchy = ClassUtils.hierarchy(declaringClass, interfacesBehavior).iterator();
        hierarchy.next();
        block0: while (hierarchy.hasNext()) {
            Class<?> c = hierarchy.next();
            Method m = MethodUtils.getMatchingAccessibleMethod(c, method.getName(), parameterTypes);
            if (m == null) continue;
            if (Arrays.equals(m.getParameterTypes(), parameterTypes)) {
                result.add(m);
                continue;
            }
            Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(declaringClass, m.getDeclaringClass());
            for (int i = 0; i < parameterTypes.length; ++i) {
                Type parentType;
                Type childType = TypeUtils.unrollVariables(typeArguments, method.getGenericParameterTypes()[i]);
                if (!TypeUtils.equals(childType, parentType = TypeUtils.unrollVariables(typeArguments, m.getGenericParameterTypes()[i]))) continue block0;
            }
            result.add(m);
        }
        return result;
    }
}

