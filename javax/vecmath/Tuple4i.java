/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.vecmath;

import java.io.Serializable;

public abstract class Tuple4i
implements Serializable,
Cloneable {
    static final long serialVersionUID = 8064614250942616720L;
    public int x;
    public int y;
    public int z;
    public int w;

    public Tuple4i(int x, int y, int z, int w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Tuple4i(int[] t) {
        this.x = t[0];
        this.y = t[1];
        this.z = t[2];
        this.w = t[3];
    }

    public Tuple4i(Tuple4i t1) {
        this.x = t1.x;
        this.y = t1.y;
        this.z = t1.z;
        this.w = t1.w;
    }

    public Tuple4i() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.w = 0;
    }

    public final void set(int x, int y, int z, int w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public final void set(int[] t) {
        this.x = t[0];
        this.y = t[1];
        this.z = t[2];
        this.w = t[3];
    }

    public final void set(Tuple4i t1) {
        this.x = t1.x;
        this.y = t1.y;
        this.z = t1.z;
        this.w = t1.w;
    }

    public final void get(int[] t) {
        t[0] = this.x;
        t[1] = this.y;
        t[2] = this.z;
        t[3] = this.w;
    }

    public final void get(Tuple4i t) {
        t.x = this.x;
        t.y = this.y;
        t.z = this.z;
        t.w = this.w;
    }

    public final void add(Tuple4i t1, Tuple4i t2) {
        this.x = t1.x + t2.x;
        this.y = t1.y + t2.y;
        this.z = t1.z + t2.z;
        this.w = t1.w + t2.w;
    }

    public final void add(Tuple4i t1) {
        this.x += t1.x;
        this.y += t1.y;
        this.z += t1.z;
        this.w += t1.w;
    }

    public final void sub(Tuple4i t1, Tuple4i t2) {
        this.x = t1.x - t2.x;
        this.y = t1.y - t2.y;
        this.z = t1.z - t2.z;
        this.w = t1.w - t2.w;
    }

    public final void sub(Tuple4i t1) {
        this.x -= t1.x;
        this.y -= t1.y;
        this.z -= t1.z;
        this.w -= t1.w;
    }

    public final void negate(Tuple4i t1) {
        this.x = -t1.x;
        this.y = -t1.y;
        this.z = -t1.z;
        this.w = -t1.w;
    }

    public final void negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        this.w = -this.w;
    }

    public final void scale(int s, Tuple4i t1) {
        this.x = s * t1.x;
        this.y = s * t1.y;
        this.z = s * t1.z;
        this.w = s * t1.w;
    }

    public final void scale(int s) {
        this.x *= s;
        this.y *= s;
        this.z *= s;
        this.w *= s;
    }

    public final void scaleAdd(int s, Tuple4i t1, Tuple4i t2) {
        this.x = s * t1.x + t2.x;
        this.y = s * t1.y + t2.y;
        this.z = s * t1.z + t2.z;
        this.w = s * t1.w + t2.w;
    }

    public final void scaleAdd(int s, Tuple4i t1) {
        this.x = s * this.x + t1.x;
        this.y = s * this.y + t1.y;
        this.z = s * this.z + t1.z;
        this.w = s * this.w + t1.w;
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + ")";
    }

    public boolean equals(Object t1) {
        try {
            Tuple4i t2 = (Tuple4i)t1;
            return this.x == t2.x && this.y == t2.y && this.z == t2.z && this.w == t2.w;
        } catch (NullPointerException e2) {
            return false;
        } catch (ClassCastException e1) {
            return false;
        }
    }

    public int hashCode() {
        long bits = 1L;
        bits = 31L * bits + (long)this.x;
        bits = 31L * bits + (long)this.y;
        bits = 31L * bits + (long)this.z;
        bits = 31L * bits + (long)this.w;
        return (int)(bits ^ bits >> 32);
    }

    public final void clamp(int min, int max, Tuple4i t) {
        this.x = t.x > max ? max : (t.x < min ? min : t.x);
        this.y = t.y > max ? max : (t.y < min ? min : t.y);
        this.z = t.z > max ? max : (t.z < min ? min : t.z);
        this.w = t.w > max ? max : (t.w < min ? min : t.w);
    }

    public final void clampMin(int min, Tuple4i t) {
        this.x = t.x < min ? min : t.x;
        this.y = t.y < min ? min : t.y;
        this.z = t.z < min ? min : t.z;
        this.w = t.w < min ? min : t.w;
    }

    public final void clampMax(int max, Tuple4i t) {
        this.x = t.x > max ? max : t.x;
        this.y = t.y > max ? max : t.y;
        this.z = t.z > max ? max : t.z;
        this.w = t.w > max ? max : t.z;
    }

    public final void absolute(Tuple4i t) {
        this.x = Math.abs(t.x);
        this.y = Math.abs(t.y);
        this.z = Math.abs(t.z);
        this.w = Math.abs(t.w);
    }

    public final void clamp(int min, int max) {
        if (this.x > max) {
            this.x = max;
        } else if (this.x < min) {
            this.x = min;
        }
        if (this.y > max) {
            this.y = max;
        } else if (this.y < min) {
            this.y = min;
        }
        if (this.z > max) {
            this.z = max;
        } else if (this.z < min) {
            this.z = min;
        }
        if (this.w > max) {
            this.w = max;
        } else if (this.w < min) {
            this.w = min;
        }
    }

    public final void clampMin(int min) {
        if (this.x < min) {
            this.x = min;
        }
        if (this.y < min) {
            this.y = min;
        }
        if (this.z < min) {
            this.z = min;
        }
        if (this.w < min) {
            this.w = min;
        }
    }

    public final void clampMax(int max) {
        if (this.x > max) {
            this.x = max;
        }
        if (this.y > max) {
            this.y = max;
        }
        if (this.z > max) {
            this.z = max;
        }
        if (this.w > max) {
            this.w = max;
        }
    }

    public final void absolute() {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);
        this.w = Math.abs(this.w);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    public final int getX() {
        return this.x;
    }

    public final void setX(int x) {
        this.x = x;
    }

    public final int getY() {
        return this.y;
    }

    public final void setY(int y) {
        this.y = y;
    }

    public final int getZ() {
        return this.z;
    }

    public final void setZ(int z) {
        this.z = z;
    }

    public final int getW() {
        return this.w;
    }

    public final void setW(int w) {
        this.w = w;
    }
}

