/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.tuples.immutable;

import Acrimony.util.tuples.Unit;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ImmutableUnit<A>
extends Unit<A> {
    private final A a;

    ImmutableUnit(A a) {
        this.a = a;
    }

    public static <A> ImmutableUnit<A> of(A a) {
        return new ImmutableUnit<A>(a);
    }

    @Override
    public A get() {
        return this.a;
    }

    @Override
    public <R> R apply(Function<? super A, ? extends R> func) {
        return func.apply(this.a);
    }

    @Override
    public void use(Consumer<? super A> func) {
        func.accept(this.a);
    }
}

