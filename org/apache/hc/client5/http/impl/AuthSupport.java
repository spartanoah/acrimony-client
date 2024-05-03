/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.Args;

@Internal
public class AuthSupport {
    public static void extractFromAuthority(String scheme, URIAuthority authority, CredentialsStore credentialsStore) {
        Args.notNull(credentialsStore, "Credentials store");
        if (authority == null) {
            return;
        }
        String userInfo = authority.getUserInfo();
        if (userInfo == null) {
            return;
        }
        int atColon = userInfo.indexOf(58);
        String userName = atColon >= 0 ? userInfo.substring(0, atColon) : userInfo;
        char[] password = atColon >= 0 ? userInfo.substring(atColon + 1).toCharArray() : null;
        credentialsStore.setCredentials(new AuthScope(scheme, authority.getHostName(), authority.getPort(), null, "Basic"), new UsernamePasswordCredentials(userName, password));
    }

    public static HttpHost resolveAuthTarget(HttpRequest request, HttpRoute route) {
        HttpHost target;
        Args.notNull(request, "Request");
        Args.notNull(route, "Route");
        URIAuthority authority = request.getAuthority();
        String scheme = request.getScheme();
        HttpHost httpHost = target = authority != null ? new HttpHost(scheme, authority) : route.getTargetHost();
        if (target.getPort() < 0) {
            return new HttpHost(target.getSchemeName(), target.getHostName(), route.getTargetHost().getPort());
        }
        return target;
    }
}

