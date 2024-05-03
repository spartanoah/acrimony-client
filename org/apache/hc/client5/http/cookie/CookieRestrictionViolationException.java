/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.cookie;

import org.apache.hc.client5.http.cookie.MalformedCookieException;

public class CookieRestrictionViolationException
extends MalformedCookieException {
    private static final long serialVersionUID = 7371235577078589013L;

    public CookieRestrictionViolationException() {
    }

    public CookieRestrictionViolationException(String message) {
        super(message);
    }
}

