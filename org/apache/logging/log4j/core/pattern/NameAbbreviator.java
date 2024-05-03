/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.pattern;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.core.util.Integers;
import org.apache.logging.log4j.util.PerformanceSensitive;

@PerformanceSensitive(value={"allocation"})
public abstract class NameAbbreviator {
    private static final NameAbbreviator DEFAULT = new NOPAbbreviator();

    public static NameAbbreviator getAbbreviator(String pattern) {
        if (pattern.length() > 0) {
            int i;
            String number;
            boolean isNegativeNumber;
            String trimmed = pattern.trim();
            if (trimmed.isEmpty()) {
                return DEFAULT;
            }
            if (trimmed.length() > 1 && trimmed.charAt(0) == '-') {
                isNegativeNumber = true;
                number = trimmed.substring(1);
            } else {
                isNegativeNumber = false;
                number = trimmed;
            }
            for (i = 0; i < number.length() && number.charAt(i) >= '0' && number.charAt(i) <= '9'; ++i) {
            }
            if (i == number.length()) {
                return new MaxElementAbbreviator(Integers.parseInt(number), isNegativeNumber ? MaxElementAbbreviator.Strategy.DROP : MaxElementAbbreviator.Strategy.RETAIN);
            }
            ArrayList<PatternAbbreviatorFragment> fragments = new ArrayList<PatternAbbreviatorFragment>(5);
            for (int pos = 0; pos < trimmed.length() && pos >= 0; ++pos) {
                int charCount;
                int ellipsisPos = pos;
                if (trimmed.charAt(pos) == '*') {
                    charCount = Integer.MAX_VALUE;
                    ++ellipsisPos;
                } else if (trimmed.charAt(pos) >= '0' && trimmed.charAt(pos) <= '9') {
                    charCount = trimmed.charAt(pos) - 48;
                    ++ellipsisPos;
                } else {
                    charCount = 0;
                }
                char ellipsis = '\u0000';
                if (ellipsisPos < trimmed.length() && (ellipsis = trimmed.charAt(ellipsisPos)) == '.') {
                    ellipsis = '\u0000';
                }
                fragments.add(new PatternAbbreviatorFragment(charCount, ellipsis));
                pos = trimmed.indexOf(46, pos);
                if (pos == -1) break;
            }
            return new PatternAbbreviator(fragments);
        }
        return DEFAULT;
    }

    public static NameAbbreviator getDefaultAbbreviator() {
        return DEFAULT;
    }

    public abstract void abbreviate(String var1, StringBuilder var2);

    private static final class PatternAbbreviator
    extends NameAbbreviator {
        private final PatternAbbreviatorFragment[] fragments;

        PatternAbbreviator(List<PatternAbbreviatorFragment> fragments) {
            if (fragments.isEmpty()) {
                throw new IllegalArgumentException("fragments must have at least one element");
            }
            this.fragments = fragments.toArray(PatternAbbreviatorFragment.EMPTY_ARRAY);
        }

        @Override
        public void abbreviate(String original, StringBuilder destination) {
            int originalIndex = 0;
            int iteration = 0;
            int originalLength = original.length();
            while (originalIndex >= 0 && originalIndex < originalLength) {
                originalIndex = this.fragment(iteration++).abbreviate(original, originalIndex, destination);
            }
        }

        PatternAbbreviatorFragment fragment(int index) {
            return this.fragments[Math.min(index, this.fragments.length - 1)];
        }
    }

    private static final class PatternAbbreviatorFragment {
        static final PatternAbbreviatorFragment[] EMPTY_ARRAY = new PatternAbbreviatorFragment[0];
        private final int charCount;
        private final char ellipsis;

        PatternAbbreviatorFragment(int charCount, char ellipsis) {
            this.charCount = charCount;
            this.ellipsis = ellipsis;
        }

        int abbreviate(String input, int inputIndex, StringBuilder buf) {
            int nextDot = input.indexOf(46, inputIndex);
            if (nextDot < 0) {
                buf.append(input, inputIndex, input.length());
                return nextDot;
            }
            if (nextDot - inputIndex > this.charCount) {
                buf.append(input, inputIndex, inputIndex + this.charCount);
                if (this.ellipsis != '\u0000') {
                    buf.append(this.ellipsis);
                }
                buf.append('.');
            } else {
                buf.append(input, inputIndex, nextDot + 1);
            }
            return nextDot + 1;
        }
    }

    private static class MaxElementAbbreviator
    extends NameAbbreviator {
        private final int count;
        private final Strategy strategy;

        public MaxElementAbbreviator(int count, Strategy strategy) {
            this.count = Math.max(count, strategy.minCount);
            this.strategy = strategy;
        }

        @Override
        public void abbreviate(String original, StringBuilder destination) {
            this.strategy.abbreviate(this.count, original, destination);
        }

        private static enum Strategy {
            DROP(0){

                @Override
                void abbreviate(int count, String original, StringBuilder destination) {
                    int start = 0;
                    for (int i = 0; i < count; ++i) {
                        int nextStart = original.indexOf(46, start);
                        if (nextStart == -1) {
                            destination.append(original);
                            return;
                        }
                        start = nextStart + 1;
                    }
                    destination.append(original, start, original.length());
                }
            }
            ,
            RETAIN(1){

                @Override
                void abbreviate(int count, String original, StringBuilder destination) {
                    int end = original.length() - 1;
                    for (int i = count; i > 0; --i) {
                        if ((end = original.lastIndexOf(46, end - 1)) != -1) continue;
                        destination.append(original);
                        return;
                    }
                    destination.append(original, end + 1, original.length());
                }
            };

            final int minCount;

            private Strategy(int minCount) {
                this.minCount = minCount;
            }

            abstract void abbreviate(int var1, String var2, StringBuilder var3);
        }
    }

    private static class NOPAbbreviator
    extends NameAbbreviator {
        @Override
        public void abbreviate(String original, StringBuilder destination) {
            destination.append(original);
        }
    }
}

