/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.io.Serializable;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public final class RequestLine
implements Serializable {
    private static final long serialVersionUID = 2810581718468737193L;
    private final ProtocolVersion protoversion;
    private final String method;
    private final String uri;

    public RequestLine(HttpRequest request) {
        Args.notNull(request, "Request");
        this.method = request.getMethod();
        this.uri = request.getRequestUri();
        this.protoversion = request.getVersion() != null ? request.getVersion() : HttpVersion.HTTP_1_1;
    }

    public RequestLine(String method, String uri, ProtocolVersion version) {
        this.method = Args.notNull(method, "Method");
        this.uri = Args.notNull(uri, "URI");
        this.protoversion = version != null ? version : HttpVersion.HTTP_1_1;
    }

    public String getMethod() {
        return this.method;
    }

    public ProtocolVersion getProtocolVersion() {
        return this.protoversion;
    }

    public String getUri() {
        return this.uri;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.method).append(" ").append(this.uri).append(" ").append(this.protoversion);
        return buf.toString();
    }
}

