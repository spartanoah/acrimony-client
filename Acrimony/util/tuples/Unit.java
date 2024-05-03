/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.tuples;

import Acrimony.util.tuples.immutable.ImmutableUnit;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Unit<A>
implements Serializable {
    public static <A> Unit<A> of(A a) {
        return ImmutableUnit.of(a);
    }

    public abstract A get();

    public abstract <R> R apply(Function<? super A, ? extends R> var1);

    public abstract void use(Consumer<? super A> var1);

    public int hashCode() {
        return Objects.hash(this.get());
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof Unit) {
            Unit other = (Unit)that;
            return Objects.equals(this.get(), other.get());
        }
        return false;
    }
}

