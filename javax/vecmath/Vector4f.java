/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.vecmath;

import java.io.Serializable;
import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple4d;
import javax.vecmath.Tuple4f;
import javax.vecmath.Vector4d;

public class Vector4f
extends Tuple4f
implements Serializable {
    static final long serialVersionUID = 8749319902347760659L;

    public Vector4f(float x, float y, float z, float w) {
        super(x, y, z, w);
    }

    public Vector4f(float[] v) {
        super(v);
    }

    public Vector4f(Vector4f v1) {
        super(v1);
    }

    public Vector4f(Vector4d v1) {
        super(v1);
    }

    public Vector4f(Tuple4f t1) {
        super(t1);
    }

    public Vector4f(Tuple4d t1) {
        super(t1);
    }

    public Vector4f(Tuple3f t1) {
        super(t1.x, t1.y, t1.z, 0.0f);
    }

    public Vector4f() {
    }

    public final void set(Tuple3f t1) {
        this.x = t1.x;
        this.y = t1.y;
        this.z = t1.z;
        this.w = 0.0f;
    }

    public final float length() {
        return (float)Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w);
    }

    public final float lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w;
    }

    public final float dot(Vector4f v1) {
        return this.x * v1.x + this.y * v1.y + this.z * v1.z + this.w * v1.w;
    }

    public final void normalize(Vector4f v1) {
        float norm = (float)(1.0 / Math.sqrt(v1.x * v1.x + v1.y * v1.y + v1.z * v1.z + v1.w * v1.w));
        this.x = v1.x * norm;
        this.y = v1.y * norm;
        this.z = v1.z * norm;
        this.w = v1.w * norm;
    }

    public final void normalize() {
        float norm = (float)(1.0 / Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w));
        this.x *= norm;
        this.y *= norm;
        this.z *= norm;
        this.w *= norm;
    }

    public final float angle(Vector4f v1) {
        double vDot = this.dot(v1) / (this.length() * v1.length());
        if (vDot < -1.0) {
            vDot = -1.0;
        }
        if (vDot > 1.0) {
            vDot = 1.0;
        }
        return (float)Math.acos(vDot);
    }
}

