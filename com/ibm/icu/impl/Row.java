/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.impl;

import com.ibm.icu.impl.Utility;
import com.ibm.icu.util.Freezable;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Row<C0, C1, C2, C3, C4>
implements Comparable,
Cloneable,
Freezable<Row<C0, C1, C2, C3, C4>> {
    protected Object[] items;
    protected boolean frozen;

    public static <C0, C1> R2<C0, C1> of(C0 p0, C1 p1) {
        return new R2<C0, C1>(p0, p1);
    }

    public static <C0, C1, C2> R3<C0, C1, C2> of(C0 p0, C1 p1, C2 p2) {
        return new R3<C0, C1, C2>(p0, p1, p2);
    }

    public static <C0, C1, C2, C3> R4<C0, C1, C2, C3> of(C0 p0, C1 p1, C2 p2, C3 p3) {
        return new R4<C0, C1, C2, C3>(p0, p1, p2, p3);
    }

    public static <C0, C1, C2, C3, C4> R5<C0, C1, C2, C3, C4> of(C0 p0, C1 p1, C2 p2, C3 p3, C4 p4) {
        return new R5<C0, C1, C2, C3, C4>(p0, p1, p2, p3, p4);
    }

    public Row<C0, C1, C2, C3, C4> set0(C0 item) {
        return this.set(0, item);
    }

    public C0 get0() {
        return (C0)this.items[0];
    }

    public Row<C0, C1, C2, C3, C4> set1(C1 item) {
        return this.set(1, item);
    }

    public C1 get1() {
        return (C1)this.items[1];
    }

    public Row<C0, C1, C2, C3, C4> set2(C2 item) {
        return this.set(2, item);
    }

    public C2 get2() {
        return (C2)this.items[2];
    }

    public Row<C0, C1, C2, C3, C4> set3(C3 item) {
        return this.set(3, item);
    }

    public C3 get3() {
        return (C3)this.items[3];
    }

    public Row<C0, C1, C2, C3, C4> set4(C4 item) {
        return this.set(4, item);
    }

    public C4 get4() {
        return (C4)this.items[4];
    }

    protected Row<C0, C1, C2, C3, C4> set(int i, Object item) {
        if (this.frozen) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        this.items[i] = item;
        return this;
    }

    public int hashCode() {
        int sum = this.items.length;
        for (Object item : this.items) {
            sum = sum * 37 + Utility.checkHash(item);
        }
        return sum;
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        try {
            Row that = (Row)other;
            if (this.items.length != that.items.length) {
                return false;
            }
            int i = 0;
            for (Object item : this.items) {
                if (Utility.objectEquals(item, that.items[i++])) continue;
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int compareTo(Object other) {
        Row that = (Row)other;
        int result = this.items.length - that.items.length;
        if (result != 0) {
            return result;
        }
        int i = 0;
        for (Object item : this.items) {
            if ((result = Utility.checkCompare((Comparable)item, (Comparable)that.items[i++])) == 0) continue;
            return result;
        }
        return 0;
    }

    public String toString() {
        StringBuilder result = new StringBuilder("[");
        boolean first = true;
        for (Object item : this.items) {
            if (first) {
                first = false;
            } else {
                result.append(", ");
            }
            result.append(item);
        }
        return result.append("]").toString();
    }

    @Override
    public boolean isFrozen() {
        return this.frozen;
    }

    @Override
    public Row<C0, C1, C2, C3, C4> freeze() {
        this.frozen = true;
        return this;
    }

    public Object clone() {
        if (this.frozen) {
            return this;
        }
        try {
            Row result = (Row)super.clone();
            this.items = (Object[])this.items.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public Row<C0, C1, C2, C3, C4> cloneAsThawed() {
        try {
            Row result = (Row)super.clone();
            this.items = (Object[])this.items.clone();
            result.frozen = false;
            return result;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static class R5<C0, C1, C2, C3, C4>
    extends Row<C0, C1, C2, C3, C4> {
        public R5(C0 a, C1 b, C2 c, C3 d, C4 e) {
            this.items = new Object[]{a, b, c, d, e};
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static class R4<C0, C1, C2, C3>
    extends Row<C0, C1, C2, C3, C3> {
        public R4(C0 a, C1 b, C2 c, C3 d) {
            this.items = new Object[]{a, b, c, d};
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static class R3<C0, C1, C2>
    extends Row<C0, C1, C2, C2, C2> {
        public R3(C0 a, C1 b, C2 c) {
            this.items = new Object[]{a, b, c};
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static class R2<C0, C1>
    extends Row<C0, C1, C1, C1, C1> {
        public R2(C0 a, C1 b) {
            this.items = new Object[]{a, b};
        }
    }
}

