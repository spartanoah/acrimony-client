/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.tuples.mutable;

import Acrimony.util.tuples.Triplet;
import java.util.function.UnaryOperator;

public class MutableTriplet<A, B, C>
extends Triplet<A, B, C> {
    private A a;
    private B b;
    private C c;

    MutableTriplet(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public static <A, B, C> MutableTriplet<A, B, C> of(A a, B b, C c) {
        return new MutableTriplet<A, B, C>(a, b, c);
    }

    public static <A> MutableTriplet<A, A, A> of(A a) {
        return new MutableTriplet<A, A, A>(a, a, a);
    }

    public MutableTriplet<A, A, A> pairOfFirst() {
        return MutableTriplet.of(this.a);
    }

    public MutableTriplet<B, B, B> pairOfSecond() {
        return MutableTriplet.of(this.b);
    }

    public MutableTriplet<C, C, C> pairOfThird() {
        return MutableTriplet.of(this.c);
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

    public void setFirst(A a) {
        this.a = a;
    }

    public void setSecond(B b) {
        this.b = b;
    }

    public void setThird(C c) {
        this.c = c;
    }

    @Override
    public <R> R apply(Triplet.TriFunction<? super A, ? super B, ? super C, ? extends R> func) {
        return func.apply(this.a, this.b, this.c);
    }

    @Override
    public void use(Triplet.TriConsumer<? super A, ? super B, ? super C> func) {
        func.accept(this.a, this.b, this.c);
    }

    public void computeFirst(UnaryOperator<A> operator) {
        this.a = operator.apply(this.a);
    }

    public void computeSecond(UnaryOperator<B> operator) {
        this.b = operator.apply(this.b);
    }

    public void computeThird(UnaryOperator<C> operator) {
        this.c = operator.apply(this.c);
    }
}

