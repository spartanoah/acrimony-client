/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  javassist.bytecode.AccessFlag
 *  javassist.bytecode.AnnotationsAttribute
 *  javassist.bytecode.AttributeInfo
 *  javassist.bytecode.ClassFile
 *  javassist.bytecode.Descriptor
 *  javassist.bytecode.Descriptor$Iterator
 *  javassist.bytecode.FieldInfo
 *  javassist.bytecode.MethodInfo
 *  javassist.bytecode.ParameterAnnotationsAttribute
 *  javassist.bytecode.annotation.Annotation
 */
package org.reflections.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

public class JavassistHelper {
    public static boolean includeInvisibleTag = true;

    public static String fieldName(ClassFile classFile, FieldInfo object) {
        return String.format("%s.%s", classFile.getName(), object.getName());
    }

    public static String methodName(ClassFile classFile, MethodInfo object) {
        return String.format("%s.%s(%s)", classFile.getName(), object.getName(), String.join((CharSequence)", ", JavassistHelper.getParameters(object)));
    }

    public static boolean isPublic(Object object) {
        if (object instanceof ClassFile) {
            return AccessFlag.isPublic((int)((ClassFile)object).getAccessFlags());
        }
        if (object instanceof FieldInfo) {
            return AccessFlag.isPublic((int)((FieldInfo)object).getAccessFlags());
        }
        if (object instanceof MethodInfo) {
            return AccessFlag.isPublic((int)((MethodInfo)object).getAccessFlags());
        }
        return false;
    }

    public static Stream<MethodInfo> getMethods(ClassFile classFile) {
        return classFile.getMethods().stream().filter(MethodInfo::isMethod);
    }

    public static Stream<MethodInfo> getConstructors(ClassFile classFile) {
        return classFile.getMethods().stream().filter(methodInfo -> !methodInfo.isMethod());
    }

    public static List<String> getParameters(MethodInfo method) {
        ArrayList<String> result = new ArrayList<String>();
        String descriptor = method.getDescriptor().substring(1);
        Descriptor.Iterator iterator = new Descriptor.Iterator(descriptor);
        Integer prev = null;
        while (iterator.hasNext()) {
            int cur = iterator.next();
            if (prev != null) {
                result.add(Descriptor.toString((String)descriptor.substring(prev, cur)));
            }
            prev = cur;
        }
        return result;
    }

    public static String getReturnType(MethodInfo method) {
        String descriptor = method.getDescriptor();
        descriptor = descriptor.substring(descriptor.lastIndexOf(")") + 1);
        return Descriptor.toString((String)descriptor);
    }

    public static List<String> getAnnotations(Function<String, AttributeInfo> function) {
        Function<String, List> names = function.andThen(attribute -> attribute != null ? ((AnnotationsAttribute)attribute).getAnnotations() : null).andThen(JavassistHelper::annotationNames);
        ArrayList<String> result = new ArrayList<String>(names.apply("RuntimeVisibleAnnotations"));
        if (includeInvisibleTag) {
            result.addAll(names.apply("RuntimeInvisibleAnnotations"));
        }
        return result;
    }

    public static List<List<String>> getParametersAnnotations(MethodInfo method) {
        Function<String, List> names = ((Function<String, AttributeInfo>)arg_0 -> ((MethodInfo)method).getAttribute(arg_0)).andThen(attribute -> attribute != null ? ((ParameterAnnotationsAttribute)attribute).getAnnotations() : (Annotation[][])null).andThen(aa -> aa != null ? Stream.of(aa).map(JavassistHelper::annotationNames).collect(Collectors.toList()) : Collections.emptyList());
        List visibleAnnotations = names.apply("RuntimeVisibleParameterAnnotations");
        if (!includeInvisibleTag) {
            return new ArrayList<List<String>>(visibleAnnotations);
        }
        List invisibleAnnotations = names.apply("RuntimeInvisibleParameterAnnotations");
        if (invisibleAnnotations.isEmpty()) {
            return new ArrayList<List<String>>(visibleAnnotations);
        }
        ArrayList<List<String>> result = new ArrayList<List<String>>();
        for (int i = 0; i < Math.max(visibleAnnotations.size(), invisibleAnnotations.size()); ++i) {
            ArrayList concat = new ArrayList();
            if (i < visibleAnnotations.size()) {
                concat.addAll((Collection)visibleAnnotations.get(i));
            }
            if (i < invisibleAnnotations.size()) {
                concat.addAll((Collection)invisibleAnnotations.get(i));
            }
            result.add(concat);
        }
        return result;
    }

    private static List<String> annotationNames(Annotation[] annotations) {
        return annotations != null ? Stream.of(annotations).map(Annotation::getTypeName).collect(Collectors.toList()) : Collections.emptyList();
    }
}

