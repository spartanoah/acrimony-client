/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.util;

import java.io.Serializable;

public abstract class NameTransformer {
    public static final NameTransformer NOP = new NopTransformer();

    protected NameTransformer() {
    }

    public static NameTransformer simpleTransformer(final String prefix, final String suffix) {
        boolean hasSuffix;
        boolean hasPrefix = prefix != null && prefix.length() > 0;
        boolean bl = hasSuffix = suffix != null && suffix.length() > 0;
        if (hasPrefix) {
            if (hasSuffix) {
                return new NameTransformer(){

                    @Override
                    public String transform(String name) {
                        return prefix + name + suffix;
                    }

                    @Override
                    public String reverse(String transformed) {
                        String str;
                        if (transformed.startsWith(prefix) && (str = transformed.substring(prefix.length())).endsWith(suffix)) {
                            return str.substring(0, str.length() - suffix.length());
                        }
                        return null;
                    }

                    public String toString() {
                        return "[PreAndSuffixTransformer('" + prefix + "','" + suffix + "')]";
                    }
                };
            }
            return new NameTransformer(){

                @Override
                public String transform(String name) {
                    return prefix + name;
                }

                @Override
                public String reverse(String transformed) {
                    if (transformed.startsWith(prefix)) {
                        return transformed.substring(prefix.length());
                    }
                    return null;
                }

                public String toString() {
                    return "[PrefixTransformer('" + prefix + "')]";
                }
            };
        }
        if (hasSuffix) {
            return new NameTransformer(){

                @Override
                public String transform(String name) {
                    return name + suffix;
                }

                @Override
                public String reverse(String transformed) {
                    if (transformed.endsWith(suffix)) {
                        return transformed.substring(0, transformed.length() - suffix.length());
                    }
                    return null;
                }

                public String toString() {
                    return "[SuffixTransformer('" + suffix + "')]";
                }
            };
        }
        return NOP;
    }

    public static NameTransformer chainedTransformer(NameTransformer t1, NameTransformer t2) {
        return new Chained(t1, t2);
    }

    public abstract String transform(String var1);

    public abstract String reverse(String var1);

    public static class Chained
    extends NameTransformer
    implements Serializable {
        private static final long serialVersionUID = 1L;
        protected final NameTransformer _t1;
        protected final NameTransformer _t2;

        public Chained(NameTransformer t1, NameTransformer t2) {
            this._t1 = t1;
            this._t2 = t2;
        }

        @Override
        public String transform(String name) {
            return this._t1.transform(this._t2.transform(name));
        }

        @Override
        public String reverse(String transformed) {
            if ((transformed = this._t1.reverse(transformed)) != null) {
                transformed = this._t2.reverse(transformed);
            }
            return transformed;
        }

        public String toString() {
            return "[ChainedTransformer(" + this._t1 + ", " + this._t2 + ")]";
        }
    }

    protected static final class NopTransformer
    extends NameTransformer
    implements Serializable {
        private static final long serialVersionUID = 1L;

        protected NopTransformer() {
        }

        @Override
        public String transform(String name) {
            return name;
        }

        @Override
        public String reverse(String transformed) {
            return transformed;
        }
    }
}

