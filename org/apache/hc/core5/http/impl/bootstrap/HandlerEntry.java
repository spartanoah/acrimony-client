/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.bootstrap;

final class HandlerEntry<T> {
    final String hostname;
    final String uriPattern;
    final T handler;

    public HandlerEntry(String hostname, String uriPattern, T handler) {
        this.hostname = hostname;
        this.uriPattern = uriPattern;
        this.handler = handler;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HandlerEntry [hostname=");
        builder.append(this.hostname);
        builder.append(", uriPattern=");
        builder.append(this.uriPattern);
        builder.append(", handler=");
        builder.append(this.handler);
        builder.append("]");
        return builder.toString();
    }
}

