/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.util.internal;

import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SuppressJava6Requirement;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public final class ThrowableUtil {
    private ThrowableUtil() {
    }

    public static <T extends Throwable> T unknownStackTrace(T cause, Class<?> clazz, String method) {
        cause.setStackTrace(new StackTraceElement[]{new StackTraceElement(clazz.getName(), method, null, -1)});
        return cause;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String stackTraceToString(Throwable cause) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream(out);
        cause.printStackTrace(pout);
        pout.flush();
        try {
            String string = new String(out.toByteArray());
            return string;
        } finally {
            try {
                out.close();
            } catch (IOException iOException) {}
        }
    }

    public static boolean haveSuppressed() {
        return PlatformDependent.javaVersion() >= 7;
    }

    @SuppressJava6Requirement(reason="Throwable addSuppressed is only available for >= 7. Has check for < 7.")
    public static void addSuppressed(Throwable target, Throwable suppressed) {
        if (!ThrowableUtil.haveSuppressed()) {
            return;
        }
        target.addSuppressed(suppressed);
    }

    public static void addSuppressedAndClear(Throwable target, List<Throwable> suppressed) {
        ThrowableUtil.addSuppressed(target, suppressed);
        suppressed.clear();
    }

    public static void addSuppressed(Throwable target, List<Throwable> suppressed) {
        for (Throwable t : suppressed) {
            ThrowableUtil.addSuppressed(target, t);
        }
    }
}

