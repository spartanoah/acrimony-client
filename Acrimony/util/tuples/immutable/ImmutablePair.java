/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.tuples.immutable;

import Acrimony.util.tuples.Pair;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public final class ImmutablePair<A, B>
extends Pair<A, B> {
    private final A a;
    private final B b;

    ImmutablePair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public static <A, B> ImmutablePair<A, B> of(A a, B b) {
        return new ImmutablePair<A, B>(a, b);
    }

    public Pair<A, A> pairOfFirst() {
        return Pair.of(this.a);
    }

    public Pair<B, B> pairOfSecond() {
        return Pair.of(this.b);
    }

    @Override
    public A getFirst() {
        return this.a;
    }

    @Override
    public B getSecond() {
        return this.b;
    }

    @Override
    public <R> R apply(BiFunction<? super A, ? super B, ? extends R> func) {
        return func.apply(this.a, this.b);
    }

    @Override
    public void use(BiConsumer<? super A, ? super B> func) {
        func.accept(this.a, this.b);
    }
}

