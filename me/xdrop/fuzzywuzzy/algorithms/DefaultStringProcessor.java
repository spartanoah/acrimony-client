/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy.algorithms;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.xdrop.fuzzywuzzy.StringProcessor;

@Deprecated
public class DefaultStringProcessor
extends StringProcessor {
    private static final String pattern = "[^\\p{Alnum}]";
    private static final Pattern r = DefaultStringProcessor.compilePattern();

    public static String subNonAlphaNumeric(String in, String sub) {
        Matcher m = r.matcher(in);
        if (m.find()) {
            return m.replaceAll(sub);
        }
        return in;
    }

    @Override
    public String process(String in) {
        in = DefaultStringProcessor.subNonAlphaNumeric(in, " ");
        in = in.toLowerCase();
        in = in.trim();
        return in;
    }

    private static Pattern compilePattern() {
        Pattern p;
        try {
            p = Pattern.compile(pattern, 256);
        } catch (IllegalArgumentException e) {
            p = Pattern.compile(pattern);
        }
        return p;
    }
}

