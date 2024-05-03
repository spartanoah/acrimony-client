/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import java.util.Locale;
import org.apache.hc.core5.http.HttpMessage;

public interface HttpResponse
extends HttpMessage {
    public int getCode();

    public void setCode(int var1);

    public String getReasonPhrase();

    public void setReasonPhrase(String var1);

    public Locale getLocale();

    public void setLocale(Locale var1);
}

