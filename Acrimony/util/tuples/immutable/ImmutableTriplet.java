/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.tuples.immutable;

import Acrimony.util.tuples.Triplet;

public final class ImmutableTriplet<A, B, C>
extends Triplet<A, B, C> {
    private final A a;
    private final B b;
    private final C c;

    ImmutableTriplet(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public static <A, B, C> ImmutableTriplet<A, B, C> of(A a, B b, C c) {
        return new ImmutableTriplet<A, B, C>(a, b, c);
    }

    public Triplet<A, A, A> pairOfFirst() {
        return Triplet.of(this.a);
    }

    public Triplet<B, B, B> pairOfSecond() {
        return Triplet.of(this.b);
    }

    public Triplet<C, C, C> pairOfThird() {
        return Triplet.of(this.c);
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
    public C getThird() {
        return this.c;
    }

    @Override
    public <R> R apply(Triplet.TriFunction<? super A, ? super B, ? super C, ? extends R> func) {
        return func.apply(this.a, this.b, this.c);
    }

    @Override
    public void use(Triplet.TriConsumer<? super A, ? super B, ? super C> func) {
        func.accept(this.a, this.b, this.c);
    }
}

