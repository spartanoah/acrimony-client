/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package me.xdrop.fuzzywuzzy.algorithms;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.xdrop.fuzzywuzzy.ToStringFunction;

public class DefaultStringFunction
implements ToStringFunction<String> {
    private static final String pattern = "(?ui)\\W";
    private static final Pattern r = DefaultStringFunction.compilePattern();

    public static String subNonAlphaNumeric(String in, String sub) {
        Matcher m = r.matcher(in);
        if (m.find()) {
            return m.replaceAll(sub);
        }
        return in;
    }

    @Override
    public String apply(String in) {
        in = DefaultStringFunction.subNonAlphaNumeric(in, " ");
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

