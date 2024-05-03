/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package fr.litarvan.openauth;

import fr.litarvan.openauth.model.AuthError;

public class AuthenticationException
extends Exception {
    private AuthError model;

    public AuthenticationException(AuthError model) {
        super(model.getErrorMessage());
        this.model = model;
    }

    public AuthError getErrorModel() {
        return this.model;
    }
}

