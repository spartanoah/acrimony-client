/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.oracle.svm.core.annotate.Alias
 *  com.oracle.svm.core.annotate.InjectAccessors
 *  com.oracle.svm.core.annotate.TargetClass
 */
package io.netty.util;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.InjectAccessors;
import com.oracle.svm.core.annotate.TargetClass;
import io.netty.util.NetUtil;
import io.netty.util.NetUtilInitializations;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

@TargetClass(value=NetUtil.class)
final class NetUtilSubstitutions {
    @Alias
    @InjectAccessors(value=NetUtilLocalhost4Accessor.class)
    public static Inet4Address LOCALHOST4;
    @Alias
    @InjectAccessors(value=NetUtilLocalhost6Accessor.class)
    public static Inet6Address LOCALHOST6;
    @Alias
    @InjectAccessors(value=NetUtilLocalhostAccessor.class)
    public static InetAddress LOCALHOST;

    private NetUtilSubstitutions() {
    }

    private static final class NetUtilLocalhostLazyHolder {
        private static final InetAddress LOCALHOST = NetUtilInitializations.determineLoopback(NetUtilLocalhost4LazyHolder.access$000(), NetUtilLocalhost6LazyHolder.access$100()).address();

        private NetUtilLocalhostLazyHolder() {
        }
    }

    private static final class NetUtilLocalhostAccessor {
        private NetUtilLocalhostAccessor() {
        }

        static InetAddress get() {
            return NetUtilLocalhostLazyHolder.LOCALHOST;
        }

        static void set(InetAddress ignored) {
        }
    }

    private static final class NetUtilLocalhost6LazyHolder {
        private static final Inet6Address LOCALHOST6 = NetUtilInitializations.createLocalhost6();

        private NetUtilLocalhost6LazyHolder() {
        }
    }

    private static final class NetUtilLocalhost6Accessor {
        private NetUtilLocalhost6Accessor() {
        }

        static Inet6Address get() {
            return NetUtilLocalhost6LazyHolder.LOCALHOST6;
        }

        static void set(Inet6Address ignored) {
        }
    }

    private static final class NetUtilLocalhost4LazyHolder {
        private static final Inet4Address LOCALHOST4 = NetUtilInitializations.createLocalhost4();

        private NetUtilLocalhost4LazyHolder() {
        }
    }

    private static final class NetUtilLocalhost4Accessor {
        private NetUtilLocalhost4Accessor() {
        }

        static Inet4Address get() {
            return NetUtilLocalhost4LazyHolder.LOCALHOST4;
        }

        static void set(Inet4Address ignored) {
        }
    }
}

