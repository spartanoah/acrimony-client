/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

import org.apache.hc.core5.http.HttpMessage;

public interface MessageCopier<T extends HttpMessage> {
    public T copy(T var1);
}

