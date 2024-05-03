/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl;

public enum ChainElement {
    REDIRECT,
    COMPRESS,
    BACK_OFF,
    RETRY,
    CACHING,
    PROTOCOL,
    CONNECT,
    MAIN_TRANSPORT;

}

