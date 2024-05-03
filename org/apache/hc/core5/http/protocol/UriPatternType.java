/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.protocol;

import org.apache.hc.core5.http.protocol.LookupRegistry;
import org.apache.hc.core5.http.protocol.UriPatternMatcher;
import org.apache.hc.core5.http.protocol.UriPatternOrderedMatcher;
import org.apache.hc.core5.http.protocol.UriRegexMatcher;

public enum UriPatternType {
    REGEX,
    URI_PATTERN,
    URI_PATTERN_IN_ORDER;


    public static <T> LookupRegistry<T> newMatcher(UriPatternType type) {
        if (type == null) {
            return new UriPatternMatcher();
        }
        switch (type) {
            case REGEX: {
                return new UriRegexMatcher();
            }
            case URI_PATTERN: {
                return new UriPatternMatcher();
            }
            case URI_PATTERN_IN_ORDER: {
                return new UriPatternOrderedMatcher();
            }
        }
        return new UriPatternMatcher();
    }
}

