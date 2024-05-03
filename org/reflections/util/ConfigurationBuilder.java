/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.reflections.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.reflections.Configuration;
import org.reflections.ReflectionsException;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.FilterBuilder;

public class ConfigurationBuilder
implements Configuration {
    public static final Set<Scanner> DEFAULT_SCANNERS = new HashSet<Scanners>(Arrays.asList(Scanners.TypesAnnotated, Scanners.SubTypes));
    public static final Predicate<String> DEFAULT_INPUTS_FILTER = t -> true;
    private Set<Scanner> scanners;
    private Set<URL> urls = new HashSet<URL>();
    private Predicate<String> inputsFilter;
    private boolean isParallel = true;
    private ClassLoader[] classLoaders;
    private boolean expandSuperTypes = true;

    public static ConfigurationBuilder build(Object ... params) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        ArrayList<Object> parameters = new ArrayList<Object>();
        for (Object param : params) {
            if (param.getClass().isArray()) {
                for (Object p2 : (Object[])param) {
                    parameters.add(p2);
                }
                continue;
            }
            if (param instanceof Iterable) {
                for (Object p3 : (Iterable)param) {
                    parameters.add(p3);
                }
                continue;
            }
            parameters.add(param);
        }
        ClassLoader[] loaders = (ClassLoader[])Stream.of(params).filter(p -> p instanceof ClassLoader).distinct().toArray(ClassLoader[]::new);
        if (loaders.length != 0) {
            builder.addClassLoaders(loaders);
        }
        FilterBuilder inputsFilter = new FilterBuilder();
        builder.filterInputsBy(inputsFilter);
        for (Object param : parameters) {
            if (param instanceof String && !((String)param).isEmpty()) {
                builder.forPackage((String)param, loaders);
                inputsFilter.includePackage((String)param);
                continue;
            }
            if (param instanceof Class && !Scanner.class.isAssignableFrom((Class)param)) {
                builder.addUrls(ClasspathHelper.forClass((Class)param, loaders));
                inputsFilter.includePackage(((Class)param).getPackage().getName());
                continue;
            }
            if (param instanceof URL) {
                builder.addUrls((URL)param);
                continue;
            }
            if (param instanceof Scanner) {
                builder.addScanners((Scanner)param);
                continue;
            }
            if (param instanceof Class && Scanner.class.isAssignableFrom((Class)param)) {
                try {
                    builder.addScanners((Scanner)((Class)param).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]));
                    continue;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if (param instanceof Predicate) {
                builder.filterInputsBy((Predicate)param);
                continue;
            }
            throw new ReflectionsException("could not use param '" + param + "'");
        }
        if (builder.getUrls().isEmpty()) {
            builder.addUrls(ClasspathHelper.forClassLoader(loaders));
        }
        return builder;
    }

    public ConfigurationBuilder forPackage(String pkg, ClassLoader ... classLoaders) {
        return this.addUrls(ClasspathHelper.forPackage(pkg, classLoaders));
    }

    public ConfigurationBuilder forPackages(String ... packages) {
        for (String pkg : packages) {
            this.forPackage(pkg, new ClassLoader[0]);
        }
        return this;
    }

    @Override
    public Set<Scanner> getScanners() {
        return this.scanners != null ? this.scanners : DEFAULT_SCANNERS;
    }

    public ConfigurationBuilder setScanners(Scanner ... scanners) {
        this.scanners = new HashSet<Scanner>(Arrays.asList(scanners));
        return this;
    }

    public ConfigurationBuilder addScanners(Scanner ... scanners) {
        if (this.scanners == null) {
            this.setScanners(scanners);
        } else {
            this.scanners.addAll(Arrays.asList(scanners));
        }
        return this;
    }

    @Override
    public Set<URL> getUrls() {
        return this.urls;
    }

    public ConfigurationBuilder setUrls(Collection<URL> urls) {
        this.urls = new HashSet<URL>(urls);
        return this;
    }

    public ConfigurationBuilder setUrls(URL ... urls) {
        return this.setUrls(Arrays.asList(urls));
    }

    public ConfigurationBuilder addUrls(Collection<URL> urls) {
        this.urls.addAll(urls);
        return this;
    }

    public ConfigurationBuilder addUrls(URL ... urls) {
        return this.addUrls(Arrays.asList(urls));
    }

    @Override
    public Predicate<String> getInputsFilter() {
        return this.inputsFilter != null ? this.inputsFilter : DEFAULT_INPUTS_FILTER;
    }

    public ConfigurationBuilder setInputsFilter(Predicate<String> inputsFilter) {
        this.inputsFilter = inputsFilter;
        return this;
    }

    public ConfigurationBuilder filterInputsBy(Predicate<String> inputsFilter) {
        return this.setInputsFilter(inputsFilter);
    }

    @Override
    public boolean isParallel() {
        return this.isParallel;
    }

    public ConfigurationBuilder setParallel(boolean parallel) {
        this.isParallel = parallel;
        return this;
    }

    @Override
    public ClassLoader[] getClassLoaders() {
        return this.classLoaders;
    }

    public ConfigurationBuilder setClassLoaders(ClassLoader[] classLoaders) {
        this.classLoaders = classLoaders;
        return this;
    }

    public ConfigurationBuilder addClassLoaders(ClassLoader ... classLoaders) {
        this.classLoaders = this.classLoaders == null ? classLoaders : (ClassLoader[])Stream.concat(Arrays.stream(this.classLoaders), Arrays.stream(classLoaders)).distinct().toArray(ClassLoader[]::new);
        return this;
    }

    @Override
    public boolean shouldExpandSuperTypes() {
        return this.expandSuperTypes;
    }

    public ConfigurationBuilder setExpandSuperTypes(boolean expandSuperTypes) {
        this.expandSuperTypes = expandSuperTypes;
        return this;
    }
}

