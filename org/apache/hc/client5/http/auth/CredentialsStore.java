/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.auth;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;

public interface CredentialsStore
extends CredentialsProvider {
    public void setCredentials(AuthScope var1, Credentials var2);

    public void clear();
}

