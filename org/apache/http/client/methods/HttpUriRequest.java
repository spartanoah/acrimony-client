/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.client.methods;

import java.net.URI;
import org.apache.http.HttpRequest;

public interface HttpUriRequest
extends HttpRequest {
    public String getMethod();

    public URI getURI();

    public void abort() throws UnsupportedOperationException;

    public boolean isAborted();
}

