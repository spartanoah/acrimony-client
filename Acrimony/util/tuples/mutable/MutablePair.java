/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package Acrimony.util.tuples.mutable;

import Acrimony.util.tuples.Pair;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public class MutablePair<A, B>
extends Pair<A, B> {
    private A a;
    private B b;

    MutablePair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public static <A, B> MutablePair<A, B> of(A a, B b) {
        return new MutablePair<A, B>(a, b);
    }

    public static <A> MutablePair<A, A> of(A a) {
        return new MutablePair<A, A>(a, a);
    }

    public MutablePair<A, A> pairOfFirst() {
        return MutablePair.of(this.a);
    }

    public MutablePair<B, B> pairOfSecond() {
        return MutablePair.of(this.b);
    }

    @Override
    public A getFirst() {
        return this.a;
    }

    @Override
    public B getSecond() {
        return this.b;
    }

    public void setFirst(A a) {
        this.a = a;
    }

    public void setSecond(B b) {
        this.b = b;
    }

    @Override
    public <R> R apply(BiFunction<? super A, ? super B, ? extends R> func) {
        return func.apply(this.a, this.b);
    }

    @Override
    public void use(BiConsumer<? super A, ? super B> func) {
        func.accept(this.a, this.b);
    }

    public void computeFirst(UnaryOperator<A> operator) {
        this.a = operator.apply(this.a);
    }

    public void computeSecond(UnaryOperator<B> operator) {
        this.b = operator.apply(this.b);
    }
}

