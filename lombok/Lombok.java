/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package lombok;

public class Lombok {
    public static RuntimeException sneakyThrow(Throwable t) {
        if (t == null) {
            throw new NullPointerException("t");
        }
        return (RuntimeException)Lombok.sneakyThrow0(t);
    }

    private static <T extends Throwable> T sneakyThrow0(Throwable t) throws T {
        throw t;
    }

    public static <T> T preventNullAnalysis(T value) {
        return value;
    }

    public static <T> T checkNotNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }
}

