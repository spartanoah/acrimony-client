/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.OsgiServiceLocator;

public final class ServiceLoaderUtil {
    private static final MethodType LOAD_CLASS_CLASSLOADER = MethodType.methodType(ServiceLoader.class, Class.class, ClassLoader.class);

    private ServiceLoaderUtil() {
    }

    public static <T> Stream<T> loadServices(Class<T> serviceType, MethodHandles.Lookup lookup) {
        return ServiceLoaderUtil.loadServices(serviceType, lookup, false);
    }

    public static <T> Stream<T> loadServices(Class<T> serviceType, MethodHandles.Lookup lookup, boolean useTccl) {
        return ServiceLoaderUtil.loadServices(serviceType, lookup, useTccl, true);
    }

    static <T> Stream<T> loadServices(Class<T> serviceType, MethodHandles.Lookup lookup, boolean useTccl, boolean verbose) {
        ClassLoader contextClassLoader;
        ClassLoader classLoader = lookup.lookupClass().getClassLoader();
        Stream<Object> services = ServiceLoaderUtil.loadClassloaderServices(serviceType, lookup, classLoader, verbose);
        if (useTccl && (contextClassLoader = LoaderUtil.getThreadContextClassLoader()) != classLoader) {
            services = Stream.concat(services, ServiceLoaderUtil.loadClassloaderServices(serviceType, lookup, contextClassLoader, verbose));
        }
        if (OsgiServiceLocator.isAvailable()) {
            services = Stream.concat(services, OsgiServiceLocator.loadServices(serviceType, lookup, verbose));
        }
        HashSet classes = new HashSet();
        return services.filter(service -> classes.add(service.getClass()));
    }

    static <T> Stream<T> loadClassloaderServices(Class<T> serviceType, MethodHandles.Lookup lookup, ClassLoader classLoader, boolean verbose) {
        return StreamSupport.stream(new ServiceLoaderSpliterator<T>(serviceType, lookup, classLoader, verbose), false);
    }

    static <T> Iterable<T> callServiceLoader(MethodHandles.Lookup lookup, Class<T> serviceType, ClassLoader classLoader, boolean verbose) {
        try {
            MethodHandle handle = lookup.findStatic(ServiceLoader.class, "load", LOAD_CLASS_CLASSLOADER);
            ServiceLoader serviceLoader = handle.invokeExact(serviceType, classLoader);
            return serviceLoader;
        } catch (Throwable e) {
            if (verbose) {
                StatusLogger.getLogger().error("Unable to load services for service {}", (Object)serviceType, (Object)e);
            }
            return Collections.emptyList();
        }
    }

    private static class ServiceLoaderSpliterator<S>
    implements Spliterator<S> {
        private final Iterator<S> serviceIterator;
        private final Logger logger;
        private final String serviceName;

        public ServiceLoaderSpliterator(Class<S> serviceType, MethodHandles.Lookup lookup, ClassLoader classLoader, boolean verbose) {
            this.serviceIterator = ServiceLoaderUtil.callServiceLoader(lookup, serviceType, classLoader, verbose).iterator();
            this.logger = verbose ? StatusLogger.getLogger() : null;
            this.serviceName = serviceType.toString();
        }

        @Override
        public boolean tryAdvance(Consumer<? super S> action) {
            while (this.serviceIterator.hasNext()) {
                try {
                    action.accept(this.serviceIterator.next());
                    return true;
                } catch (ServiceConfigurationError e) {
                    if (this.logger == null) continue;
                    this.logger.warn("Unable to load service class for service {}", (Object)this.serviceName, (Object)e);
                }
            }
            return false;
        }

        @Override
        public Spliterator<S> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return 1280;
        }
    }
}

