/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.text;

import com.ibm.icu.text.DigitList;
import com.ibm.icu.text.NFRuleSet;
import com.ibm.icu.text.NFSubstitution;
import com.ibm.icu.text.RuleBasedNumberFormat;
import java.text.ParsePosition;

class FractionalPartSubstitution
extends NFSubstitution {
    private boolean byDigits = false;
    private boolean useSpaces = true;

    FractionalPartSubstitution(int pos, NFRuleSet ruleSet, RuleBasedNumberFormat formatter, String description) {
        super(pos, ruleSet, formatter, description);
        if (description.equals(">>") || description.equals(">>>") || ruleSet == this.ruleSet) {
            this.byDigits = true;
            if (description.equals(">>>")) {
                this.useSpaces = false;
            }
        } else {
            this.ruleSet.makeIntoFractionRuleSet();
        }
    }

    public void doSubstitution(double number, StringBuffer toInsertInto, int position) {
        if (!this.byDigits) {
            super.doSubstitution(number, toInsertInto, position);
        } else {
            DigitList dl = new DigitList();
            dl.set(number, 20, true);
            boolean pad = false;
            while (dl.count > Math.max(0, dl.decimalAt)) {
                if (pad && this.useSpaces) {
                    toInsertInto.insert(position + this.pos, ' ');
                } else {
                    pad = true;
                }
                this.ruleSet.format(dl.digits[--dl.count] - 48, toInsertInto, position + this.pos);
            }
            while (dl.decimalAt < 0) {
                if (pad && this.useSpaces) {
                    toInsertInto.insert(position + this.pos, ' ');
                } else {
                    pad = true;
                }
                this.ruleSet.format(0L, toInsertInto, position + this.pos);
                ++dl.decimalAt;
            }
        }
    }

    public long transformNumber(long number) {
        return 0L;
    }

    public double transformNumber(double number) {
        return number - Math.floor(number);
    }

    public Number doParse(String text, ParsePosition parsePosition, double baseValue, double upperBound, boolean lenientParse) {
        if (!this.byDigits) {
            return super.doParse(text, parsePosition, baseValue, 0.0, lenientParse);
        }
        String workText = text;
        ParsePosition workPos = new ParsePosition(1);
        double result = 0.0;
        DigitList dl = new DigitList();
        while (workText.length() > 0 && workPos.getIndex() != 0) {
            Number n;
            workPos.setIndex(0);
            int digit = this.ruleSet.parse(workText, workPos, 10.0).intValue();
            if (lenientParse && workPos.getIndex() == 0 && (n = this.rbnf.getDecimalFormat().parse(workText, workPos)) != null) {
                digit = n.intValue();
            }
            if (workPos.getIndex() == 0) continue;
            dl.append(48 + digit);
            parsePosition.setIndex(parsePosition.getIndex() + workPos.getIndex());
            workText = workText.substring(workPos.getIndex());
            while (workText.length() > 0 && workText.charAt(0) == ' ') {
                workText = workText.substring(1);
                parsePosition.setIndex(parsePosition.getIndex() + 1);
            }
        }
        result = dl.count == 0 ? 0.0 : dl.getDouble();
        result = this.composeRuleValue(result, baseValue);
        return new Double(result);
    }

    public double composeRuleValue(double newRuleValue, double oldRuleValue) {
        return newRuleValue + oldRuleValue;
    }

    public double calcUpperBound(double oldUpperBound) {
        return 0.0;
    }

    char tokenChar() {
        return '>';
    }
}

