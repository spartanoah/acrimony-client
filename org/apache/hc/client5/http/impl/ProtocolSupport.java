/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.net.URIAuthority;

@Internal
public final class ProtocolSupport {
    public static String getRequestUri(HttpRequest request) {
        URIAuthority authority = request.getAuthority();
        if (authority != null) {
            String path;
            StringBuilder buf = new StringBuilder();
            String scheme = request.getScheme();
            buf.append(scheme != null ? scheme : URIScheme.HTTP.id);
            buf.append("://");
            if (authority.getUserInfo() != null) {
                buf.append(authority.getUserInfo());
                buf.append("@");
            }
            buf.append(authority.getHostName());
            if (authority.getPort() != -1) {
                buf.append(":");
                buf.append(authority.getPort());
            }
            if ((path = request.getPath()) == null || !path.startsWith("/")) {
                buf.append("/");
            }
            if (path != null) {
                buf.append(path);
            }
            return buf.toString();
        }
        return request.getPath();
    }
}

