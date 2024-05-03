/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.vecmath;

import java.io.Serializable;

public abstract class Tuple3b
implements Serializable,
Cloneable {
    static final long serialVersionUID = -483782685323607044L;
    public byte x;
    public byte y;
    public byte z;

    public Tuple3b(byte b1, byte b2, byte b3) {
        this.x = b1;
        this.y = b2;
        this.z = b3;
    }

    public Tuple3b(byte[] t) {
        this.x = t[0];
        this.y = t[1];
        this.z = t[2];
    }

    public Tuple3b(Tuple3b t1) {
        this.x = t1.x;
        this.y = t1.y;
        this.z = t1.z;
    }

    public Tuple3b() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public String toString() {
        return "(" + (this.x & 0xFF) + ", " + (this.y & 0xFF) + ", " + (this.z & 0xFF) + ")";
    }

    public final void get(byte[] t) {
        t[0] = this.x;
        t[1] = this.y;
        t[2] = this.z;
    }

    public final void get(Tuple3b t1) {
        t1.x = this.x;
        t1.y = this.y;
        t1.z = this.z;
    }

    public final void set(Tuple3b t1) {
        this.x = t1.x;
        this.y = t1.y;
        this.z = t1.z;
    }

    public final void set(byte[] t) {
        this.x = t[0];
        this.y = t[1];
        this.z = t[2];
    }

    public boolean equals(Tuple3b t1) {
        try {
            return this.x == t1.x && this.y == t1.y && this.z == t1.z;
        } catch (NullPointerException e2) {
            return false;
        }
    }

    public boolean equals(Object t1) {
        try {
            Tuple3b t2 = (Tuple3b)t1;
            return this.x == t2.x && this.y == t2.y && this.z == t2.z;
        } catch (NullPointerException e2) {
            return false;
        } catch (ClassCastException e1) {
            return false;
        }
    }

    public int hashCode() {
        return (this.x & 0xFF) << 0 | (this.y & 0xFF) << 8 | (this.z & 0xFF) << 16;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    public final byte getX() {
        return this.x;
    }

    public final void setX(byte x) {
        this.x = x;
    }

    public final byte getY() {
        return this.y;
    }

    public final void setY(byte y) {
        this.y = y;
    }

    public final byte getZ() {
        return this.z;
    }

    public final void setZ(byte z) {
        this.z = z;
    }
}

