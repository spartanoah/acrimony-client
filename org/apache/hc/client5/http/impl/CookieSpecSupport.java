/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

import org.apache.hc.client5.http.cookie.CookieSpecFactory;
import org.apache.hc.client5.http.impl.cookie.IgnoreCookieSpecFactory;
import org.apache.hc.client5.http.impl.cookie.RFC6265CookieSpecFactory;
import org.apache.hc.client5.http.psl.PublicSuffixMatcher;
import org.apache.hc.client5.http.psl.PublicSuffixMatcherLoader;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.config.RegistryBuilder;

@Internal
public final class CookieSpecSupport {
    public static RegistryBuilder<CookieSpecFactory> createDefaultBuilder(PublicSuffixMatcher publicSuffixMatcher) {
        return RegistryBuilder.create().register("relaxed", new RFC6265CookieSpecFactory(RFC6265CookieSpecFactory.CompatibilityLevel.RELAXED, publicSuffixMatcher)).register("strict", new RFC6265CookieSpecFactory(RFC6265CookieSpecFactory.CompatibilityLevel.STRICT, publicSuffixMatcher)).register("ignore", (RFC6265CookieSpecFactory)((Object)new IgnoreCookieSpecFactory()));
    }

    public static RegistryBuilder<CookieSpecFactory> createDefaultBuilder() {
        return CookieSpecSupport.createDefaultBuilder(PublicSuffixMatcherLoader.getDefault());
    }

    public static Lookup<CookieSpecFactory> createDefault() {
        return CookieSpecSupport.createDefault(PublicSuffixMatcherLoader.getDefault());
    }

    public static Lookup<CookieSpecFactory> createDefault(PublicSuffixMatcher publicSuffixMatcher) {
        return CookieSpecSupport.createDefaultBuilder(publicSuffixMatcher).build();
    }

    private CookieSpecSupport() {
    }
}

