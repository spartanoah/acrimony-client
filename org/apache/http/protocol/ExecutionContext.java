/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.protocol;

@Deprecated
public interface ExecutionContext {
    public static final String HTTP_CONNECTION = "http.connection";
    public static final String HTTP_REQUEST = "http.request";
    public static final String HTTP_RESPONSE = "http.response";
    public static final String HTTP_TARGET_HOST = "http.target_host";
    @Deprecated
    public static final String HTTP_PROXY_HOST = "http.proxy_host";
    public static final String HTTP_REQ_SENT = "http.request_sent";
}

