/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.util.CharArrayBuffer;

public interface FormattedHeader
extends Header {
    public CharArrayBuffer getBuffer();

    public int getValuePos();
}

