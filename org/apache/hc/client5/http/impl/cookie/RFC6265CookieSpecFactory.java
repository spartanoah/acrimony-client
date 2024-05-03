/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cookie;

import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieOrigin;
import org.apache.hc.client5.http.cookie.CookieSpec;
import org.apache.hc.client5.http.cookie.CookieSpecFactory;
import org.apache.hc.client5.http.cookie.MalformedCookieException;
import org.apache.hc.client5.http.impl.cookie.BasicDomainHandler;
import org.apache.hc.client5.http.impl.cookie.BasicExpiresHandler;
import org.apache.hc.client5.http.impl.cookie.BasicMaxAgeHandler;
import org.apache.hc.client5.http.impl.cookie.BasicPathHandler;
import org.apache.hc.client5.http.impl.cookie.BasicSecureHandler;
import org.apache.hc.client5.http.impl.cookie.LaxExpiresHandler;
import org.apache.hc.client5.http.impl.cookie.LaxMaxAgeHandler;
import org.apache.hc.client5.http.impl.cookie.PublicSuffixDomainFilter;
import org.apache.hc.client5.http.impl.cookie.RFC6265LaxSpec;
import org.apache.hc.client5.http.impl.cookie.RFC6265StrictSpec;
import org.apache.hc.client5.http.psl.PublicSuffixMatcher;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.SAFE)
public class RFC6265CookieSpecFactory
implements CookieSpecFactory {
    private final CompatibilityLevel compatibilityLevel;
    private final PublicSuffixMatcher publicSuffixMatcher;
    private volatile CookieSpec cookieSpec;

    public RFC6265CookieSpecFactory(CompatibilityLevel compatibilityLevel, PublicSuffixMatcher publicSuffixMatcher) {
        this.compatibilityLevel = compatibilityLevel != null ? compatibilityLevel : CompatibilityLevel.RELAXED;
        this.publicSuffixMatcher = publicSuffixMatcher;
    }

    public RFC6265CookieSpecFactory(PublicSuffixMatcher publicSuffixMatcher) {
        this(CompatibilityLevel.RELAXED, publicSuffixMatcher);
    }

    public RFC6265CookieSpecFactory() {
        this(CompatibilityLevel.RELAXED, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public CookieSpec create(HttpContext context) {
        if (this.cookieSpec == null) {
            RFC6265CookieSpecFactory rFC6265CookieSpecFactory = this;
            synchronized (rFC6265CookieSpecFactory) {
                if (this.cookieSpec == null) {
                    switch (this.compatibilityLevel) {
                        case STRICT: {
                            this.cookieSpec = new RFC6265StrictSpec(new BasicPathHandler(), PublicSuffixDomainFilter.decorate(new BasicDomainHandler(), this.publicSuffixMatcher), new BasicMaxAgeHandler(), new BasicSecureHandler(), new BasicExpiresHandler(RFC6265StrictSpec.DATE_PATTERNS));
                            break;
                        }
                        case IE_MEDIUM_SECURITY: {
                            this.cookieSpec = new RFC6265LaxSpec(new BasicPathHandler(){

                                @Override
                                public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
                                }
                            }, PublicSuffixDomainFilter.decorate(new BasicDomainHandler(), this.publicSuffixMatcher), new BasicMaxAgeHandler(), new BasicSecureHandler(), new BasicExpiresHandler(RFC6265StrictSpec.DATE_PATTERNS));
                            break;
                        }
                        default: {
                            this.cookieSpec = new RFC6265LaxSpec(new BasicPathHandler(), PublicSuffixDomainFilter.decorate(new BasicDomainHandler(), this.publicSuffixMatcher), new LaxMaxAgeHandler(), new BasicSecureHandler(), new LaxExpiresHandler());
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

