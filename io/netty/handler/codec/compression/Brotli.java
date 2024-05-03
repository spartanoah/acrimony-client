/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  com.aayushatharva.brotli4j.Brotli4jLoader
 */
package io.netty.handler.codec.compression;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public final class Brotli {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Brotli.class);
    private static final ClassNotFoundException CNFE;
    private static Throwable cause;

    public static boolean isAvailable() {
        return CNFE == null && Brotli4jLoader.isAvailable();
    }

    public static void ensureAvailability() throws Throwable {
        if (CNFE != null) {
            throw CNFE;
        }
        Brotli4jLoader.ensureAvailability();
    }

    public static Throwable cause() {
        return cause;
    }

    private Brotli() {
    }

    static {
        ClassNotFoundException cnfe = null;
        try {
            Class.forName("com.aayushatharva.brotli4j.Brotli4jLoader", false, PlatformDependent.getClassLoader(Brotli.class));
        } catch (ClassNotFoundException t) {
            cnfe = t;
            logger.debug("brotli4j not in the classpath; Brotli support will be unavailable.");
        }
        CNFE = cnfe;
        if (cnfe == null && (cause = Brotli4jLoader.getUnavailabilityCause()) != null) {
            logger.debug("Failed to load brotli4j; Brotli support will be unavailable.", cause);
        }
    }
}

