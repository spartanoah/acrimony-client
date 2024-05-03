/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.io.Serializable;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public final class StatusLine
implements Serializable {
    private static final long serialVersionUID = -2443303766890459269L;
    private final ProtocolVersion protoVersion;
    private final int statusCode;
    private final StatusClass statusClass;
    private final String reasonPhrase;

    public StatusLine(HttpResponse response) {
        Args.notNull(response, "Response");
        this.protoVersion = response.getVersion() != null ? response.getVersion() : HttpVersion.HTTP_1_1;
        this.statusCode = response.getCode();
        this.statusClass = StatusClass.from(this.statusCode);
        this.reasonPhrase = response.getReasonPhrase();
    }

    public StatusLine(ProtocolVersion version, int statusCode, String reasonPhrase) {
        this.statusCode = Args.notNegative(statusCode, "Status code");
        this.statusClass = StatusClass.from(this.statusCode);
        this.protoVersion = version != null ? version : HttpVersion.HTTP_1_1;
        this.reasonPhrase = reasonPhrase;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public StatusClass getStatusClass() {
        return this.statusClass;
    }

    public ProtocolVersion getProtocolVersion() {
        return this.protoVersion;
    }

    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.protoVersion).append(" ").append(this.statusCode).append(" ");
        if (this.reasonPhrase != null) {
            buf.append(this.reasonPhrase);
        }
        return buf.toString();
    }

    public static enum StatusClass {
        INFORMATIONAL,
        SUCCESSFUL,
        REDIRECTION,
        CLIENT_ERROR,
        SERVER_ERROR,
        OTHER;


        public static StatusClass from(int statusCode) {
            StatusClass statusClass;
            switch (statusCode / 100) {
                case 1: {
                    statusClass = INFORMATIONAL;
                    break;
                }
                case 2: {
                    statusClass = SUCCESSFUL;
                    break;
                }
                case 3: {
                    statusClass = REDIRECTION;
                    break;
                }
                case 4: {
                    statusClass = CLIENT_ERROR;
                    break;
                }
                case 5: {
                    statusClass = SERVER_ERROR;
                    break;
                }
                default: {
                    statusClass = OTHER;
                }
            }
            return statusClass;
        }
    }
}

