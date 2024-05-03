/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.vecmath;

class VecMathUtil {
    static int floatToIntBits(float f) {
        if (f == 0.0f) {
            return 0;
        }
        return Float.floatToIntBits(f);
    }

    static long doubleToLongBits(double d) {
        if (d == 0.0) {
            return 0L;
        }
        return Double.doubleToLongBits(d);
    }

    private VecMathUtil() {
    }
}

