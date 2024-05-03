/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal;

import io.netty.util.internal.Cleaner;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.PlatformDependent0;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

final class CleanerJava6
implements Cleaner {
    private static final long CLEANER_FIELD_OFFSET;
    private static final Method CLEAN_METHOD;
    private static final Field CLEANER_FIELD;
    private static final InternalLogger logger;

    CleanerJava6() {
    }

    static boolean isSupported() {
        return CLEANER_FIELD_OFFSET != -1L || CLEANER_FIELD != null;
    }

    @Override
    public void freeDirectBuffer(ByteBuffer buffer) {
        if (!buffer.isDirect()) {
            return;
        }
        if (System.getSecurityManager() == null) {
            try {
                CleanerJava6.freeDirectBuffer0(buffer);
            } catch (Throwable cause) {
                PlatformDependent0.throwException(cause);
            }
        } else {
            CleanerJava6.freeDirectBufferPrivileged(buffer);
        }
    }

    private static void freeDirectBufferPrivileged(final ByteBuffer buffer) {
        Throwable cause = AccessController.doPrivileged(new PrivilegedAction<Throwable>(){

            @Override
            public Throwable run() {
                try {
                    CleanerJava6.freeDirectBuffer0(buffer);
                    return null;
                } catch (Throwable cause) {
                    return cause;
                }
            }
        });
        if (cause != null) {
            PlatformDependent0.throwException(cause);
        }
    }

    private static void freeDirectBuffer0(ByteBuffer buffer) throws Exception {
        Object cleaner = CLEANER_FIELD_OFFSET == -1L ? CLEANER_FIELD.get(buffer) : PlatformDependent0.getObject(buffer, CLEANER_FIELD_OFFSET);
        if (cleaner != null) {
            CLEAN_METHOD.invoke(cleaner, new Object[0]);
        }
    }

    static {
        Method clean;
        long fieldOffset;
        Field cleanerField;
        logger = InternalLoggerFactory.getInstance(CleanerJava6.class);
        Throwable error = null;
        final ByteBuffer direct = ByteBuffer.allocateDirect(1);
        try {
            Object cleaner;
            Object mayBeCleanerField = AccessController.doPrivileged(new PrivilegedAction<Object>(){

                @Override
                public Object run() {
                    try {
                        Field cleanerField = direct.getClass().getDeclaredField("cleaner");
                        if (!PlatformDependent.hasUnsafe()) {
                            cleanerField.setAccessible(true);
                        }
                        return cleanerField;
                    } catch (Throwable cause) {
                        return cause;
                    }
                }
            });
            if (mayBeCleanerField instanceof Throwable) {
                throw (Throwable)mayBeCleanerField;
            }
            cleanerField = (Field)mayBeCleanerField;
            if (PlatformDependent.hasUnsafe()) {
                fieldOffset = PlatformDependent0.objectFieldOffset(cleanerField);
                cleaner = PlatformDependent0.getObject(direct, fieldOffset);
            } else {
                fieldOffset = -1L;
                cleaner = cleanerField.get(direct);
            }
            clean = cleaner.getClass().getDeclaredMethod("clean", new Class[0]);
            clean.invoke(cleaner, new Object[0]);
        } catch (Throwable t) {
            fieldOffset = -1L;
            clean = null;
            error = t;
            cleanerField = null;
        }
        if (error == null) {
            logger.debug("java.nio.ByteBuffer.cleaner(): available");
        } else {
            logger.debug("java.nio.ByteBuffer.cleaner(): unavailable", error);
        }
        CLEANER_FIELD = cleanerField;
        CLEANER_FIELD_OFFSET = fieldOffset;
        CLEAN_METHOD = clean;
    }
}

