/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.cookie;

import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.cookie.CommonCookieAttributeHandler;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.BasicDomainHandler;
import org.apache.http.impl.cookie.BasicExpiresHandler;
import org.apache.http.impl.cookie.BasicMaxAgeHandler;
import org.apache.http.impl.cookie.BasicPathHandler;
import org.apache.http.impl.cookie.BasicSecureHandler;
import org.apache.http.impl.cookie.LaxExpiresHandler;
import org.apache.http.impl.cookie.LaxMaxAgeHandler;
import org.apache.http.impl.cookie.PublicSuffixDomainFilter;
import org.apache.http.impl.cookie.RFC6265LaxSpec;
import org.apache.http.impl.cookie.RFC6265StrictSpec;
import org.apache.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class RFC6265CookieSpecProvider
implements CookieSpecProvider {
    private final CompatibilityLevel compatibilityLevel;
    private final PublicSuffixMatcher publicSuffixMatcher;
    private volatile CookieSpec cookieSpec;

    public RFC6265CookieSpecProvider(CompatibilityLevel compatibilityLevel, PublicSuffixMatcher publicSuffixMatcher) {
        this.compatibilityLevel = compatibilityLevel != null ? compatibilityLevel : CompatibilityLevel.RELAXED;
        this.publicSuffixMatcher = publicSuffixMatcher;
    }

    public RFC6265CookieSpecProvider(PublicSuffixMatcher publicSuffixMatcher) {
        this(CompatibilityLevel.RELAXED, publicSuffixMatcher);
    }

    public RFC6265CookieSpecProvider() {
        this(CompatibilityLevel.RELAXED, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public CookieSpec create(HttpContext context) {
        if (this.cookieSpec == null) {
            RFC6265CookieSpecProvider rFC6265CookieSpecProvider = this;
            synchronized (rFC6265CookieSpecProvider) {
                if (this.cookieSpec == null) {
                    switch (this.compatibilityLevel) {
                        case STRICT: {
                            this.cookieSpec = new RFC6265StrictSpec(new CommonCookieAttributeHandler[]{new BasicPathHandler(), PublicSuffixDomainFilter.decorate((CommonCookieAttributeHandler)((Object)new BasicDomainHandler()), this.publicSuffixMatcher), new BasicMaxAgeHandler(), new BasicSecureHandler(), new BasicExpiresHandler(RFC6265StrictSpec.DATE_PATTERNS)});
                            break;
                        }
                        case IE_MEDIUM_SECURITY: {
                            this.cookieSpec = new RFC6265LaxSpec(new CommonCookieAttributeHandler[]{new BasicPathHandler(){

                                @Override
                                public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
                                }
                            }, PublicSuffixDomainFilter.decorate((CommonCookieAttributeHandler)((Object)new BasicDomainHandler()), this.publicSuffixMatcher), new BasicMaxAgeHandler(), new BasicSecureHandler(), new BasicExpiresHandler(RFC6265StrictSpec.DATE_PATTERNS)});
                            break;
                        }
                        default: {
                            this.cookieSpec = new RFC6265LaxSpec(new CommonCookieAttributeHandler[]{new BasicPathHandler(), PublicSuffixDomainFilter.decorate((CommonCookieAttributeHandler)((Object)new BasicDomainHandler()), this.publicSuffixMatcher), new LaxMaxAgeHandler(), new BasicSecureHandler(), new LaxExpiresHandler()});
                        }
                    }
                }
            }
        }
        return this.cookieSpec;
    }

    public static enum CompatibilityLevel {
        STRICT,
        RELAXED,
        IE_MEDIUM_SECURITY;

    }
}

