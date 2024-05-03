/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http;

import java.util.List;
import java.util.Map;
import org.apache.hc.client5.http.auth.AuthChallenge;
import org.apache.hc.client5.http.auth.AuthScheme;
import org.apache.hc.client5.http.auth.ChallengeType;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.protocol.HttpContext;

@Contract(threading=ThreadingBehavior.STATELESS)
public interface AuthenticationStrategy {
    public List<AuthScheme> select(ChallengeType var1, Map<String, AuthChallenge> var2, HttpContext var3);
}

