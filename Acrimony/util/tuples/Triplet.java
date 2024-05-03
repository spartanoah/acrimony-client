/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.tuples;

import Acrimony.util.tuples.immutable.ImmutableTriplet;
import java.io.Serializable;
import java.util.Objects;

public abstract class Triplet<A, B, C>
implements Serializable {
    public static <A, B, C> Triplet<A, B, C> of(A a, B b, C c) {
        return ImmutableTriplet.of(a, b, c);
    }

    public static <A> Triplet<A, A, A> of(A a) {
        return ImmutableTriplet.of(a, a, a);
    }

    public abstract A getFirst();

    public abstract B getSecond();

    public abstract C getThird();

    public abstract <R> R apply(TriFunction<? super A, ? super B, ? super C, ? extends R> var1);

    public abstract void use(TriConsumer<? super A, ? super B, ? super C> var1);

    public int hashCode() {
        return Objects.hash(this.getFirst(), this.getSecond(), this.getThird());
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof Triplet) {
            Triplet other = (Triplet)that;
            return Objects.equals(this.getFirst(), other.getFirst()) && Objects.equals(this.getSecond(), other.getSecond()) && Objects.equals(this.getThird(), other.getThird());
        }
        return false;
    }

    public static interface TriConsumer<T, U, V> {
        public void accept(T var1, U var2, V var3);
    }

    public static interface TriFunction<T, U, V, R> {
        public R apply(T var1, U var2, V var3);
    }
}

