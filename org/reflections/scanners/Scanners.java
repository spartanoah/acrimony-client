/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  javassist.bytecode.ClassFile
 *  javassist.bytecode.FieldInfo
 *  javassist.bytecode.MethodInfo
 */
package org.reflections.scanners;

import java.lang.annotation.Inherited;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import org.reflections.Store;
import org.reflections.scanners.Scanner;
import org.reflections.util.FilterBuilder;
import org.reflections.util.JavassistHelper;
import org.reflections.util.NameHelper;
import org.reflections.util.QueryBuilder;
import org.reflections.util.QueryFunction;
import org.reflections.vfs.Vfs;

public enum Scanners implements Scanner,
QueryBuilder,
NameHelper
{
    SubTypes{
        {
            this.filterResultsBy(new FilterBuilder().excludePattern("java\\.lang\\.Object"));
        }

        @Override
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            entries.add(this.entry(classFile.getSuperclass(), classFile.getName()));
            entries.addAll(this.entries(Arrays.asList(classFile.getInterfaces()), classFile.getName()));
        }
    }
    ,
    TypesAnnotated{

        @Override
        public boolean acceptResult(String annotation) {
            return super.acceptResult(annotation) || annotation.equals(Inherited.class.getName());
        }

        @Override
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            entries.addAll(this.entries(JavassistHelper.getAnnotations(arg_0 -> ((ClassFile)classFile).getAttribute(arg_0)), classFile.getName()));
        }
    }
    ,
    MethodsAnnotated{

        @Override
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            JavassistHelper.getMethods(classFile).forEach(method -> entries.addAll(this.entries(JavassistHelper.getAnnotations(arg_0 -> ((MethodInfo)method).getAttribute(arg_0)), JavassistHelper.methodName(classFile, method))));
        }
    }
    ,
    ConstructorsAnnotated{

        @Override
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            JavassistHelper.getConstructors(classFile).forEach(constructor -> entries.addAll(this.entries(JavassistHelper.getAnnotations(arg_0 -> ((MethodInfo)constructor).getAttribute(arg_0)), JavassistHelper.methodName(classFile, constructor))));
        }
    }
    ,
    FieldsAnnotated{

        @Override
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            classFile.getFields().forEach(field -> entries.addAll(this.entries(JavassistHelper.getAnnotations(arg_0 -> ((FieldInfo)field).getAttribute(arg_0)), JavassistHelper.fieldName(classFile, field))));
        }
    }
    ,
    Resources{

        @Override
        public boolean acceptsInput(String file) {
            return !file.endsWith(".class");
        }

        @Override
        public List<Map.Entry<String, String>> scan(Vfs.File file) {
            return Collections.singletonList(this.entry(file.getName(), file.getRelativePath()));
        }

        @Override
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            throw new IllegalStateException();
        }

        @Override
        public QueryFunction<Store, String> with(String pattern) {
            return store -> store.getOrDefault(this.index(), Collections.emptyMap()).entrySet().stream().filter(entry -> ((String)entry.getKey()).matches(pattern)).flatMap(entry -> ((Set)entry.getValue()).stream()).collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }
    ,
    MethodsParameter{

        @Override
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            JavassistHelper.getMethods(classFile).forEach(method -> {
                String value = JavassistHelper.methodName(classFile, method);
                entries.addAll(this.entries(JavassistHelper.getParameters(method), value));
                JavassistHelper.getParametersAnnotations(method).forEach(annotations -> entries.addAll(this.entries((Collection<String>)annotations, value)));
            });
        }
    }
    ,
    ConstructorsParameter{

        @Override
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            JavassistHelper.getConstructors(classFile).forEach(constructor -> {
                String value = JavassistHelper.methodName(classFile, constructor);
                entries.addAll(this.entries(JavassistHelper.getParameters(constructor), value));
                JavassistHelper.getParametersAnnotations(constructor).forEach(annotations -> entries.addAll(this.entries((Collection<String>)annotations, value)));
            });
        }
    }
    ,
    MethodsSignature{

        @Override
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            JavassistHelper.getMethods(classFile).forEach(method -> entries.add(this.entry(JavassistHelper.getParameters(method).toString(), JavassistHelper.methodName(classFile, method))));
        }

        @Override
        public QueryFunction<Store, String> with(AnnotatedElement ... keys) {
            return QueryFunction.single(this.toNames(keys).toString()).getAll(this::get);
        }
    }
    ,
    ConstructorsSignature{

        @Override
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            JavassistHelper.getConstructors(classFile).forEach(constructor -> entries.add(this.entry(JavassistHelper.getParameters(constructor).toString(), JavassistHelper.methodName(classFile, constructor))));
        }

        @Override
        public QueryFunction<Store, String> with(AnnotatedElement ... keys) {
            return QueryFunction.single(this.toNames(keys).toString()).getAll(this::get);
        }
    }
    ,
    MethodsReturn{

        @Override
        public void scan(ClassFile classFile, List<Map.Entry<String, String>> entries) {
            JavassistHelper.getMethods(classFile).forEach(method -> entries.add(this.entry(JavassistHelper.getReturnType(method), JavassistHelper.methodName(classFile, method))));
        }
    };

    private Predicate<String> resultFilter = s -> true;

    @Override
    public String index() {
        return this.name();
    }

    public Scanners filterResultsBy(Predicate<String> filter) {
        this.resultFilter = filter;
        return this;
    }

    @Override
    public final List<Map.Entry<String, String>> scan(ClassFile classFile) {
        ArrayList<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>();
        this.scan(classFile, entries);
        return entries.stream().filter(a -> this.acceptResult((String)a.getKey())).collect(Collectors.toList());
    }

    abstract void scan(ClassFile var1, List<Map.Entry<String, String>> var2);

    protected boolean acceptResult(String fqn) {
        return fqn != null && this.resultFilter.test(fqn);
    }
}

