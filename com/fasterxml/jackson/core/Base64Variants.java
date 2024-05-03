/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.Base64Variant;

public final class Base64Variants {
    static final String STD_BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    public static final Base64Variant MIME = new Base64Variant("MIME", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", true, '=', 76);
    public static final Base64Variant MIME_NO_LINEFEEDS = new Base64Variant(MIME, "MIME-NO-LINEFEEDS", Integer.MAX_VALUE);
    public static final Base64Variant PEM = new Base64Variant(MIME, "PEM", true, '=', 64);
    public static final Base64Variant MODIFIED_FOR_URL;

    public static Base64Variant getDefaultVariant() {
        return MIME_NO_LINEFEEDS;
    }

    public static Base64Variant valueOf(String name) throws IllegalArgumentException {
        if (Base64Variants.MIME._name.equals(name)) {
            return MIME;
        }
        if (Base64Variants.MIME_NO_LINEFEEDS._name.equals(name)) {
            return MIME_NO_LINEFEEDS;
        }
        if (Base64Variants.PEM._name.equals(name)) {
            return PEM;
        }
        if (Base64Variants.MODIFIED_FOR_URL._name.equals(name)) {
            return MODIFIED_FOR_URL;
        }
        name = name == null ? "<null>" : "'" + name + "'";
        throw new IllegalArgumentException("No Base64Variant with name " + name);
    }

    static {
        StringBuilder sb = new StringBuilder(STD_BASE64_ALPHABET);
        sb.setCharAt(sb.indexOf("+"), '-');
        sb.setCharAt(sb.indexOf("/"), '_');
        MODIFIED_FOR_URL = new Base64Variant("MODIFIED-FOR-URL", sb.toString(), false, '\u0000', Integer.MAX_VALUE);
    }
}

