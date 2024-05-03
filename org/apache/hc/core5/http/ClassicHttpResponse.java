/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.io.Closeable;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;

public interface ClassicHttpResponse
extends HttpResponse,
HttpEntityContainer,
Closeable {
}

