/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.tuples.mutable;

import Acrimony.util.tuples.Unit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class MutableUnit<A>
extends Unit<A> {
    private A a;

    MutableUnit(A a) {
        this.a = a;
    }

    public static <A> MutableUnit<A> of(A a) {
        return new MutableUnit<A>(a);
    }

    @Override
    public A get() {
        return this.a;
    }

    public void set(A a) {
        this.a = a;
    }

    @Override
    public <R> R apply(Function<? super A, ? extends R> func) {
        return func.apply(this.a);
    }

    @Override
    public void use(Consumer<? super A> func) {
        func.accept(this.a);
    }

    public void compute(UnaryOperator<A> mapper) {
        this.a = mapper.apply(this.a);
    }
}

