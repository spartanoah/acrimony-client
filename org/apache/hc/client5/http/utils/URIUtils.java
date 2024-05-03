/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TextUtils;

public class URIUtils {
    public static URI rewriteURI(URI uri, HttpHost target, boolean dropFragment) throws URISyntaxException {
        Args.notNull(uri, "URI");
        if (uri.isOpaque()) {
            return uri;
        }
        URIBuilder uribuilder = new URIBuilder(uri);
        if (target != null) {
            uribuilder.setScheme(target.getSchemeName());
            uribuilder.setHost(target.getHostName());
            uribuilder.setPort(target.getPort());
        } else {
            uribuilder.setScheme(null);
            uribuilder.setHost((String)null);
            uribuilder.setPort(-1);
        }
        if (dropFragment) {
            uribuilder.setFragment(null);
        }
        List<String> originalPathSegments = uribuilder.getPathSegments();
        ArrayList<String> pathSegments = new ArrayList<String>(originalPathSegments);
        Iterator it = pathSegments.iterator();
        while (it.hasNext()) {
            String pathSegment = (String)it.next();
            if (!pathSegment.isEmpty() || !it.hasNext()) continue;
            it.remove();
        }
        if (pathSegments.size() != originalPathSegments.size()) {
            uribuilder.setPathSegments(pathSegments);
        }
        if (pathSegments.isEmpty()) {
            uribuilder.setPathSegments("");
        }
        return uribuilder.build();
    }

    public static URI rewriteURI(URI uri, HttpHost target) throws URISyntaxException {
        return URIUtils.rewriteURI(uri, target, false);
    }

    public static URI rewriteURI(URI uri) throws URISyntaxException {
        Args.notNull(uri, "URI");
        if (uri.isOpaque()) {
            return uri;
        }
        URIBuilder uribuilder = new URIBuilder(uri);
        if (uribuilder.getUserInfo() != null) {
            uribuilder.setUserInfo(null);
        }
        if (TextUtils.isEmpty(uribuilder.getPath())) {
            uribuilder.setPath("/");
        }
        if (uribuilder.getHost() != null) {
            uribuilder.setHost(uribuilder.getHost().toLowerCase(Locale.ROOT));
        }
        uribuilder.setFragment(null);
        return uribuilder.build();
    }

    public static URI resolve(URI baseURI, String reference) {
        return URIUtils.resolve(baseURI, URI.create(reference));
    }

    public static URI resolve(URI baseURI, URI reference) {
        URI resolved;
        Args.notNull(baseURI, "Base URI");
        Args.notNull(reference, "Reference URI");
        String s = reference.toASCIIString();
        if (s.startsWith("?")) {
            String baseUri = baseURI.toASCIIString();
            int i = baseUri.indexOf(63);
            baseUri = i > -1 ? baseUri.substring(0, i) : baseUri;
            return URI.create(baseUri + s);
        }
        boolean emptyReference = s.isEmpty();
        if (emptyReference) {
            resolved = baseURI.resolve(URI.create("#"));
            String resolvedString = resolved.toASCIIString();
            resolved = URI.create(resolvedString.substring(0, resolvedString.indexOf(35)));
        } else {
            resolved = baseURI.resolve(reference);
        }
        try {
            return URIUtils.normalizeSyntax(resolved);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    static URI normalizeSyntax(URI uri) throws URISyntaxException {
        if (uri.isOpaque() || uri.getAuthority() == null) {
            return uri;
        }
        Args.check(uri.isAbsolute(), "Base URI must be absolute");
        URIBuilder builder = new URIBuilder(uri);
        String path = builder.getPath();
        if (path != null && !path.equals("/")) {
            String[] inputSegments = path.split("/");
            Stack<String> outputSegments = new Stack<String>();
            for (String inputSegment : inputSegments) {
                if (inputSegment.isEmpty() || ".".equals(inputSegment)) continue;
                if ("..".equals(inputSegment)) {
                    if (outputSegments.isEmpty()) continue;
                    outputSegments.pop();
                    continue;
                }
                outputSegments.push(inputSegment);
            }
            StringBuilder outputBuffer = new StringBuilder();
            for (String outputSegment : outputSegments) {
                outputBuffer.append('/').append(outputSegment);
            }
            if (path.lastIndexOf(47) == path.length() - 1) {
                outputBuffer.append('/');
            }
            builder.setPath(outputBuffer.toString());
        }
        if (builder.getScheme() != null) {
            builder.setScheme(builder.getScheme().toLowerCase(Locale.ROOT));
        }
        if (builder.getHost() != null) {
            builder.setHost(builder.getHost().toLowerCase(Locale.ROOT));
        }
        return builder.build();
    }

    public static HttpHost extractHost(URI uri) {
        if (uri == null) {
            return null;
        }
        if (uri.isAbsolute()) {
            if (uri.getHost() == null) {
                if (uri.getAuthority() != null) {
                    int port;
                    String hostname;
                    String content = uri.getAuthority();
                    int at = content.indexOf(64);
                    if (at != -1) {
                        content = content.substring(at + 1);
                    }
                    String scheme = uri.getScheme();
                    at = content.indexOf(":");
                    if (at != -1) {
                        hostname = content.substring(0, at);
                        try {
                            String portText = content.substring(at + 1);
                            port = !TextUtils.isEmpty(portText) ? Integer.parseInt(portText) : -1;
                        } catch (NumberFormatException ex) {
                            return null;
                        }
                    } else {
                        hostname = content;
                        port = -1;
                    }
                    try {
                        return new HttpHost(scheme, hostname, port);
                    } catch (IllegalArgumentException ex) {
                        return null;
                    }
                }
            } else {
                return new HttpHost(uri.getScheme(), uri.getHost(), uri.getPort());
            }
        }
        return null;
    }

    public static URI resolve(URI originalURI, HttpHost target, List<URI> redirects) throws URISyntaxException {
        URIBuilder uribuilder;
        Args.notNull(originalURI, "Request URI");
        if (redirects == null || redirects.isEmpty()) {
            uribuilder = new URIBuilder(originalURI);
        } else {
            uribuilder = new URIBuilder(redirects.get(redirects.size() - 1));
            String frag = uribuilder.getFragment();
            for (int i = redirects.size() - 1; frag == null && i >= 0; --i) {
                frag = redirects.get(i).getFragment();
            }
            uribuilder.setFragment(frag);
        }
        if (uribuilder.getFragment() == null) {
            uribuilder.setFragment(originalURI.getFragment());
        }
        if (target != null && !uribuilder.isAbsolute()) {
            uribuilder.setScheme(target.getSchemeName());
            uribuilder.setHost(target.getHostName());
            uribuilder.setPort(target.getPort());
        }
        return uribuilder.build();
    }

    public static URI create(HttpHost host, String path) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(path);
        if (host != null) {
            builder.setHost(host.getHostName()).setPort(host.getPort()).setScheme(host.getSchemeName());
        }
        return builder.build();
    }

    public static URI create(String scheme, URIAuthority host, String path) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(path);
        if (scheme != null) {
            builder.setScheme(scheme);
        }
        if (host != null) {
            builder.setHost(host.getHostName()).setPort(host.getPort());
        }
        return builder.build();
    }

    private URIUtils() {
    }
}

