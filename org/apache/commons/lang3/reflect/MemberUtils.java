/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.lang3.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import org.apache.commons.lang3.ClassUtils;

abstract class MemberUtils {
    private static final int ACCESS_TEST = 7;
    private static final Class<?>[] ORDERED_PRIMITIVE_TYPES = new Class[]{Byte.TYPE, Short.TYPE, Character.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE};

    MemberUtils() {
    }

    static boolean setAccessibleWorkaround(AccessibleObject o) {
        if (o == null || o.isAccessible()) {
            return false;
        }
        Member m = (Member)((Object)o);
        if (!o.isAccessible() && Modifier.isPublic(m.getModifiers()) && MemberUtils.isPackageAccess(m.getDeclaringClass().getModifiers())) {
            try {
                o.setAccessible(true);
                return true;
            } catch (SecurityException securityException) {
                // empty catch block
            }
        }
        return false;
    }

    static boolean isPackageAccess(int modifiers) {
        return (modifiers & 7) == 0;
    }

    static boolean isAccessible(Member m) {
        return m != null && Modifier.isPublic(m.getModifiers()) && !m.isSynthetic();
    }

    static int compareParameterTypes(Class<?>[] left, Class<?>[] right, Class<?>[] actual) {
        float rightCost;
        float leftCost = MemberUtils.getTotalTransformationCost(actual, left);
        return leftCost < (rightCost = MemberUtils.getTotalTransformationCost(actual, right)) ? -1 : (rightCost < leftCost ? 1 : 0);
    }

    private static float getTotalTransformationCost(Class<?>[] srcArgs, Class<?>[] destArgs) {
        float totalCost = 0.0f;
        for (int i = 0; i < srcArgs.length; ++i) {
            Class<?> srcClass = srcArgs[i];
            Class<?> destClass = destArgs[i];
            totalCost += MemberUtils.getObjectTransformationCost(srcClass, destClass);
        }
        return totalCost;
    }

    private static float getObjectTransformationCost(Class<?> srcClass, Class<?> destClass) {
        if (destClass.isPrimitive()) {
            return MemberUtils.getPrimitivePromotionCost(srcClass, destClass);
        }
        float cost = 0.0f;
        while (srcClass != null && !destClass.equals(srcClass)) {
            if (destClass.isInterface() && ClassUtils.isAssignable(srcClass, destClass)) {
                cost += 0.25f;
                break;
            }
            cost += 1.0f;
            srcClass = srcClass.getSuperclass();
        }
        if (srcClass == null) {
            cost += 1.5f;
        }
        return cost;
    }

    private static float getPrimitivePromotionCost(Class<?> srcClass, Class<?> destClass) {
        float cost = 0.0f;
        Class<?> cls = srcClass;
        if (!cls.isPrimitive()) {
            cost += 0.1f;
            cls = ClassUtils.wrapperToPrimitive(cls);
        }
        for (int i = 0; cls != destClass && i < ORDERED_PRIMITIVE_TYPES.length; ++i) {
            if (cls != ORDERED_PRIMITIVE_TYPES[i]) continue;
            cost += 0.1f;
            if (i >= ORDERED_PRIMITIVE_TYPES.length - 1) continue;
            cls = ORDERED_PRIMITIVE_TYPES[i + 1];
        }
        return cost;
    }
}

