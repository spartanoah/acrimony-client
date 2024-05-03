/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.hc.client5.http.AuthenticationStrategy;
import org.apache.hc.client5.http.auth.AuthChallenge;
import org.apache.hc.client5.http.auth.AuthScheme;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.auth.ChallengeType;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Contract(threading=ThreadingBehavior.STATELESS)
public class DefaultAuthenticationStrategy
implements AuthenticationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultAuthenticationStrategy.class);
    public static final DefaultAuthenticationStrategy INSTANCE = new DefaultAuthenticationStrategy();
    private static final List<String> DEFAULT_SCHEME_PRIORITY = Collections.unmodifiableList(Arrays.asList("Negotiate", "Kerberos", "NTLM", "Digest", "Basic"));

    @Override
    public List<AuthScheme> select(ChallengeType challengeType, Map<String, AuthChallenge> challenges, HttpContext context) {
        Collection<String> authPrefs;
        Args.notNull(challengeType, "ChallengeType");
        Args.notNull(challenges, "Map of auth challenges");
        Args.notNull(context, "HTTP context");
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        ArrayList<AuthScheme> options = new ArrayList<AuthScheme>();
        Lookup<AuthSchemeFactory> registry = clientContext.getAuthSchemeRegistry();
        if (registry == null) {
            LOG.debug("Auth scheme registry not set in the context");
            return options;
        }
        RequestConfig config = clientContext.getRequestConfig();
        Collection<String> collection = authPrefs = challengeType == ChallengeType.TARGET ? config.getTargetPreferredAuthSchemes() : config.getProxyPreferredAuthSchemes();
        if (authPrefs == null) {
            authPrefs = DEFAULT_SCHEME_PRIORITY;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Authentication schemes in the order of preference: {}", (Object)authPrefs);
        }
        for (String schemeName : authPrefs) {
            AuthChallenge challenge = challenges.get(schemeName.toLowerCase(Locale.ROOT));
            if (challenge != null) {
                AuthSchemeFactory authSchemeFactory = registry.lookup(schemeName);
                if (authSchemeFactory == null) {
                    if (!LOG.isWarnEnabled()) continue;
                    LOG.warn("Authentication scheme {} not supported", (Object)schemeName);
                    continue;
                }
                AuthScheme authScheme = authSchemeFactory.create(context);
                options.add(authScheme);
                continue;
            }
            if (!LOG.isDebugEnabled()) continue;
            LOG.debug("Challenge for {} authentication scheme not available", (Object)schemeName);
        }
        return options;
    }
}

