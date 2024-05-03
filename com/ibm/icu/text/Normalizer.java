/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.text;

import com.ibm.icu.impl.Norm2AllModes;
import com.ibm.icu.impl.Normalizer2Impl;
import com.ibm.icu.impl.UCaseProps;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.FilteredNormalizer2;
import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.text.UCharacterIterator;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.text.UnicodeSet;
import java.nio.CharBuffer;
import java.text.CharacterIterator;

public final class Normalizer
implements Cloneable {
    private UCharacterIterator text;
    private Normalizer2 norm2;
    private Mode mode;
    private int options;
    private int currentIndex;
    private int nextIndex;
    private StringBuilder buffer;
    private int bufferPos;
    public static final int UNICODE_3_2 = 32;
    public static final int DONE = -1;
    public static final Mode NONE = new NONEMode();
    public static final Mode NFD = new NFDMode();
    public static final Mode NFKD = new NFKDMode();
    public static final Mode NFC;
    public static final Mode DEFAULT;
    public static final Mode NFKC;
    public static final Mode FCD;
    public static final Mode NO_OP;
    public static final Mode COMPOSE;
    public static final Mode COMPOSE_COMPAT;
    public static final Mode DECOMP;
    public static final Mode DECOMP_COMPAT;
    public static final int IGNORE_HANGUL = 1;
    public static final QuickCheckResult NO;
    public static final QuickCheckResult YES;
    public static final QuickCheckResult MAYBE;
    public static final int FOLD_CASE_DEFAULT = 0;
    public static final int INPUT_IS_FCD = 131072;
    public static final int COMPARE_IGNORE_CASE = 65536;
    public static final int COMPARE_CODE_POINT_ORDER = 32768;
    public static final int FOLD_CASE_EXCLUDE_SPECIAL_I = 1;
    public static final int COMPARE_NORM_OPTIONS_SHIFT = 20;
    private static final int COMPARE_EQUIV = 524288;

    public Normalizer(String str, Mode mode, int opt) {
        this.text = UCharacterIterator.getInstance(str);
        this.mode = mode;
        this.options = opt;
        this.norm2 = mode.getNormalizer2(opt);
        this.buffer = new StringBuilder();
    }

    public Normalizer(CharacterIterator iter, Mode mode, int opt) {
        this.text = UCharacterIterator.getInstance((CharacterIterator)iter.clone());
        this.mode = mode;
        this.options = opt;
        this.norm2 = mode.getNormalizer2(opt);
        this.buffer = new StringBuilder();
    }

    public Normalizer(UCharacterIterator iter, Mode mode, int options) {
        try {
            this.text = (UCharacterIterator)iter.clone();
            this.mode = mode;
            this.options = options;
            this.norm2 = mode.getNormalizer2(options);
            this.buffer = new StringBuilder();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e.toString());
        }
    }

    public Object clone() {
        try {
            Normalizer copy = (Normalizer)super.clone();
            copy.text = (UCharacterIterator)this.text.clone();
            copy.mode = this.mode;
            copy.options = this.options;
            copy.norm2 = this.norm2;
            copy.buffer = new StringBuilder(this.buffer);
            copy.bufferPos = this.bufferPos;
            copy.currentIndex = this.currentIndex;
            copy.nextIndex = this.nextIndex;
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final Normalizer2 getComposeNormalizer2(boolean compat, int options) {
        return (compat ? NFKC : NFC).getNormalizer2(options);
    }

    private static final Normalizer2 getDecomposeNormalizer2(boolean compat, int options) {
        return (compat ? NFKD : NFD).getNormalizer2(options);
    }

    public static String compose(String str, boolean compat) {
        return Normalizer.compose(str, compat, 0);
    }

    public static String compose(String str, boolean compat, int options) {
        return Normalizer.getComposeNormalizer2(compat, options).normalize(str);
    }

    public static int compose(char[] source, char[] target, boolean compat, int options) {
        return Normalizer.compose(source, 0, source.length, target, 0, target.length, compat, options);
    }

    public static int compose(char[] src, int srcStart, int srcLimit, char[] dest, int destStart, int destLimit, boolean compat, int options) {
        CharBuffer srcBuffer = CharBuffer.wrap(src, srcStart, srcLimit - srcStart);
        CharsAppendable app = new CharsAppendable(dest, destStart, destLimit);
        Normalizer.getComposeNormalizer2(compat, options).normalize((CharSequence)srcBuffer, app);
        return app.length();
    }

    public static String decompose(String str, boolean compat) {
        return Normalizer.decompose(str, compat, 0);
    }

    public static String decompose(String str, boolean compat, int options) {
        return Normalizer.getDecomposeNormalizer2(compat, options).normalize(str);
    }

    public static int decompose(char[] source, char[] target, boolean compat, int options) {
        return Normalizer.decompose(source, 0, source.length, target, 0, target.length, compat, options);
    }

    public static int decompose(char[] src, int srcStart, int srcLimit, char[] dest, int destStart, int destLimit, boolean compat, int options) {
        CharBuffer srcBuffer = CharBuffer.wrap(src, srcStart, srcLimit - srcStart);
        CharsAppendable app = new CharsAppendable(dest, destStart, destLimit);
        Normalizer.getDecomposeNormalizer2(compat, options).normalize((CharSequence)srcBuffer, app);
        return app.length();
    }

    public static String normalize(String str, Mode mode, int options) {
        return mode.getNormalizer2(options).normalize(str);
    }

    public static String normalize(String src, Mode mode) {
        return Normalizer.normalize(src, mode, 0);
    }

    public static int normalize(char[] source, char[] target, Mode mode, int options) {
        return Normalizer.normalize(source, 0, source.length, target, 0, target.length, mode, options);
    }

    public static int normalize(char[] src, int srcStart, int srcLimit, char[] dest, int destStart, int destLimit, Mode mode, int options) {
        CharBuffer srcBuffer = CharBuffer.wrap(src, srcStart, srcLimit - srcStart);
        CharsAppendable app = new CharsAppendable(dest, destStart, destLimit);
        mode.getNormalizer2(options).normalize((CharSequence)srcBuffer, app);
        return app.length();
    }

    public static String normalize(int char32, Mode mode, int options) {
        if (mode == NFD && options == 0) {
            String decomposition = Norm2AllModes.getNFCInstance().impl.getDecomposition(char32);
            if (decomposition == null) {
                decomposition = UTF16.valueOf(char32);
            }
            return decomposition;
        }
        return Normalizer.normalize(UTF16.valueOf(char32), mode, options);
    }

    public static String normalize(int char32, Mode mode) {
        return Normalizer.normalize(char32, mode, 0);
    }

    public static QuickCheckResult quickCheck(String source, Mode mode) {
        return Normalizer.quickCheck(source, mode, 0);
    }

    public static QuickCheckResult quickCheck(String source, Mode mode, int options) {
        return mode.getNormalizer2(options).quickCheck(source);
    }

    public static QuickCheckResult quickCheck(char[] source, Mode mode, int options) {
        return Normalizer.quickCheck(source, 0, source.length, mode, options);
    }

    public static QuickCheckResult quickCheck(char[] source, int start, int limit, Mode mode, int options) {
        CharBuffer srcBuffer = CharBuffer.wrap(source, start, limit - start);
        return mode.getNormalizer2(options).quickCheck(srcBuffer);
    }

    public static boolean isNormalized(char[] src, int start, int limit, Mode mode, int options) {
        CharBuffer srcBuffer = CharBuffer.wrap(src, start, limit - start);
        return mode.getNormalizer2(options).isNormalized(srcBuffer);
    }

    public static boolean isNormalized(String str, Mode mode, int options) {
        return mode.getNormalizer2(options).isNormalized(str);
    }

    public static boolean isNormalized(int char32, Mode mode, int options) {
        return Normalizer.isNormalized(UTF16.valueOf(char32), mode, options);
    }

    public static int compare(char[] s1, int s1Start, int s1Limit, char[] s2, int s2Start, int s2Limit, int options) {
        if (s1 == null || s1Start < 0 || s1Limit < 0 || s2 == null || s2Start < 0 || s2Limit < 0 || s1Limit < s1Start || s2Limit < s2Start) {
            throw new IllegalArgumentException();
        }
        return Normalizer.internalCompare(CharBuffer.wrap(s1, s1Start, s1Limit - s1Start), CharBuffer.wrap(s2, s2Start, s2Limit - s2Start), options);
    }

    public static int compare(String s1, String s2, int options) {
        return Normalizer.internalCompare(s1, s2, options);
    }

    public static int compare(char[] s1, char[] s2, int options) {
        return Normalizer.internalCompare(CharBuffer.wrap(s1), CharBuffer.wrap(s2), options);
    }

    public static int compare(int char32a, int char32b, int options) {
        return Normalizer.internalCompare(UTF16.valueOf(char32a), UTF16.valueOf(char32b), options | 0x20000);
    }

    public static int compare(int char32a, String str2, int options) {
        return Normalizer.internalCompare(UTF16.valueOf(char32a), str2, options);
    }

    public static int concatenate(char[] left, int leftStart, int leftLimit, char[] right, int rightStart, int rightLimit, char[] dest, int destStart, int destLimit, Mode mode, int options) {
        if (dest == null) {
            throw new IllegalArgumentException();
        }
        if (right == dest && rightStart < destLimit && destStart < rightLimit) {
            throw new IllegalArgumentException("overlapping right and dst ranges");
        }
        StringBuilder destBuilder = new StringBuilder(leftLimit - leftStart + rightLimit - rightStart + 16);
        destBuilder.append(left, leftStart, leftLimit - leftStart);
        CharBuffer rightBuffer = CharBuffer.wrap(right, rightStart, rightLimit - rightStart);
        mode.getNormalizer2(options).append(destBuilder, rightBuffer);
        int destLength = destBuilder.length();
        if (destLength <= destLimit - destStart) {
            destBuilder.getChars(0, destLength, dest, destStart);
            return destLength;
        }
        throw new IndexOutOfBoundsException(Integer.toString(destLength));
    }

    public static String concatenate(char[] left, char[] right, Mode mode, int options) {
        StringBuilder dest = new StringBuilder(left.length + right.length + 16).append(left);
        return mode.getNormalizer2(options).append(dest, CharBuffer.wrap(right)).toString();
    }

    public static String concatenate(String left, String right, Mode mode, int options) {
        StringBuilder dest = new StringBuilder(left.length() + right.length() + 16).append(left);
        return mode.getNormalizer2(options).append(dest, right).toString();
    }

    public static int getFC_NFKC_Closure(int c, char[] dest) {
        String closure = Normalizer.getFC_NFKC_Closure(c);
        int length = closure.length();
        if (length != 0 && dest != null && length <= dest.length) {
            closure.getChars(0, length, dest, 0);
        }
        return length;
    }

    public static String getFC_NFKC_Closure(int c) {
        Normalizer2 nfkc = NFKCModeImpl.INSTANCE.normalizer2;
        UCaseProps csp = UCaseProps.INSTANCE;
        StringBuilder folded = new StringBuilder();
        int folded1Length = csp.toFullFolding(c, folded, 0);
        if (folded1Length < 0) {
            Normalizer2Impl nfkcImpl = ((Norm2AllModes.Normalizer2WithImpl)nfkc).impl;
            if (nfkcImpl.getCompQuickCheck(nfkcImpl.getNorm16(c)) != 0) {
                return "";
            }
            folded.appendCodePoint(c);
        } else if (folded1Length > 31) {
            folded.appendCodePoint(folded1Length);
        }
        String kc1 = nfkc.normalize(folded);
        String kc2 = nfkc.normalize(UCharacter.foldCase(kc1, 0));
        if (kc1.equals(kc2)) {
            return "";
        }
        return kc2;
    }

    public int current() {
        if (this.bufferPos < this.buffer.length() || this.nextNormalize()) {
            return this.buffer.codePointAt(this.bufferPos);
        }
        return -1;
    }

    public int next() {
        if (this.bufferPos < this.buffer.length() || this.nextNormalize()) {
            int c = this.buffer.codePointAt(this.bufferPos);
            this.bufferPos += Character.charCount(c);
            return c;
        }
        return -1;
    }

    public int previous() {
        if (this.bufferPos > 0 || this.previousNormalize()) {
            int c = this.buffer.codePointBefore(this.bufferPos);
            this.bufferPos -= Character.charCount(c);
            return c;
        }
        return -1;
    }

    public void reset() {
        this.text.setToStart();
        this.nextIndex = 0;
        this.currentIndex = 0;
        this.clearBuffer();
    }

    public void setIndexOnly(int index) {
        this.text.setIndex(index);
        this.currentIndex = this.nextIndex = index;
        this.clearBuffer();
    }

    public int setIndex(int index) {
        this.setIndexOnly(index);
        return this.current();
    }

    public int getBeginIndex() {
        return 0;
    }

    public int getEndIndex() {
        return this.endIndex();
    }

    public int first() {
        this.reset();
        return this.next();
    }

    public int last() {
        this.text.setToLimit();
        this.currentIndex = this.nextIndex = this.text.getIndex();
        this.clearBuffer();
        return this.previous();
    }

    public int getIndex() {
        if (this.bufferPos < this.buffer.length()) {
            return this.currentIndex;
        }
        return this.nextIndex;
    }

    public int startIndex() {
        return 0;
    }

    public int endIndex() {
        return this.text.getLength();
    }

    public void setMode(Mode newMode) {
        this.mode = newMode;
        this.norm2 = this.mode.getNormalizer2(this.options);
    }

    public Mode getMode() {
        return this.mode;
    }

    public void setOption(int option, boolean value) {
        this.options = value ? (this.options |= option) : (this.options &= ~option);
        this.norm2 = this.mode.getNormalizer2(this.options);
    }

    public int getOption(int option) {
        if ((this.options & option) != 0) {
            return 1;
        }
        return 0;
    }

    public int getText(char[] fillIn) {
        return this.text.getText(fillIn);
    }

    public int getLength() {
        return this.text.getLength();
    }

    public String getText() {
        return this.text.getText();
    }

    public void setText(StringBuffer newText) {
        UCharacterIterator newIter = UCharacterIterator.getInstance(newText);
        if (newIter == null) {
            throw new IllegalStateException("Could not create a new UCharacterIterator");
        }
        this.text = newIter;
        this.reset();
    }

    public void setText(char[] newText) {
        UCharacterIterator newIter = UCharacterIterator.getInstance(newText);
        if (newIter == null) {
            throw new IllegalStateException("Could not create a new UCharacterIterator");
        }
        this.text = newIter;
        this.reset();
    }

    public void setText(String newText) {
        UCharacterIterator newIter = UCharacterIterator.getInstance(newText);
        if (newIter == null) {
            throw new IllegalStateException("Could not create a new UCharacterIterator");
        }
        this.text = newIter;
        this.reset();
    }

    public void setText(CharacterIterator newText) {
        UCharacterIterator newIter = UCharacterIterator.getInstance(newText);
        if (newIter == null) {
            throw new IllegalStateException("Could not create a new UCharacterIterator");
        }
        this.text = newIter;
        this.reset();
    }

    public void setText(UCharacterIterator newText) {
        try {
            UCharacterIterator newIter = (UCharacterIterator)newText.clone();
            if (newIter == null) {
                throw new IllegalStateException("Could not create a new UCharacterIterator");
            }
            this.text = newIter;
            this.reset();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Could not clone the UCharacterIterator");
        }
    }

    private void clearBuffer() {
        this.buffer.setLength(0);
        this.bufferPos = 0;
    }

    private boolean nextNormalize() {
        this.clearBuffer();
        this.currentIndex = this.nextIndex;
        this.text.setIndex(this.nextIndex);
        int c = this.text.nextCodePoint();
        if (c < 0) {
            return false;
        }
        StringBuilder segment = new StringBuilder().appendCodePoint(c);
        while ((c = this.text.nextCodePoint()) >= 0) {
            if (this.norm2.hasBoundaryBefore(c)) {
                this.text.moveCodePointIndex(-1);
                break;
            }
            segment.appendCodePoint(c);
        }
        this.nextIndex = this.text.getIndex();
        this.norm2.normalize((CharSequence)segment, this.buffer);
        return this.buffer.length() != 0;
    }

    private boolean previousNormalize() {
        int c;
        this.clearBuffer();
        this.nextIndex = this.currentIndex;
        this.text.setIndex(this.currentIndex);
        StringBuilder segment = new StringBuilder();
        while ((c = this.text.previousCodePoint()) >= 0) {
            if (c <= 65535) {
                segment.insert(0, (char)c);
            } else {
                segment.insert(0, Character.toChars(c));
            }
            if (!this.norm2.hasBoundaryBefore(c)) continue;
        }
        this.currentIndex = this.text.getIndex();
        this.norm2.normalize((CharSequence)segment, this.buffer);
        this.bufferPos = this.buffer.length();
        return this.buffer.length() != 0;
    }

    private static int internalCompare(CharSequence s1, CharSequence s2, int options) {
        int normOptions = options >>> 20;
        if (((options |= 0x80000) & 0x20000) == 0 || (options & 1) != 0) {
            Normalizer2 n2 = (options & 1) != 0 ? NFD.getNormalizer2(normOptions) : FCD.getNormalizer2(normOptions);
            int spanQCYes1 = n2.spanQuickCheckYes(s1);
            int spanQCYes2 = n2.spanQuickCheckYes(s2);
            if (spanQCYes1 < s1.length()) {
                StringBuilder fcd1 = new StringBuilder(s1.length() + 16).append(s1, 0, spanQCYes1);
                s1 = n2.normalizeSecondAndAppend(fcd1, s1.subSequence(spanQCYes1, s1.length()));
            }
            if (spanQCYes2 < s2.length()) {
                StringBuilder fcd2 = new StringBuilder(s2.length() + 16).append(s2, 0, spanQCYes2);
                s2 = n2.normalizeSecondAndAppend(fcd2, s2.subSequence(spanQCYes2, s2.length()));
            }
        }
        return Normalizer.cmpEquivFold(s1, s2, options);
    }

    private static final CmpEquivLevel[] createCmpEquivLevelStack() {
        return new CmpEquivLevel[]{new CmpEquivLevel(), new CmpEquivLevel()};
    }

    static int cmpEquivFold(CharSequence cs1, CharSequence cs2, int options) {
        StringBuilder fold2;
        StringBuilder fold1;
        UCaseProps csp;
        CmpEquivLevel[] stack1 = null;
        CmpEquivLevel[] stack2 = null;
        Normalizer2Impl nfcImpl = (options & 0x80000) != 0 ? Norm2AllModes.getNFCInstance().impl : null;
        if ((options & 0x10000) != 0) {
            csp = UCaseProps.INSTANCE;
            fold1 = new StringBuilder();
            fold2 = new StringBuilder();
        } else {
            csp = null;
            fold2 = null;
            fold1 = null;
        }
        int s1 = 0;
        int limit1 = cs1.length();
        int s2 = 0;
        int limit2 = cs2.length();
        int level2 = 0;
        int level1 = 0;
        int c2 = -1;
        int c1 = -1;
        while (true) {
            String decomp2;
            String decomp1;
            int length;
            char c;
            if (c1 < 0) {
                while (true) {
                    if (s1 == limit1) {
                        if (level1 == 0) {
                            c1 = -1;
                            break;
                        }
                    } else {
                        c1 = cs1.charAt(s1++);
                        break;
                    }
                    while ((cs1 = stack1[--level1].cs) == null) {
                    }
                    s1 = stack1[level1].s;
                    limit1 = cs1.length();
                }
            }
            if (c2 < 0) {
                while (true) {
                    if (s2 == limit2) {
                        if (level2 == 0) {
                            c2 = -1;
                            break;
                        }
                    } else {
                        c2 = cs2.charAt(s2++);
                        break;
                    }
                    while ((cs2 = stack2[--level2].cs) == null) {
                    }
                    s2 = stack2[level2].s;
                    limit2 = cs2.length();
                }
            }
            if (c1 == c2) {
                if (c1 < 0) {
                    return 0;
                }
                c2 = -1;
                c1 = -1;
                continue;
            }
            if (c1 < 0) {
                return -1;
            }
            if (c2 < 0) {
                return 1;
            }
            int cp1 = c1;
            if (UTF16.isSurrogate((char)c1)) {
                if (Normalizer2Impl.UTF16Plus.isSurrogateLead(c1)) {
                    if (s1 != limit1 && Character.isLowSurrogate(c = cs1.charAt(s1))) {
                        cp1 = Character.toCodePoint((char)c1, c);
                    }
                } else if (0 <= s1 - 2 && Character.isHighSurrogate(c = cs1.charAt(s1 - 2))) {
                    cp1 = Character.toCodePoint(c, (char)c1);
                }
            }
            int cp2 = c2;
            if (UTF16.isSurrogate((char)c2)) {
                if (Normalizer2Impl.UTF16Plus.isSurrogateLead(c2)) {
                    if (s2 != limit2 && Character.isLowSurrogate(c = cs2.charAt(s2))) {
                        cp2 = Character.toCodePoint((char)c2, c);
                    }
                } else if (0 <= s2 - 2 && Character.isHighSurrogate(c = cs2.charAt(s2 - 2))) {
                    cp2 = Character.toCodePoint(c, (char)c2);
                }
            }
            if (level1 == 0 && (options & 0x10000) != 0 && (length = csp.toFullFolding(cp1, fold1, options)) >= 0) {
                if (UTF16.isSurrogate((char)c1)) {
                    if (Normalizer2Impl.UTF16Plus.isSurrogateLead(c1)) {
                        ++s1;
                    } else {
                        c2 = cs2.charAt(--s2 - 1);
                    }
                }
                if (stack1 == null) {
                    stack1 = Normalizer.createCmpEquivLevelStack();
                }
                stack1[0].cs = cs1;
                stack1[0].s = s1;
                ++level1;
                if (length <= 31) {
                    fold1.delete(0, fold1.length() - length);
                } else {
                    fold1.setLength(0);
                    fold1.appendCodePoint(length);
                }
                cs1 = fold1;
                s1 = 0;
                limit1 = fold1.length();
                c1 = -1;
                continue;
            }
            if (level2 == 0 && (options & 0x10000) != 0 && (length = csp.toFullFolding(cp2, fold2, options)) >= 0) {
                if (UTF16.isSurrogate((char)c2)) {
                    if (Normalizer2Impl.UTF16Plus.isSurrogateLead(c2)) {
                        ++s2;
                    } else {
                        c1 = cs1.charAt(--s1 - 1);
                    }
                }
                if (stack2 == null) {
                    stack2 = Normalizer.createCmpEquivLevelStack();
                }
                stack2[0].cs = cs2;
                stack2[0].s = s2;
                ++level2;
                if (length <= 31) {
                    fold2.delete(0, fold2.length() - length);
                } else {
                    fold2.setLength(0);
                    fold2.appendCodePoint(length);
                }
                cs2 = fold2;
                s2 = 0;
                limit2 = fold2.length();
                c2 = -1;
                continue;
            }
            if (level1 < 2 && (options & 0x80000) != 0 && (decomp1 = nfcImpl.getDecomposition(cp1)) != null) {
                if (UTF16.isSurrogate((char)c1)) {
                    if (Normalizer2Impl.UTF16Plus.isSurrogateLead(c1)) {
                        ++s1;
                    } else {
                        c2 = cs2.charAt(--s2 - 1);
                    }
                }
                if (stack1 == null) {
                    stack1 = Normalizer.createCmpEquivLevelStack();
                }
                stack1[level1].cs = cs1;
                stack1[level1].s = s1;
                if (++level1 < 2) {
                    stack1[level1++].cs = null;
                }
                cs1 = decomp1;
                s1 = 0;
                limit1 = decomp1.length();
                c1 = -1;
                continue;
            }
            if (level2 >= 2 || (options & 0x80000) == 0 || (decomp2 = nfcImpl.getDecomposition(cp2)) == null) break;
            if (UTF16.isSurrogate((char)c2)) {
                if (Normalizer2Impl.UTF16Plus.isSurrogateLead(c2)) {
                    ++s2;
                } else {
                    c1 = cs1.charAt(--s1 - 1);
                }
            }
            if (stack2 == null) {
                stack2 = Normalizer.createCmpEquivLevelStack();
            }
            stack2[level2].cs = cs2;
            stack2[level2].s = s2;
            if (++level2 < 2) {
                stack2[level2++].cs = null;
            }
            cs2 = decomp2;
            s2 = 0;
            limit2 = decomp2.length();
            c2 = -1;
        }
        if (c1 >= 55296 && c2 >= 55296 && (options & 0x8000) != 0) {
            if (!(c1 <= 56319 && s1 != limit1 && Character.isLowSurrogate(cs1.charAt(s1)) || Character.isLowSurrogate((char)c1) && 0 != s1 - 1 && Character.isHighSurrogate(cs1.charAt(s1 - 2)))) {
                c1 -= 10240;
            }
            if (!(c2 <= 56319 && s2 != limit2 && Character.isLowSurrogate(cs2.charAt(s2)) || Character.isLowSurrogate((char)c2) && 0 != s2 - 1 && Character.isHighSurrogate(cs2.charAt(s2 - 2)))) {
                c2 -= 10240;
            }
        }
        return c1 - c2;
    }

    static {
        DEFAULT = NFC = new NFCMode();
        NFKC = new NFKCMode();
        FCD = new FCDMode();
        NO_OP = NONE;
        COMPOSE = NFC;
        COMPOSE_COMPAT = NFKC;
        DECOMP = NFD;
        DECOMP_COMPAT = NFKD;
        NO = new QuickCheckResult(0);
        YES = new QuickCheckResult(1);
        MAYBE = new QuickCheckResult(2);
    }

    private static final class CharsAppendable
    implements Appendable {
        private final char[] chars;
        private final int start;
        private final int limit;
        private int offset;

        public CharsAppendable(char[] dest, int destStart, int destLimit) {
            this.chars = dest;
            this.start = this.offset = destStart;
            this.limit = destLimit;
        }

        public int length() {
            int len = this.offset - this.start;
            if (this.offset <= this.limit) {
                return len;
            }
            throw new IndexOutOfBoundsException(Integer.toString(len));
        }

        public Appendable append(char c) {
            if (this.offset < this.limit) {
                this.chars[this.offset] = c;
            }
            ++this.offset;
            return this;
        }

        public Appendable append(CharSequence s) {
            return this.append(s, 0, s.length());
        }

        public Appendable append(CharSequence s, int sStart, int sLimit) {
            int len = sLimit - sStart;
            if (len <= this.limit - this.offset) {
                while (sStart < sLimit) {
                    this.chars[this.offset++] = s.charAt(sStart++);
                }
            } else {
                this.offset += len;
            }
            return this;
        }
    }

    private static final class CmpEquivLevel {
        CharSequence cs;
        int s;

        private CmpEquivLevel() {
        }
    }

    public static final class QuickCheckResult {
        private QuickCheckResult(int value) {
        }
    }

    private static final class FCDMode
    extends Mode {
        private FCDMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return (options & 0x20) != 0 ? FCD32ModeImpl.INSTANCE.normalizer2 : FCDModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NFKCMode
    extends Mode {
        private NFKCMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return (options & 0x20) != 0 ? NFKC32ModeImpl.INSTANCE.normalizer2 : NFKCModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NFCMode
    extends Mode {
        private NFCMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return (options & 0x20) != 0 ? NFC32ModeImpl.INSTANCE.normalizer2 : NFCModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NFKDMode
    extends Mode {
        private NFKDMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return (options & 0x20) != 0 ? NFKD32ModeImpl.INSTANCE.normalizer2 : NFKDModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NFDMode
    extends Mode {
        private NFDMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return (options & 0x20) != 0 ? NFD32ModeImpl.INSTANCE.normalizer2 : NFDModeImpl.INSTANCE.normalizer2;
        }
    }

    private static final class NONEMode
    extends Mode {
        private NONEMode() {
        }

        protected Normalizer2 getNormalizer2(int options) {
            return Norm2AllModes.NOOP_NORMALIZER2;
        }
    }

    public static abstract class Mode {
        protected abstract Normalizer2 getNormalizer2(int var1);
    }

    private static final class FCD32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Norm2AllModes.getFCDNormalizer2(), Unicode32.access$100()));

        private FCD32ModeImpl() {
        }
    }

    private static final class NFKC32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Norm2AllModes.getNFKCInstance().comp, Unicode32.access$100()));

        private NFKC32ModeImpl() {
        }
    }

    private static final class NFC32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Norm2AllModes.getNFCInstance().comp, Unicode32.access$100()));

        private NFC32ModeImpl() {
        }
    }

    private static final class NFKD32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Norm2AllModes.getNFKCInstance().decomp, Unicode32.access$100()));

        private NFKD32ModeImpl() {
        }
    }

    private static final class NFD32ModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(new FilteredNormalizer2(Norm2AllModes.getNFCInstance().decomp, Unicode32.access$100()));

        private NFD32ModeImpl() {
        }
    }

    private static final class Unicode32 {
        private static final UnicodeSet INSTANCE = new UnicodeSet("[:age=3.2:]").freeze();

        private Unicode32() {
        }

        static /* synthetic */ UnicodeSet access$100() {
            return INSTANCE;
        }
    }

    private static final class FCDModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Norm2AllModes.getFCDNormalizer2());

        private FCDModeImpl() {
        }
    }

    private static final class NFKCModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Norm2AllModes.getNFKCInstance().comp);

        private NFKCModeImpl() {
        }
    }

    private static final class NFCModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Norm2AllModes.getNFCInstance().comp);

        private NFCModeImpl() {
        }
    }

    private static final class NFKDModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Norm2AllModes.getNFKCInstance().decomp);

        private NFKDModeImpl() {
        }
    }

    private static final class NFDModeImpl {
        private static final ModeImpl INSTANCE = new ModeImpl(Norm2AllModes.getNFCInstance().decomp);

        private NFDModeImpl() {
        }
    }

    private static final class ModeImpl {
        private final Normalizer2 normalizer2;

        private ModeImpl(Normalizer2 n2) {
            this.normalizer2 = n2;
        }
    }
}

