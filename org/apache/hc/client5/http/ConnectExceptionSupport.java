/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.net.NamedEndpoint;

@Internal
public final class ConnectExceptionSupport {
    public static ConnectTimeoutException createConnectTimeoutException(IOException cause, NamedEndpoint namedEndpoint, InetAddress ... remoteAddresses) {
        String message = "Connect to " + (namedEndpoint != null ? namedEndpoint : "remote endpoint") + (remoteAddresses != null && remoteAddresses.length > 0 ? " " + Arrays.asList(remoteAddresses) : "") + (cause != null && cause.getMessage() != null ? " failed: " + cause.getMessage() : " timed out");
        return new ConnectTimeoutException(message, namedEndpoint);
    }

    public static HttpHostConnectException createHttpHostConnectException(IOException cause, NamedEndpoint namedEndpoint, InetAddress ... remoteAddresses) {
        String message = "Connect to " + (namedEndpoint != null ? namedEndpoint : "remote endpoint") + (remoteAddresses != null && remoteAddresses.length > 0 ? " " + Arrays.asList(remoteAddresses) : "") + (cause != null && cause.getMessage() != null ? " failed: " + cause.getMessage() : " refused");
        return new HttpHostConnectException(message, namedEndpoint);
    }

    public static IOException enhance(IOException cause, NamedEndpoint namedEndpoint, InetAddress ... remoteAddresses) {
        if (cause instanceof SocketTimeoutException) {
            ConnectTimeoutException ex = ConnectExceptionSupport.createConnectTimeoutException(cause, namedEndpoint, remoteAddresses);
            ex.setStackTrace(cause.getStackTrace());
            return ex;
        }
        if (cause instanceof ConnectException) {
            if ("Connection timed out".equals(cause.getMessage())) {
                ConnectTimeoutException ex = ConnectExceptionSupport.createConnectTimeoutException(cause, namedEndpoint, remoteAddresses);
                ex.initCause(cause);
                return ex;
            }
            HttpHostConnectException ex = ConnectExceptionSupport.createHttpHostConnectException(cause, namedEndpoint, remoteAddresses);
            ex.setStackTrace(cause.getStackTrace());
            return ex;
        }
        return cause;
    }
}

