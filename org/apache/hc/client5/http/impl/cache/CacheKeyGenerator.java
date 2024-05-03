/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.cache;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.impl.cache.HttpCacheSupport;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.function.Resolver;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.MessageSupport;

@Contract(threading=ThreadingBehavior.STATELESS)
public class CacheKeyGenerator
implements Resolver<URI, String> {
    public static final CacheKeyGenerator INSTANCE = new CacheKeyGenerator();

    @Override
    public String resolve(URI uri) {
        return this.generateKey(uri);
    }

    public String generateKey(URI requestUri) {
        try {
            URI normalizeRequestUri = HttpCacheSupport.normalize(requestUri);
            return normalizeRequestUri.toASCIIString();
        } catch (URISyntaxException ex) {
            return requestUri.toASCIIString();
        }
    }

    public String generateKey(HttpHost host, HttpRequest request) {
        String s = HttpCacheSupport.getRequestUri(request, host);
        try {
            return this.generateKey(new URI(s));
        } catch (URISyntaxException ex) {
            return s;
        }
    }

    private String getFullHeaderValue(Header[] headers) {
        if (headers == null) {
            return "";
        }
        StringBuilder buf = new StringBuilder("");
        for (int i = 0; i < headers.length; ++i) {
            Header hdr = headers[i];
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(hdr.getValue().trim());
        }
        return buf.toString();
    }

    public String generateKey(HttpHost host, HttpRequest request, HttpCacheEntry entry) {
        if (!entry.hasVariants()) {
            return this.generateKey(host, request);
        }
        return this.generateVariantKey(request, entry) + this.generateKey(host, request);
    }

    public String generateVariantKey(HttpRequest req, HttpCacheEntry entry) {
        StringBuilder buf;
        ArrayList<String> variantHeaderNames = new ArrayList<String>();
        Iterator<HeaderElement> it = MessageSupport.iterate(entry, "Vary");
        while (it.hasNext()) {
            HeaderElement elt = it.next();
            variantHeaderNames.add(elt.getName());
        }
        Collections.sort(variantHeaderNames);
        try {
            buf = new StringBuilder("{");
            boolean first = true;
            for (String headerName : variantHeaderNames) {
                if (!first) {
                    buf.append("&");
                }
                buf.append(URLEncoder.encode(headerName, StandardCharsets.UTF_8.name()));
                buf.append("=");
                buf.append(URLEncoder.encode(this.getFullHeaderValue(req.getHeaders(headerName)), StandardCharsets.UTF_8.name()));
                first = false;
            }
            buf.append("}");
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("couldn't encode to UTF-8", uee);
        }
        return buf.toString();
    }
}

