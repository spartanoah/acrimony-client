/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.impl.cookie;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.util.PublicSuffixList;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.cookie.CommonCookieAttributeHandler;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class PublicSuffixDomainFilter
implements CommonCookieAttributeHandler {
    private final CommonCookieAttributeHandler handler;
    private final PublicSuffixMatcher publicSuffixMatcher;
    private final Map<String, Boolean> localDomainMap;

    private static Map<String, Boolean> createLocalDomainMap() {
        ConcurrentHashMap<String, Boolean> map = new ConcurrentHashMap<String, Boolean>();
        map.put(".localhost.", Boolean.TRUE);
        map.put(".test.", Boolean.TRUE);
        map.put(".local.", Boolean.TRUE);
        map.put(".local", Boolean.TRUE);
        map.put(".localdomain", Boolean.TRUE);
        return map;
    }

    public PublicSuffixDomainFilter(CommonCookieAttributeHandler handler, PublicSuffixMatcher publicSuffixMatcher) {
        this.handler = Args.notNull(handler, "Cookie handler");
        this.publicSuffixMatcher = Args.notNull(publicSuffixMatcher, "Public suffix matcher");
        this.localDomainMap = PublicSuffixDomainFilter.createLocalDomainMap();
    }

    public PublicSuffixDomainFilter(CommonCookieAttributeHandler handler, PublicSuffixList suffixList) {
        Args.notNull(handler, "Cookie handler");
        Args.notNull(suffixList, "Public suffix list");
        this.handler = handler;
        this.publicSuffixMatcher = new PublicSuffixMatcher(suffixList.getRules(), suffixList.getExceptions());
        this.localDomainMap = PublicSuffixDomainFilter.createLocalDomainMap();
    }

    @Override
    public boolean match(Cookie cookie, CookieOrigin origin) {
        String domain;
        String host = cookie.getDomain();
        if (host == null) {
            return false;
        }
        int i = host.indexOf(46);
        if (i >= 0 ? !this.localDomainMap.containsKey(domain = host.substring(i)) && this.publicSuffixMatcher.matches(host) : !host.equalsIgnoreCase(origin.getHost()) && this.publicSuffixMatcher.matches(host)) {
            return false;
        }
        return this.handler.match(cookie, origin);
    }

    @Override
    public void parse(SetCookie cookie, String value) throws MalformedCookieException {
        this.handler.parse(cookie, value);
    }

    @Override
    public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
        this.handler.validate(cookie, origin);
    }

    @Override
    public String getAttributeName() {
        return this.handler.getAttributeName();
    }

    public static CommonCookieAttributeHandler decorate(CommonCookieAttributeHandler handler, PublicSuffixMatcher publicSuffixMatcher) {
        Args.notNull(handler, "Cookie attribute handler");
        return publicSuffixMatcher != null ? new PublicSuffixDomainFilter(handler, publicSuffixMatcher) : handler;
    }
}

