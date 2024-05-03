/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.reflections.util;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.reflections.ReflectionsException;
import org.reflections.util.ClasspathHelper;

public interface NameHelper {
    public static final List<String> primitiveNames = Arrays.asList("boolean", "char", "byte", "short", "int", "long", "float", "double", "void");
    public static final List<Class<?>> primitiveTypes = Arrays.asList(Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE, Void.TYPE);
    public static final List<String> primitiveDescriptors = Arrays.asList("Z", "C", "B", "S", "I", "J", "F", "D", "V");

    default public String toName(AnnotatedElement element) {
        return element.getClass().equals(Class.class) ? this.toName((Class)element) : (element.getClass().equals(Constructor.class) ? this.toName((Constructor)element) : (element.getClass().equals(Method.class) ? this.toName((Method)element) : (element.getClass().equals(Field.class) ? this.toName((Field)element) : null)));
    }

    default public String toName(Class<?> type) {
        int dim = 0;
        while (type.isArray()) {
            ++dim;
            type = type.getComponentType();
        }
        return type.getName() + String.join((CharSequence)"", Collections.nCopies(dim, "[]"));
    }

    default public String toName(Constructor<?> constructor) {
        return String.format("%s.<init>(%s)", constructor.getName(), String.join((CharSequence)", ", this.toNames(constructor.getParameterTypes())));
    }

    default public String toName(Method method) {
        return String.format("%s.%s(%s)", method.getDeclaringClass().getName(), method.getName(), String.join((CharSequence)", ", this.toNames(method.getParameterTypes())));
    }

    default public String toName(Field field) {
        return String.format("%s.%s", field.getDeclaringClass().getName(), field.getName());
    }

    default public Collection<String> toNames(Collection<? extends AnnotatedElement> elements) {
        return elements.stream().map(this::toName).filter(Objects::nonNull).collect(Collectors.toList());
    }

    default public Collection<String> toNames(AnnotatedElement ... elements) {
        return this.toNames(Arrays.asList(elements));
    }

    default public <T> T forName(String name, Class<T> resultType, ClassLoader ... loaders) {
        return (T)(resultType.equals(Class.class) ? this.forClass(name, loaders) : (resultType.equals(Constructor.class) ? this.forConstructor(name, loaders) : (resultType.equals(Method.class) ? this.forMethod(name, loaders) : (resultType.equals(Field.class) ? this.forField(name, loaders) : (resultType.equals(Member.class) ? this.forMember(name, loaders) : null)))));
    }

    default public Class<?> forClass(String typeName, ClassLoader ... loaders) {
        String type;
        if (primitiveNames.contains(typeName)) {
            return primitiveTypes.get(primitiveNames.indexOf(typeName));
        }
        if (typeName.contains("[")) {
            int i = typeName.indexOf("[");
            type = typeName.substring(0, i);
            String array = typeName.substring(i).replace("]", "");
            type = primitiveNames.contains(type) ? primitiveDescriptors.get(primitiveNames.indexOf(type)) : "L" + type + ";";
            type = array + type;
        } else {
            type = typeName;
        }
        for (ClassLoader classLoader : ClasspathHelper.classLoaders(loaders)) {
            if (type.contains("[")) {
                try {
                    return Class.forName(type, false, classLoader);
                } catch (Throwable throwable) {
                    // empty catch block
                }
            }
            try {
                return classLoader.loadClass(type);
            } catch (Throwable throwable) {
            }
        }
        return null;
    }

    default public Member forMember(String descriptor, ClassLoader ... loaders) throws ReflectionsException {
        int p0 = descriptor.lastIndexOf(40);
        String memberKey = p0 != -1 ? descriptor.substring(0, p0) : descriptor;
        String methodParameters = p0 != -1 ? descriptor.substring(p0 + 1, descriptor.lastIndexOf(41)) : "";
        int p1 = Math.max(memberKey.lastIndexOf(46), memberKey.lastIndexOf("$"));
        String className = memberKey.substring(0, p1);
        String memberName = memberKey.substring(p1 + 1);
        Class[] parameterTypes = null;
        if (!methodParameters.isEmpty()) {
            String[] parameterNames = methodParameters.split(",");
            parameterTypes = (Class[])Arrays.stream(parameterNames).map(name -> this.forClass(name.trim(), loaders)).toArray(Class[]::new);
        }
        try {
        } catch (Exception e) {
            return null;
        }
        for (Class<?> aClass = this.forClass(className, loaders); aClass != null; aClass = aClass.getSuperclass()) {
            try {
                if (!descriptor.contains("(")) {
                    return aClass.isInterface() ? aClass.getField(memberName) : aClass.getDeclaredField(memberName);
                }
                if (descriptor.contains("init>")) {
                    return aClass.isInterface() ? aClass.getConstructor(parameterTypes) : aClass.getDeclaredConstructor(parameterTypes);
                }
                return aClass.isInterface() ? aClass.getMethod(memberName, parameterTypes) : aClass.getDeclaredMethod(memberName, parameterTypes);
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }

    @Nullable
    default public <T extends AnnotatedElement> T forElement(String descriptor, Class<T> resultType, ClassLoader[] loaders) {
        Member member = this.forMember(descriptor, loaders);
        return (T)(member != null && member.getClass().equals(resultType) ? (AnnotatedElement)((Object)member) : null);
    }

    @Nullable
    default public Method forMethod(String descriptor, ClassLoader ... loaders) throws ReflectionsException {
        return this.forElement(descriptor, Method.class, loaders);
    }

    default public Constructor<?> forConstructor(String descriptor, ClassLoader ... loaders) throws ReflectionsException {
        return this.forElement(descriptor, Constructor.class, loaders);
    }

    @Nullable
    default public Field forField(String descriptor, ClassLoader ... loaders) {
        return this.forElement(descriptor, Field.class, loaders);
    }

    default public <T> Collection<T> forNames(Collection<String> names, Class<T> resultType, ClassLoader ... loaders) {
        return names.stream().map(name -> this.forName((String)name, resultType, loaders)).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    default public Collection<Class<?>> forNames(Collection<String> names, ClassLoader ... loaders) {
        return this.forNames(names, Class.class, loaders);
    }
}

