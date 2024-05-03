/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl;

import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.MethodNotSupportedException;
import org.apache.hc.core5.http.NotImplementedException;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.RequestHeaderFieldsTooLargeException;
import org.apache.hc.core5.http.UnsupportedHttpVersionException;

@Internal
public class ServerSupport {
    public static void validateResponse(HttpResponse response, EntityDetails responseEntityDetails) throws HttpException {
        int status = response.getCode();
        switch (status) {
            case 204: 
            case 304: {
                if (responseEntityDetails == null) break;
                throw new HttpException("Response " + status + " must not enclose an entity");
            }
        }
    }

    public static String toErrorMessage(Exception ex) {
        String message = ex.getMessage();
        return message != null ? message : ex.toString();
    }

    public static int toStatusCode(Exception ex) {
        int code = ex instanceof MethodNotSupportedException ? 501 : (ex instanceof UnsupportedHttpVersionException ? 505 : (ex instanceof NotImplementedException ? 501 : (ex instanceof RequestHeaderFieldsTooLargeException ? 431 : (ex instanceof ProtocolException ? 400 : 500))));
        return code;
    }
}

