/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.auth;

import java.security.Principal;

public interface Credentials {
    public Principal getUserPrincipal();

    public char[] getPassword();
}

