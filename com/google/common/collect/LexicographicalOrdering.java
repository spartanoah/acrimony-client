/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.Ordering;
import java.io.Serializable;
import java.util.Iterator;
import javax.annotation.Nullable;

@GwtCompatible(serializable=true)
final class LexicographicalOrdering<T>
extends Ordering<Iterable<T>>
implements Serializable {
    final Ordering<? super T> elementOrder;
    private static final long serialVersionUID = 0L;

    LexicographicalOrdering(Ordering<? super T> elementOrder) {
        this.elementOrder = elementOrder;
    }

    @Override
    public int compare(Iterable<T> leftIterable, Iterable<T> rightIterable) {
        Iterator<T> left = leftIterable.iterator();
        Iterator<T> right = rightIterable.iterator();
        while (left.hasNext()) {
            if (!right.hasNext()) {
                return 1;
            }
            int result = this.elementOrder.compare(left.next(), right.next());
            if (result == 0) continue;
            return result;
        }
        if (right.hasNext()) {
            return -1;
        }
        return 0;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof LexicographicalOrdering) {
            LexicographicalOrdering that = (LexicographicalOrdering)object;
            return this.elementOrder.equals(that.elementOrder);
        }
        return false;
    }

    public int hashCode() {
        return this.elementOrder.hashCode() ^ 0x7BB78CF5;
    }

    public String toString() {
        return this.elementOrder + ".lexicographical()";
    }
}

