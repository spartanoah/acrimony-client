/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.tuples;

import Acrimony.util.tuples.immutable.ImmutablePair;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public abstract class Pair<A, B>
implements Serializable {
    public static <A, B> Pair<A, B> of(A a, B b) {
        return ImmutablePair.of(a, b);
    }

    public static <A> Pair<A, A> of(A a) {
        return ImmutablePair.of(a, a);
    }

    public abstract A getFirst();

    public abstract B getSecond();

    public abstract <R> R apply(BiFunction<? super A, ? super B, ? extends R> var1);

    public abstract void use(BiConsumer<? super A, ? super B> var1);

    public int hashCode() {
        return Objects.hash(this.getFirst(), this.getSecond());
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof Pair) {
            Pair other = (Pair)that;
            return Objects.equals(this.getFirst(), other.getFirst()) && Objects.equals(this.getSecond(), other.getSecond());
        }
        return false;
    }
}

