/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.text;

import com.ibm.icu.text.NFRuleSet;
import com.ibm.icu.text.NFSubstitution;
import com.ibm.icu.text.RuleBasedNumberFormat;
import java.text.ParsePosition;

class NumeratorSubstitution
extends NFSubstitution {
    double denominator;
    boolean withZeros;

    NumeratorSubstitution(int pos, double denominator, NFRuleSet ruleSet, RuleBasedNumberFormat formatter, String description) {
        super(pos, ruleSet, formatter, NumeratorSubstitution.fixdesc(description));
        this.denominator = denominator;
        this.withZeros = description.endsWith("<<");
    }

    static String fixdesc(String description) {
        return description.endsWith("<<") ? description.substring(0, description.length() - 1) : description;
    }

    public boolean equals(Object that) {
        if (super.equals(that)) {
            NumeratorSubstitution that2 = (NumeratorSubstitution)that;
            return this.denominator == that2.denominator;
        }
        return false;
    }

    public int hashCode() {
        assert (false) : "hashCode not designed";
        return 42;
    }

    public void doSubstitution(double number, StringBuffer toInsertInto, int position) {
        double numberToFormat = this.transformNumber(number);
        if (this.withZeros && this.ruleSet != null) {
            long nf = (long)numberToFormat;
            int len = toInsertInto.length();
            while (true) {
                long l;
                nf *= 10L;
                if (!((double)l < this.denominator)) break;
                toInsertInto.insert(position + this.pos, ' ');
                this.ruleSet.format(0L, toInsertInto, position + this.pos);
            }
            position += toInsertInto.length() - len;
        }
        if (numberToFormat == Math.floor(numberToFormat) && this.ruleSet != null) {
            this.ruleSet.format((long)numberToFormat, toInsertInto, position + this.pos);
        } else if (this.ruleSet != null) {
            this.ruleSet.format(numberToFormat, toInsertInto, position + this.pos);
        } else {
            toInsertInto.insert(position + this.pos, this.numberFormat.format(numberToFormat));
        }
    }

    public long transformNumber(long number) {
        return Math.round((double)number * this.denominator);
    }

    public double transformNumber(double number) {
        return Math.round(number * this.denominator);
    }

    public Number doParse(String text, ParsePosition parsePosition, double baseValue, double upperBound, boolean lenientParse) {
        int zeroCount = 0;
        if (this.withZeros) {
            String workText = text;
            ParsePosition workPos = new ParsePosition(1);
            while (workText.length() > 0 && workPos.getIndex() != 0) {
                workPos.setIndex(0);
                this.ruleSet.parse(workText, workPos, 1.0).intValue();
                if (workPos.getIndex() == 0) break;
                ++zeroCount;
                parsePosition.setIndex(parsePosition.getIndex() + workPos.getIndex());
                workText = workText.substring(workPos.getIndex());
                while (workText.length() > 0 && workText.charAt(0) == ' ') {
                    workText = workText.substring(1);
                    parsePosition.setIndex(parsePosition.getIndex() + 1);
                }
            }
            text = text.substring(parsePosition.getIndex());
            parsePosition.setIndex(0);
        }
        Number result = super.doParse(text, parsePosition, this.withZeros ? 1.0 : baseValue, upperBound, false);
        if (this.withZeros) {
            long d;
            long n = result.longValue();
            for (d = 1L; d <= n; d *= 10L) {
            }
            while (zeroCount > 0) {
                d *= 10L;
                --zeroCount;
            }
            result = new Double((double)n / (double)d);
        }
        return result;
    }

    public double composeRuleValue(double newRuleValue, double oldRuleValue) {
        return newRuleValue / oldRuleValue;
    }

    public double calcUpperBound(double oldUpperBound) {
        return this.denominator;
    }

    char tokenChar() {
        return '<';
    }
}

