/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal;

import io.netty.util.internal.Cleaner;
import io.netty.util.internal.PlatformDependent0;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

final class CleanerJava9
implements Cleaner {
    private static final InternalLogger logger;
    private static final Method INVOKE_CLEANER;

    CleanerJava9() {
    }

    static boolean isSupported() {
        return INVOKE_CLEANER != null;
    }

    @Override
    public void freeDirectBuffer(ByteBuffer buffer) {
        if (System.getSecurityManager() == null) {
            try {
                INVOKE_CLEANER.invoke(PlatformDependent0.UNSAFE, buffer);
            } catch (Throwable cause) {
                PlatformDependent0.throwException(cause);
            }
        } else {
            CleanerJava9.freeDirectBufferPrivileged(buffer);
        }
    }

    private static void freeDirectBufferPrivileged(final ByteBuffer buffer) {
        Exception error = AccessController.doPrivileged(new PrivilegedAction<Exception>(){

            @Override
            public Exception run() {
                try {
                    INVOKE_CLEANER.invoke(PlatformDependent0.UNSAFE, buffer);
                } catch (InvocationTargetException e) {
                    return e;
                } catch (IllegalAccessException e) {
                    return e;
                }
                return null;
            }
        });
        if (error != null) {
            PlatformDependent0.throwException(error);
        }
    }

    static {
        Throwable error;
        Method method;
        logger = InternalLoggerFactory.getInstance(CleanerJava9.class);
        if (PlatformDependent0.hasUnsafe()) {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(1);
            Object maybeInvokeMethod = AccessController.doPrivileged(new PrivilegedAction<Object>(){

                @Override
                public Object run() {
                    try {
                        Method m = PlatformDependent0.UNSAFE.getClass().getDeclaredMethod("invokeCleaner", ByteBuffer.class);
                        m.invoke(PlatformDependent0.UNSAFE, buffer);
                        return m;
                    } catch (NoSuchMethodException e) {
                        return e;
                    } catch (InvocationTargetException e) {
                        return e;
                    } catch (IllegalAccessException e) {
                        return e;
                    }
                }
            });
            if (maybeInvokeMethod instanceof Throwable) {
                method = null;
                error = (Throwable)maybeInvokeMethod;
            } else {
                method = (Method)maybeInvokeMethod;
                error = null;
            }
        } else {
            method = null;
            error = new UnsupportedOperationException("sun.misc.Unsafe unavailable");
        }
        if (error == null) {
            logger.debug("java.nio.ByteBuffer.cleaner(): available");
        } else {
            logger.debug("java.nio.ByteBuffer.cleaner(): unavailable", error);
        }
        INVOKE_CLEANER = method;
    }
}

