/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.resolver.dns;

import io.netty.resolver.dns.DefaultDnsServerAddressStreamProvider;
import io.netty.resolver.dns.DnsServerAddressStream;
import io.netty.resolver.dns.DnsServerAddressStreamProvider;
import io.netty.resolver.dns.UnixResolverDnsServerAddressStreamProvider;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class DnsServerAddressStreamProviders {
    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(DnsServerAddressStreamProviders.class);
    private static final Constructor<? extends DnsServerAddressStreamProvider> STREAM_PROVIDER_CONSTRUCTOR;
    private static final String MACOS_PROVIDER_CLASS_NAME = "io.netty.resolver.dns.macos.MacOSDnsServerAddressStreamProvider";

    private DnsServerAddressStreamProviders() {
    }

    public static DnsServerAddressStreamProvider platformDefault() {
        if (STREAM_PROVIDER_CONSTRUCTOR != null) {
            try {
                return STREAM_PROVIDER_CONSTRUCTOR.newInstance(new Object[0]);
            } catch (IllegalAccessException illegalAccessException) {
            } catch (InstantiationException instantiationException) {
            } catch (InvocationTargetException invocationTargetException) {
                // empty catch block
            }
        }
        return DnsServerAddressStreamProviders.unixDefault();
    }

    public static DnsServerAddressStreamProvider unixDefault() {
        return DefaultProviderHolder.DEFAULT_DNS_SERVER_ADDRESS_STREAM_PROVIDER;
    }

    static {
        Constructor constructor = null;
        if (PlatformDependent.isOsx()) {
            try {
                Object maybeProvider = AccessController.doPrivileged(new PrivilegedAction<Object>(){

                    @Override
                    public Object run() {
                        try {
                            return Class.forName(DnsServerAddressStreamProviders.MACOS_PROVIDER_CLASS_NAME, true, DnsServerAddressStreamProviders.class.getClassLoader());
                        } catch (Throwable cause) {
                            return cause;
                        }
                    }
                });
                if (!(maybeProvider instanceof Class)) {
                    throw (Throwable)maybeProvider;
                }
                Class providerClass = (Class)maybeProvider;
                constructor = providerClass.getConstructor(new Class[0]);
                constructor.newInstance(new Object[0]);
                LOGGER.debug("{}: available", (Object)MACOS_PROVIDER_CLASS_NAME);
            } catch (ClassNotFoundException cause) {
                LOGGER.warn("Can not find {} in the classpath, fallback to system defaults. This may result in incorrect DNS resolutions on MacOS.", (Object)MACOS_PROVIDER_CLASS_NAME);
            } catch (Throwable cause) {
                LOGGER.error("Unable to load {}, fallback to system defaults. This may result in incorrect DNS resolutions on MacOS.", (Object)MACOS_PROVIDER_CLASS_NAME, (Object)cause);
                constructor = null;
            }
        }
        STREAM_PROVIDER_CONSTRUCTOR = constructor;
    }

    private static final class DefaultProviderHolder {
        private static final long REFRESH_INTERVAL = TimeUnit.MINUTES.toNanos(5L);
        static final DnsServerAddressStreamProvider DEFAULT_DNS_SERVER_ADDRESS_STREAM_PROVIDER = new DnsServerAddressStreamProvider(){
            private volatile DnsServerAddressStreamProvider currentProvider = this.provider();
            private final AtomicLong lastRefresh = new AtomicLong(System.nanoTime());

            @Override
            public DnsServerAddressStream nameServerAddressStream(String hostname) {
                long last = this.lastRefresh.get();
                DnsServerAddressStreamProvider current = this.currentProvider;
                if (System.nanoTime() - last > REFRESH_INTERVAL && this.lastRefresh.compareAndSet(last, System.nanoTime())) {
                    current = this.currentProvider = this.provider();
                }
                return current.nameServerAddressStream(hostname);
            }

            private DnsServerAddressStreamProvider provider() {
                return PlatformDependent.isWindows() ? DefaultDnsServerAddressStreamProvider.INSTANCE : UnixResolverDnsServerAddressStreamProvider.parseSilently();
            }
        };

        private DefaultProviderHolder() {
        }
    }
}

