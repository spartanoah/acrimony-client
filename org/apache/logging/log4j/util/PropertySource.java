/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.util.BiConsumer;

public interface PropertySource {
    public int getPriority();

    default public void forEach(BiConsumer<String, String> action) {
    }

    default public Collection<String> getPropertyNames() {
        return Collections.emptySet();
    }

    default public CharSequence getNormalForm(Iterable<? extends CharSequence> tokens) {
        return null;
    }

    default public String getProperty(String key) {
        return null;
    }

    default public boolean containsProperty(String key) {
        return false;
    }

    public static final class Util {
        private static final Pattern PREFIX_PATTERN = Pattern.compile("(^log4j2?[-._/]?|^org\\.apache\\.logging\\.log4j\\.)|(?=AsyncLogger(Config)?\\.)", 2);
        private static final Pattern PROPERTY_TOKENIZER = Pattern.compile("([A-Z]*[a-z0-9]+|[A-Z0-9]+)[-._/]?");
        private static final Map<CharSequence, List<CharSequence>> CACHE = new ConcurrentHashMap<CharSequence, List<CharSequence>>();

        public static List<CharSequence> tokenize(CharSequence value) {
            if (CACHE.containsKey(value)) {
                return CACHE.get(value);
            }
            ArrayList<CharSequence> tokens = new ArrayList<CharSequence>();
            int start = 0;
            Matcher prefixMatcher = PREFIX_PATTERN.matcher(value);
            if (prefixMatcher.find(start)) {
                start = prefixMatcher.end();
                Matcher matcher = PROPERTY_TOKENIZER.matcher(value);
                while (matcher.find(start)) {
                    tokens.add(matcher.group(1).toLowerCase());
                    start = matcher.end();
                }
            }
            CACHE.put(value, tokens);
            return tokens;
        }

        public static CharSequence joinAsCamelCase(Iterable<? extends CharSequence> tokens) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (CharSequence charSequence : tokens) {
                if (first) {
                    sb.append(charSequence);
                } else {
                    sb.append(Character.toUpperCase(charSequence.charAt(0)));
                    if (charSequence.length() > 1) {
                        sb.append(charSequence.subSequence(1, charSequence.length()));
                    }
                }
                first = false;
            }
            return sb.toString();
        }

        private Util() {
        }

        static {
            CACHE.put("disableThreadContext", Arrays.asList("disable", "thread", "context"));
            CACHE.put("disableThreadContextStack", Arrays.asList("disable", "thread", "context", "stack"));
            CACHE.put("disableThreadContextMap", Arrays.asList("disable", "thread", "context", "map"));
            CACHE.put("isThreadContextMapInheritable", Arrays.asList("is", "thread", "context", "map", "inheritable"));
        }
    }

    public static class Comparator
    implements java.util.Comparator<PropertySource>,
    Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(PropertySource o1, PropertySource o2) {
            return Integer.compare(Objects.requireNonNull(o1).getPriority(), Objects.requireNonNull(o2).getPriority());
        }
    }
}

