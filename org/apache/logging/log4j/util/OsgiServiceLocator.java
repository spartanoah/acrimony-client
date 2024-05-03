/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.osgi.framework.Bundle
 *  org.osgi.framework.BundleContext
 *  org.osgi.framework.FrameworkUtil
 */
package org.apache.logging.log4j.util;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LowLevelLogUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class OsgiServiceLocator {
    private static final boolean OSGI_AVAILABLE = OsgiServiceLocator.checkOsgiAvailable();

    private static boolean checkOsgiAvailable() {
        try {
            Class.forName("org.osgi.framework.Bundle");
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        } catch (Throwable e) {
            LowLevelLogUtil.logException("Unknown error checking for existence of class: org.osgi.framework.Bundle", e);
            return false;
        }
    }

    public static boolean isAvailable() {
        return OSGI_AVAILABLE;
    }

    public static <T> Stream<T> loadServices(Class<T> serviceType, MethodHandles.Lookup lookup) {
        return OsgiServiceLocator.loadServices(serviceType, lookup, true);
    }

    public static <T> Stream<T> loadServices(Class<T> serviceType, MethodHandles.Lookup lookup, boolean verbose) {
        block3: {
            Bundle bundle = FrameworkUtil.getBundle(lookup.lookupClass());
            if (bundle != null) {
                BundleContext ctx = bundle.getBundleContext();
                try {
                    return ctx.getServiceReferences(serviceType, null).stream().map(arg_0 -> ((BundleContext)ctx).getService(arg_0));
                } catch (Throwable e) {
                    if (!verbose) break block3;
                    StatusLogger.getLogger().error("Unable to load OSGI services for service {}", (Object)serviceType, (Object)e);
                }
            }
        }
        return Stream.empty();
    }
}

