/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.http.client;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;

public interface CredentialsProvider {
    public void setCredentials(AuthScope var1, Credentials var2);

    public Credentials getCredentials(AuthScope var1);

    public void clear();
}

