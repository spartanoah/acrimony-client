/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class BeanUtil {
    public static String okNameForGetter(AnnotatedMethod am, boolean stdNaming) {
        String name = am.getName();
        String str = BeanUtil.okNameForIsGetter(am, name, stdNaming);
        if (str == null) {
            str = BeanUtil.okNameForRegularGetter(am, name, stdNaming);
        }
        return str;
    }

    public static String okNameForRegularGetter(AnnotatedMethod am, String name, boolean stdNaming) {
        if (name.startsWith("get")) {
            if ("getCallbacks".equals(name) ? BeanUtil.isCglibGetCallbacks(am) : "getMetaClass".equals(name) && BeanUtil.isGroovyMetaClassGetter(am)) {
                return null;
            }
            return stdNaming ? BeanUtil.stdManglePropertyName(name, 3) : BeanUtil.legacyManglePropertyName(name, 3);
        }
        return null;
    }

    public static String okNameForIsGetter(AnnotatedMethod am, String name, boolean stdNaming) {
        Class<?> rt;
        if (name.startsWith("is") && ((rt = am.getRawType()) == Boolean.class || rt == Boolean.TYPE)) {
            return stdNaming ? BeanUtil.stdManglePropertyName(name, 2) : BeanUtil.legacyManglePropertyName(name, 2);
        }
        return null;
    }

    @Deprecated
    public static String okNameForSetter(AnnotatedMethod am, boolean stdNaming) {
        String name = BeanUtil.okNameForMutator(am, "set", stdNaming);
        if (!(name == null || "metaClass".equals(name) && BeanUtil.isGroovyMetaClassSetter(am))) {
            return name;
        }
        return null;
    }

    public static String okNameForMutator(AnnotatedMethod am, String prefix, boolean stdNaming) {
        String name = am.getName();
        if (name.startsWith(prefix)) {
            return stdNaming ? BeanUtil.stdManglePropertyName(name, prefix.length()) : BeanUtil.legacyManglePropertyName(name, prefix.length());
        }
        return null;
    }

    public static Object getDefaultValue(JavaType type) {
        Class<?> cls = type.getRawClass();
        Class<?> prim = ClassUtil.primitiveType(cls);
        if (prim != null) {
            return ClassUtil.defaultValue(prim);
        }
        if (type.isContainerType() || type.isReferenceType()) {
            return JsonInclude.Include.NON_EMPTY;
        }
        if (cls == String.class) {
            return "";
        }
        if (type.isTypeOrSubTypeOf(Date.class)) {
            return new Date(0L);
        }
        if (type.isTypeOrSubTypeOf(Calendar.class)) {
            GregorianCalendar c = new GregorianCalendar();
            c.setTimeInMillis(0L);
            return c;
        }
        return null;
    }

    protected static boolean isCglibGetCallbacks(AnnotatedMethod am) {
        Class<?> compType;
        String pkgName;
        Class<?> rt = am.getRawType();
        if (rt.isArray() && (pkgName = ClassUtil.getPackageName(compType = rt.getComponentType())) != null && pkgName.contains(".cglib")) {
            return pkgName.startsWith("net.sf.cglib") || pkgName.startsWith("org.hibernate.repackage.cglib") || pkgName.startsWith("org.springframework.cglib");
        }
        return false;
    }

    protected static boolean isGroovyMetaClassSetter(AnnotatedMethod am) {
        Class<?> argType = am.getRawParameterType(0);
        String pkgName = ClassUtil.getPackageName(argType);
        return pkgName != null && pkgName.startsWith("groovy.lang");
    }

    protected static boolean isGroovyMetaClassGetter(AnnotatedMethod am) {
        String pkgName = ClassUtil.getPackageName(am.getRawType());
        return pkgName != null && pkgName.startsWith("groovy.lang");
    }

    protected static String legacyManglePropertyName(String basename, int offset) {
        char d;
        int end = basename.length();
        if (end == offset) {
            return null;
        }
        char c = basename.charAt(offset);
        if (c == (d = Character.toLowerCase(c))) {
            return basename.substring(offset);
        }
        StringBuilder sb = new StringBuilder(end - offset);
        sb.append(d);
        for (int i = offset + 1; i < end; ++i) {
            c = basename.charAt(i);
            if (c == (d = Character.toLowerCase(c))) {
                sb.append(basename, i, end);
                break;
            }
            sb.append(d);
        }
        return sb.toString();
    }

    public static String stdManglePropertyName(String basename, int offset) {
        char c1;
        int end = basename.length();
        if (end == offset) {
            return null;
        }
        char c0 = basename.charAt(offset);
        if (c0 == (c1 = Character.toLowerCase(c0))) {
            return basename.substring(offset);
        }
        if (offset + 1 < end && Character.isUpperCase(basename.charAt(offset + 1))) {
            return basename.substring(offset);
        }
        StringBuilder sb = new StringBuilder(end - offset);
        sb.append(c1);
        sb.append(basename, offset + 1, end);
        return sb.toString();
    }
}

