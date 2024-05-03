/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.util;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.Provider;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.ServiceLoaderUtil;

public final class ProviderUtil {
    protected static final String PROVIDER_RESOURCE = "META-INF/log4j-provider.properties";
    protected static final Collection<Provider> PROVIDERS = new HashSet<Provider>();
    protected static final Lock STARTUP_LOCK = new ReentrantLock();
    private static final String API_VERSION = "Log4jAPIVersion";
    private static final String[] COMPATIBLE_API_VERSIONS = new String[]{"2.6.0"};
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static volatile ProviderUtil instance;

    private ProviderUtil() {
        ServiceLoaderUtil.loadServices(Provider.class, MethodHandles.lookup(), false).filter(provider -> ProviderUtil.validVersion(provider.getVersions())).forEach(PROVIDERS::add);
        for (LoaderUtil.UrlResource resource : LoaderUtil.findUrlResources(PROVIDER_RESOURCE, false)) {
            ProviderUtil.loadProvider(resource.getUrl(), resource.getClassLoader());
        }
    }

    protected static void addProvider(Provider provider) {
        PROVIDERS.add(provider);
        LOGGER.debug("Loaded Provider {}", (Object)provider);
    }

    protected static void loadProvider(URL url, ClassLoader cl) {
        try {
            Properties props = PropertiesUtil.loadClose(url.openStream(), url);
            if (ProviderUtil.validVersion(props.getProperty(API_VERSION))) {
                Provider provider = new Provider(props, url, cl);
                PROVIDERS.add(provider);
                LOGGER.debug("Loaded Provider {}", (Object)provider);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to open {}", (Object)url, (Object)e);
        }
    }

    protected static void loadProviders(ClassLoader classLoader) {
        ServiceLoaderUtil.loadClassloaderServices(Provider.class, MethodHandles.lookup(), classLoader, true).filter(provider -> ProviderUtil.validVersion(provider.getVersions())).forEach(PROVIDERS::add);
    }

    @Deprecated
    protected static void loadProviders(Enumeration<URL> urls, ClassLoader cl) {
        if (urls != null) {
            while (urls.hasMoreElements()) {
                ProviderUtil.loadProvider(urls.nextElement(), cl);
            }
        }
    }

    public static Iterable<Provider> getProviders() {
        ProviderUtil.lazyInit();
        return PROVIDERS;
    }

    public static boolean hasProviders() {
        ProviderUtil.lazyInit();
        return !PROVIDERS.isEmpty();
    }

    protected static void lazyInit() {
        if (instance == null) {
            try {
                STARTUP_LOCK.lockInterruptibly();
                try {
                    if (instance == null) {
                        instance = new ProviderUtil();
                    }
                } finally {
                    STARTUP_LOCK.unlock();
                }
            } catch (InterruptedException e) {
                LOGGER.fatal("Interrupted before Log4j Providers could be loaded.", (Throwable)e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public static ClassLoader findClassLoader() {
        return LoaderUtil.getThreadContextClassLoader();
    }

    private static boolean validVersion(String version) {
        for (String v : COMPATIBLE_API_VERSIONS) {
            if (!version.startsWith(v)) continue;
            return true;
        }
        return false;
    }
}

