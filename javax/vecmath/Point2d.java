/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.vecmath;

import java.io.Serializable;
import javax.vecmath.Point2f;
import javax.vecmath.Tuple2d;
import javax.vecmath.Tuple2f;

public class Point2d
extends Tuple2d
implements Serializable {
    static final long serialVersionUID = 1133748791492571954L;

    public Point2d(double x, double y) {
        super(x, y);
    }

    public Point2d(double[] p) {
        super(p);
    }

    public Point2d(Point2d p1) {
        super(p1);
    }

    public Point2d(Point2f p1) {
        super(p1);
    }

    public Point2d(Tuple2d t1) {
        super(t1);
    }

    public Point2d(Tuple2f t1) {
        super(t1);
    }

    public Point2d() {
    }

    public final double distanceSquared(Point2d p1) {
        double dx = this.x - p1.x;
        double dy = this.y - p1.y;
        return dx * dx + dy * dy;
    }

    public final double distance(Point2d p1) {
        double dx = this.x - p1.x;
        double dy = this.y - p1.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public final double distanceL1(Point2d p1) {
        return Math.abs(this.x - p1.x) + Math.abs(this.y - p1.y);
    }

    public final double distanceLinf(Point2d p1) {
        return Math.max(Math.abs(this.x - p1.x), Math.abs(this.y - p1.y));
    }
}

