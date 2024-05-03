/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.text;

import com.ibm.icu.text.NFRuleSet;
import com.ibm.icu.text.NFSubstitution;
import com.ibm.icu.text.RuleBasedNumberFormat;

class MultiplierSubstitution
extends NFSubstitution {
    double divisor;

    MultiplierSubstitution(int pos, double divisor, NFRuleSet ruleSet, RuleBasedNumberFormat formatter, String description) {
        super(pos, ruleSet, formatter, description);
        this.divisor = divisor;
        if (divisor == 0.0) {
            throw new IllegalStateException("Substitution with bad divisor (" + divisor + ") " + description.substring(0, pos) + " | " + description.substring(pos));
        }
    }

    public void setDivisor(int radix, int exponent) {
        this.divisor = Math.pow(radix, exponent);
        if (this.divisor == 0.0) {
            throw new IllegalStateException("Substitution with divisor 0");
        }
    }

    public boolean equals(Object that) {
        if (super.equals(that)) {
            MultiplierSubstitution that2 = (MultiplierSubstitution)that;
            return this.divisor == that2.divisor;
        }
        return false;
    }

    public int hashCode() {
        assert (false) : "hashCode not designed";
        return 42;
    }

    public long transformNumber(long number) {
        return (long)Math.floor((double)number / this.divisor);
    }

    public double transformNumber(double number) {
        if (this.ruleSet == null) {
            return number / this.divisor;
        }
        return Math.floor(number / this.divisor);
    }

    public double composeRuleValue(double newRuleValue, double oldRuleValue) {
        return newRuleValue * this.divisor;
    }

    public double calcUpperBound(double oldUpperBound) {
        return this.divisor;
    }

    char tokenChar() {
        return '<';
    }
}

