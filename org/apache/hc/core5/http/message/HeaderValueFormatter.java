/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.util.CharArrayBuffer;

public interface HeaderValueFormatter {
    public void formatElements(CharArrayBuffer var1, HeaderElement[] var2, boolean var3);

    public void formatHeaderElement(CharArrayBuffer var1, HeaderElement var2, boolean var3);

    public void formatParameters(CharArrayBuffer var1, NameValuePair[] var2, boolean var3);

    public void formatNameValuePair(CharArrayBuffer var1, NameValuePair var2, boolean var3);
}

