/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.ProtocolVersion;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public final class HttpVersion
extends ProtocolVersion {
    private static final long serialVersionUID = -5856653513894415344L;
    public static final String HTTP = "HTTP";
    public static final HttpVersion HTTP_0_9 = new HttpVersion(0, 9);
    public static final HttpVersion HTTP_1_0 = new HttpVersion(1, 0);
    public static final HttpVersion HTTP_1_1 = new HttpVersion(1, 1);
    public static final HttpVersion HTTP_2_0;
    public static final HttpVersion HTTP_2;
    public static final HttpVersion DEFAULT;
    public static final HttpVersion[] ALL;

    public static HttpVersion get(int major, int minor) {
        for (int i = 0; i < ALL.length; ++i) {
            if (!ALL[i].equals(major, minor)) continue;
            return ALL[i];
        }
        return new HttpVersion(major, minor);
    }

    public HttpVersion(int major, int minor) {
        super(HTTP, major, minor);
    }

    static {
        HTTP_2 = HTTP_2_0 = new HttpVersion(2, 0);
        DEFAULT = HTTP_1_1;
        ALL = new HttpVersion[]{HTTP_0_9, HTTP_1_0, HTTP_1_1, HTTP_2_0};
    }
}

