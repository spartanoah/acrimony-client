/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.classic.methods;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.hc.core5.util.Args;

public class HttpOptions
extends HttpUriRequestBase {
    private static final long serialVersionUID = 1L;
    public static final String METHOD_NAME = "OPTIONS";

    public HttpOptions(URI uri) {
        super(METHOD_NAME, uri);
    }

    public HttpOptions(String uri) {
        this(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }

    public Set<String> getAllowedMethods(HttpResponse response) {
        Args.notNull(response, "HTTP response");
        Iterator<HeaderElement> it = MessageSupport.iterate(response, "Allow");
        HashSet<String> methods = new HashSet<String>();
        while (it.hasNext()) {
            HeaderElement element = it.next();
            methods.add(element.getName());
        }
        return methods;
    }
}

