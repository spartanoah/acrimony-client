/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.protocols;

public interface IProtocol {
    public boolean acceptProvidedProtocol(String var1);

    public String getProvidedProtocol();

    public IProtocol copyInstance();

    public String toString();
}

