/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.pattern;

import org.apache.logging.log4j.util.PerformanceSensitive;

@PerformanceSensitive(value={"allocation"})
public final class FormattingInfo {
    private static final char[] SPACES = new char[]{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
    private static final char[] ZEROS = new char[]{'0', '0', '0', '0', '0', '0', '0', '0'};
    private static final FormattingInfo DEFAULT = new FormattingInfo(false, 0, Integer.MAX_VALUE, true);
    private final int minLength;
    private final int maxLength;
    private final boolean leftAlign;
    private final boolean leftTruncate;
    private final boolean zeroPad;
    public static final FormattingInfo[] EMPTY_ARRAY = new FormattingInfo[0];

    public FormattingInfo(boolean leftAlign, int minLength, int maxLength, boolean leftTruncate) {
        this(leftAlign, minLength, maxLength, leftTruncate, false);
    }

    public FormattingInfo(boolean leftAlign, int minLength, int maxLength, boolean leftTruncate, boolean zeroPad) {
        this.leftAlign = leftAlign;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.leftTruncate = leftTruncate;
        this.zeroPad = zeroPad;
    }

    public static FormattingInfo getDefault() {
        return DEFAULT;
    }

    public boolean isLeftAligned() {
        return this.leftAlign;
    }

    public boolean isLeftTruncate() {
        return this.leftTruncate;
    }

    public boolean isZeroPad() {
        return this.zeroPad;
    }

    public int getMinLength() {
        return this.minLength;
    }

    public int getMaxLength() {
        return this.maxLength;
    }

    public void format(int fieldStart, StringBuilder buffer) {
        int rawLength = buffer.length() - fieldStart;
        if (rawLength > this.maxLength) {
            if (this.leftTruncate) {
                buffer.delete(fieldStart, buffer.length() - this.maxLength);
            } else {
                buffer.delete(fieldStart + this.maxLength, fieldStart + buffer.length());
            }
        } else if (rawLength < this.minLength) {
            if (this.leftAlign) {
                int fieldEnd = buffer.length();
                buffer.setLength(fieldStart + this.minLength);
                for (int i = fieldEnd; i < buffer.length(); ++i) {
                    buffer.setCharAt(i, ' ');
                }
            } else {
                int padLength;
                char[] paddingArray;
                char[] cArray = paddingArray = this.zeroPad ? ZEROS : SPACES;
                for (padLength = this.minLength - rawLength; padLength > paddingArray.length; padLength -= paddingArray.length) {
                    buffer.insert(fieldStart, paddingArray);
                }
                buffer.insert(fieldStart, paddingArray, 0, padLength);
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("[leftAlign=");
        sb.append(this.leftAlign);
        sb.append(", maxLength=");
        sb.append(this.maxLength);
        sb.append(", minLength=");
        sb.append(this.minLength);
        sb.append(", leftTruncate=");
        sb.append(this.leftTruncate);
        sb.append(", zeroPad=");
        sb.append(this.zeroPad);
        sb.append(']');
        return sb.toString();
    }
}

