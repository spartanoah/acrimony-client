/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

public enum MessageState {
    IDLE,
    HEADERS,
    ACK,
    BODY,
    COMPLETE;

}

