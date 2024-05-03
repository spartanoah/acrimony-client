/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  javassist.CannotCompileException
 *  javassist.ClassPath
 *  javassist.ClassPool
 *  javassist.CtBehavior
 *  javassist.CtClass
 *  javassist.CtConstructor
 *  javassist.LoaderClassPath
 *  javassist.NotFoundException
 *  javassist.bytecode.ClassFile
 *  javassist.bytecode.MethodInfo
 *  javassist.expr.ConstructorCall
 *  javassist.expr.ExprEditor
 *  javassist.expr.FieldAccess
 *  javassist.expr.MethodCall
 *  javassist.expr.NewExpr
 */
package org.reflections.scanners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;
import javax.annotation.Nonnull;
import org.reflections.ReflectionsException;
import org.reflections.scanners.Scanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.JavassistHelper;

public class MemberUsageScanner
implements Scanner {
    private Predicate<String> resultFilter = s -> true;
    private final ClassLoader[] classLoaders;
    private volatile ClassPool classPool;

    public MemberUsageScanner() {
        this(ClasspathHelper.classLoaders(new ClassLoader[0]));
    }

    public MemberUsageScanner(@Nonnull ClassLoader[] classLoaders) {
        this.classLoaders = classLoaders;
    }

    @Override
    public List<Map.Entry<String, String>> scan(ClassFile classFile) {
        ArrayList<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>();
        CtClass ctClass = null;
        try {
            ctClass = this.getClassPool().get(classFile.getName());
            for (CtConstructor ctConstructor : ctClass.getDeclaredConstructors()) {
                this.scanMember((CtBehavior)ctConstructor, entries);
            }
            for (CtConstructor ctConstructor : ctClass.getDeclaredMethods()) {
                this.scanMember((CtBehavior)ctConstructor, entries);
            }
        } catch (Exception e) {
            throw new ReflectionsException("Could not scan method usage for " + classFile.getName(), e);
        } finally {
            if (ctClass != null) {
                ctClass.detach();
            }
        }
        return entries;
    }

    public Scanner filterResultsBy(Predicate<String> filter) {
        this.resultFilter = filter;
        return this;
    }

    private void scanMember(CtBehavior member, final List<Map.Entry<String, String>> entries) throws CannotCompileException {
        final String key = member.getDeclaringClass().getName() + "." + member.getMethodInfo().getName() + "(" + MemberUsageScanner.parameterNames(member.getMethodInfo()) + ")";
        member.instrument(new ExprEditor(){

            public void edit(NewExpr e) {
                try {
                    MemberUsageScanner.this.add(entries, e.getConstructor().getDeclaringClass().getName() + ".<init>(" + MemberUsageScanner.parameterNames(e.getConstructor().getMethodInfo()) + ")", key + " #" + e.getLineNumber());
                } catch (NotFoundException e1) {
                    throw new ReflectionsException("Could not find new instance usage in " + key, e1);
                }
            }

            public void edit(MethodCall m) {
                try {
                    MemberUsageScanner.this.add(entries, m.getMethod().getDeclaringClass().getName() + "." + m.getMethodName() + "(" + MemberUsageScanner.parameterNames(m.getMethod().getMethodInfo()) + ")", key + " #" + m.getLineNumber());
                } catch (NotFoundException e) {
                    throw new ReflectionsException("Could not find member " + m.getClassName() + " in " + key, e);
                }
            }

            public void edit(ConstructorCall c) {
                try {
                    MemberUsageScanner.this.add(entries, c.getConstructor().getDeclaringClass().getName() + ".<init>(" + MemberUsageScanner.parameterNames(c.getConstructor().getMethodInfo()) + ")", key + " #" + c.getLineNumber());
                } catch (NotFoundException e) {
                    throw new ReflectionsException("Could not find member " + c.getClassName() + " in " + key, e);
                }
            }

            public void edit(FieldAccess f) {
                try {
                    MemberUsageScanner.this.add(entries, f.getField().getDeclaringClass().getName() + "." + f.getFieldName(), key + " #" + f.getLineNumber());
                } catch (NotFoundException e) {
                    throw new ReflectionsException("Could not find member " + f.getFieldName() + " in " + key, e);
                }
            }
        });
    }

    private void add(List<Map.Entry<String, String>> entries, String key, String value) {
        if (this.resultFilter.test(key)) {
            entries.add(this.entry(key, value));
        }
    }

    public static String parameterNames(MethodInfo info) {
        return String.join((CharSequence)", ", JavassistHelper.getParameters(info));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private ClassPool getClassPool() {
        if (this.classPool == null) {
            MemberUsageScanner memberUsageScanner = this;
            synchronized (memberUsageScanner) {
                if (this.classPool == null) {
                    this.classPool = new ClassPool();
                    for (ClassLoader classLoader : this.classLoaders) {
                        this.classPool.appendClassPath((ClassPath)new LoaderClassPath(classLoader));
                    }
                }
            }
        }
        return this.classPool;
    }
}

