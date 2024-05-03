/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.impl.duration.impl;

import com.ibm.icu.impl.duration.TimeUnit;
import com.ibm.icu.impl.duration.impl.DataRecord;
import com.ibm.icu.impl.duration.impl.Utils;
import java.util.Arrays;

public class PeriodFormatterData {
    final DataRecord dr;
    String localeName;
    public static boolean trace = false;
    private static final int FORM_PLURAL = 0;
    private static final int FORM_SINGULAR = 1;
    private static final int FORM_DUAL = 2;
    private static final int FORM_PAUCAL = 3;
    private static final int FORM_SINGULAR_SPELLED = 4;
    private static final int FORM_SINGULAR_NO_OMIT = 5;
    private static final int FORM_HALF_SPELLED = 6;

    public PeriodFormatterData(String localeName, DataRecord dr) {
        this.dr = dr;
        this.localeName = localeName;
        if (localeName == null) {
            throw new NullPointerException("localename is null");
        }
        if (dr == null) {
            throw new NullPointerException("data record is null");
        }
    }

    public int pluralization() {
        return this.dr.pl;
    }

    public boolean allowZero() {
        return this.dr.allowZero;
    }

    public boolean weeksAloneOnly() {
        return this.dr.weeksAloneOnly;
    }

    public int useMilliseconds() {
        return this.dr.useMilliseconds;
    }

    public boolean appendPrefix(int tl, int td, StringBuffer sb) {
        String prefix;
        int ix;
        DataRecord.ScopeData sd;
        if (this.dr.scopeData != null && (sd = this.dr.scopeData[ix = tl * 3 + td]) != null && (prefix = sd.prefix) != null) {
            sb.append(prefix);
            return sd.requiresDigitPrefix;
        }
        return false;
    }

    public void appendSuffix(int tl, int td, StringBuffer sb) {
        String suffix;
        int ix;
        DataRecord.ScopeData sd;
        if (this.dr.scopeData != null && (sd = this.dr.scopeData[ix = tl * 3 + td]) != null && (suffix = sd.suffix) != null) {
            if (trace) {
                System.out.println("appendSuffix '" + suffix + "'");
            }
            sb.append(suffix);
        }
    }

    public boolean appendUnit(TimeUnit unit, int count, int cv, int uv, boolean useCountSep, boolean useDigitPrefix, boolean multiple, boolean last, boolean wasSkipped, StringBuffer sb) {
        int px = unit.ordinal();
        boolean willRequireSkipMarker = false;
        if (this.dr.requiresSkipMarker != null && this.dr.requiresSkipMarker[px] && this.dr.skippedUnitMarker != null) {
            if (!wasSkipped && last) {
                sb.append(this.dr.skippedUnitMarker);
            }
            willRequireSkipMarker = true;
        }
        if (uv != 0) {
            String[] names;
            boolean useMedium = uv == 1;
            String[] stringArray = names = useMedium ? this.dr.mediumNames : this.dr.shortNames;
            if (names == null || names[px] == null) {
                String[] stringArray2 = names = useMedium ? this.dr.shortNames : this.dr.mediumNames;
            }
            if (names != null && names[px] != null) {
                this.appendCount(unit, false, false, count, cv, useCountSep, names[px], last, sb);
                return false;
            }
        }
        if (cv == 2 && this.dr.halfSupport != null) {
            switch (this.dr.halfSupport[px]) {
                case 0: {
                    break;
                }
                case 2: {
                    if (count > 1000) break;
                }
                case 1: {
                    count = count / 500 * 500;
                    cv = 3;
                }
            }
        }
        String name = null;
        int form = this.computeForm(unit, count, cv, multiple && last);
        if (form == 4) {
            if (this.dr.singularNames == null) {
                form = 1;
                name = this.dr.pluralNames[px][form];
            } else {
                name = this.dr.singularNames[px];
            }
        } else if (form == 5) {
            name = this.dr.pluralNames[px][1];
        } else if (form == 6) {
            name = this.dr.halfNames[px];
        } else {
            try {
                name = this.dr.pluralNames[px][form];
            } catch (NullPointerException e) {
                System.out.println("Null Pointer in PeriodFormatterData[" + this.localeName + "].au px: " + px + " form: " + form + " pn: " + Arrays.toString((Object[])this.dr.pluralNames));
                throw e;
            }
        }
        if (name == null) {
            form = 0;
            name = this.dr.pluralNames[px][form];
        }
        boolean omitCount = form == 4 || form == 6 || this.dr.omitSingularCount && form == 1 || this.dr.omitDualCount && form == 2;
        int suffixIndex = this.appendCount(unit, omitCount, useDigitPrefix, count, cv, useCountSep, name, last, sb);
        if (last && suffixIndex >= 0) {
            String suffix = null;
            if (this.dr.rqdSuffixes != null && suffixIndex < this.dr.rqdSuffixes.length) {
                suffix = this.dr.rqdSuffixes[suffixIndex];
            }
            if (suffix == null && this.dr.optSuffixes != null && suffixIndex < this.dr.optSuffixes.length) {
                suffix = this.dr.optSuffixes[suffixIndex];
            }
            if (suffix != null) {
                sb.append(suffix);
            }
        }
        return willRequireSkipMarker;
    }

    public int appendCount(TimeUnit unit, boolean omitCount, boolean useDigitPrefix, int count, int cv, boolean useSep, String name, boolean last, StringBuffer sb) {
        String measure;
        if (cv == 2 && this.dr.halves == null) {
            cv = 0;
        }
        if (!omitCount && useDigitPrefix && this.dr.digitPrefix != null) {
            sb.append(this.dr.digitPrefix);
        }
        int index = unit.ordinal();
        block0 : switch (cv) {
            case 0: {
                if (omitCount) break;
                this.appendInteger(count / 1000, 1, 10, sb);
                break;
            }
            case 1: {
                int val2 = count / 1000;
                if (unit == TimeUnit.MINUTE && (this.dr.fiveMinutes != null || this.dr.fifteenMinutes != null) && val2 != 0 && val2 % 5 == 0) {
                    if (this.dr.fifteenMinutes != null && (val2 == 15 || val2 == 45)) {
                        int n = val2 = val2 == 15 ? 1 : 3;
                        if (!omitCount) {
                            this.appendInteger(val2, 1, 10, sb);
                        }
                        name = this.dr.fifteenMinutes;
                        index = 8;
                        break;
                    }
                    if (this.dr.fiveMinutes != null) {
                        val2 /= 5;
                        if (!omitCount) {
                            this.appendInteger(val2, 1, 10, sb);
                        }
                        name = this.dr.fiveMinutes;
                        index = 9;
                        break;
                    }
                }
                if (omitCount) break;
                this.appendInteger(val2, 1, 10, sb);
                break;
            }
            case 2: {
                int solox;
                int v = count / 500;
                if (v != 1 && !omitCount) {
                    this.appendCountValue(count, 1, 0, sb);
                }
                if ((v & 1) != 1) break;
                if (v == 1 && this.dr.halfNames != null && this.dr.halfNames[index] != null) {
                    sb.append(name);
                    return last ? index : -1;
                }
                int n = solox = v == 1 ? 0 : 1;
                if (this.dr.genders != null && this.dr.halves.length > 2 && this.dr.genders[index] == 1) {
                    solox += 2;
                }
                byte hp = this.dr.halfPlacements == null ? (byte)0 : this.dr.halfPlacements[solox & 1];
                String half = this.dr.halves[solox];
                String measure2 = this.dr.measures == null ? null : this.dr.measures[index];
                switch (hp) {
                    case 0: {
                        sb.append(half);
                        break block0;
                    }
                    case 1: {
                        if (measure2 != null) {
                            sb.append(measure2);
                            sb.append(half);
                            if (useSep && !omitCount) {
                                sb.append(this.dr.countSep);
                            }
                        } else {
                            sb.append(name);
                            sb.append(half);
                            return last ? index : -1;
                        }
                        sb.append(name);
                        return -1;
                    }
                    case 2: {
                        if (measure2 != null) {
                            sb.append(measure2);
                        }
                        if (useSep && !omitCount) {
                            sb.append(this.dr.countSep);
                        }
                        sb.append(name);
                        sb.append(half);
                        return last ? index : -1;
                    }
                }
                break;
            }
            default: {
                int decimals = 1;
                switch (cv) {
                    case 4: {
                        decimals = 2;
                        break;
                    }
                    case 5: {
                        decimals = 3;
                        break;
                    }
                }
                if (omitCount) break;
                this.appendCountValue(count, 1, decimals, sb);
            }
        }
        if (!omitCount && useSep) {
            sb.append(this.dr.countSep);
        }
        if (!omitCount && this.dr.measures != null && index < this.dr.measures.length && (measure = this.dr.measures[index]) != null) {
            sb.append(measure);
        }
        sb.append(name);
        return last ? index : -1;
    }

    public void appendCountValue(int count, int integralDigits, int decimalDigits, StringBuffer sb) {
        int ival = count / 1000;
        if (decimalDigits == 0) {
            this.appendInteger(ival, integralDigits, 10, sb);
            return;
        }
        if (this.dr.requiresDigitSeparator && sb.length() > 0) {
            sb.append(' ');
        }
        this.appendDigits(ival, integralDigits, 10, sb);
        int dval = count % 1000;
        if (decimalDigits == 1) {
            dval /= 100;
        } else if (decimalDigits == 2) {
            dval /= 10;
        }
        sb.append(this.dr.decimalSep);
        this.appendDigits(dval, decimalDigits, decimalDigits, sb);
        if (this.dr.requiresDigitSeparator) {
            sb.append(' ');
        }
    }

    public void appendInteger(int num, int mindigits, int maxdigits, StringBuffer sb) {
        String name;
        if (this.dr.numberNames != null && num < this.dr.numberNames.length && (name = this.dr.numberNames[num]) != null) {
            sb.append(name);
            return;
        }
        if (this.dr.requiresDigitSeparator && sb.length() > 0) {
            sb.append(' ');
        }
        switch (this.dr.numberSystem) {
            case 0: {
                this.appendDigits(num, mindigits, maxdigits, sb);
                break;
            }
            case 1: {
                sb.append(Utils.chineseNumber(num, Utils.ChineseDigits.TRADITIONAL));
                break;
            }
            case 2: {
                sb.append(Utils.chineseNumber(num, Utils.ChineseDigits.SIMPLIFIED));
                break;
            }
            case 3: {
                sb.append(Utils.chineseNumber(num, Utils.ChineseDigits.KOREAN));
            }
        }
        if (this.dr.requiresDigitSeparator) {
            sb.append(' ');
        }
    }

    public void appendDigits(long num, int mindigits, int maxdigits, StringBuffer sb) {
        char[] buf = new char[maxdigits];
        int ix = maxdigits;
        while (ix > 0 && num > 0L) {
            buf[--ix] = (char)((long)this.dr.zero + num % 10L);
            num /= 10L;
        }
        int e = maxdigits - mindigits;
        while (ix > e) {
            buf[--ix] = this.dr.zero;
        }
        sb.append(buf, ix, maxdigits - ix);
    }

    public void appendSkippedUnit(StringBuffer sb) {
        if (this.dr.skippedUnitMarker != null) {
            sb.append(this.dr.skippedUnitMarker);
        }
    }

    public boolean appendUnitSeparator(TimeUnit unit, boolean longSep, boolean afterFirst, boolean beforeLast, StringBuffer sb) {
        if (longSep && this.dr.unitSep != null || this.dr.shortUnitSep != null) {
            if (longSep && this.dr.unitSep != null) {
                int ix = (afterFirst ? 2 : 0) + (beforeLast ? 1 : 0);
                sb.append(this.dr.unitSep[ix]);
                return this.dr.unitSepRequiresDP != null && this.dr.unitSepRequiresDP[ix];
            }
            sb.append(this.dr.shortUnitSep);
        }
        return false;
    }

    private int computeForm(TimeUnit unit, int count, int cv, boolean lastOfMultiple) {
        if (trace) {
            System.err.println("pfd.cf unit: " + unit + " count: " + count + " cv: " + cv + " dr.pl: " + this.dr.pl);
            Thread.dumpStack();
        }
        if (this.dr.pl == 0) {
            return 0;
        }
        int val2 = count / 1000;
        block0 : switch (cv) {
            case 0: 
            case 1: {
                break;
            }
            case 2: {
                switch (this.dr.fractionHandling) {
                    case 0: {
                        return 0;
                    }
                    case 1: 
                    case 2: {
                        int v = count / 500;
                        if (v == 1) {
                            if (this.dr.halfNames != null && this.dr.halfNames[unit.ordinal()] != null) {
                                return 6;
                            }
                            return 5;
                        }
                        if ((v & 1) != 1) break block0;
                        if (this.dr.pl == 5 && v > 21) {
                            return 5;
                        }
                        if (v != 3 || this.dr.pl != 1 || this.dr.fractionHandling == 2) break block0;
                        return 0;
                    }
                    case 3: {
                        int v = count / 500;
                        if (v != 1 && v != 3) break block0;
                        return 3;
                    }
                    default: {
                        throw new IllegalStateException();
                    }
                }
            }
            default: {
                switch (this.dr.decimalHandling) {
                    case 0: {
                        break;
                    }
                    case 1: {
                        return 5;
                    }
                    case 2: {
                        if (count >= 1000) break;
                        return 5;
                    }
                    case 3: {
                        if (this.dr.pl != 3) break;
                        return 3;
                    }
                }
                return 0;
            }
        }
        if (trace && count == 0) {
            System.err.println("EZeroHandling = " + this.dr.zeroHandling);
        }
        if (count == 0 && this.dr.zeroHandling == 1) {
            return 4;
        }
        int form = 0;
        switch (this.dr.pl) {
            case 0: {
                break;
            }
            case 1: {
                if (val2 != 1) break;
                form = 4;
                break;
            }
            case 2: {
                if (val2 == 2) {
                    form = 2;
                    break;
                }
                if (val2 != 1) break;
                form = 1;
                break;
            }
            case 3: {
                int v = val2;
                if ((v %= 100) > 20) {
                    v %= 10;
                }
                if (v == 1) {
                    form = 1;
                    break;
                }
                if (v <= 1 || v >= 5) break;
                form = 3;
                break;
            }
            case 4: {
                if (val2 == 2) {
                    form = 2;
                    break;
                }
                if (val2 == 1) {
                    if (lastOfMultiple) {
                        form = 4;
                        break;
                    }
                    form = 1;
                    break;
                }
                if (unit != TimeUnit.YEAR || val2 <= 11) break;
                form = 5;
                break;
            }
            case 5: {
                if (val2 == 2) {
                    form = 2;
                    break;
                }
                if (val2 == 1) {
                    form = 1;
                    break;
                }
                if (val2 <= 10) break;
                form = 5;
                break;
            }
            default: {
                System.err.println("dr.pl is " + this.dr.pl);
                throw new IllegalStateException();
            }
        }
        return form;
    }
}

