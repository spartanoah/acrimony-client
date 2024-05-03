/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.extensions;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.framing.Framedata;

public class DefaultExtension
implements IExtension {
    @Override
    public void decodeFrame(Framedata inputFrame) throws InvalidDataException {
    }

    @Override
    public void encodeFrame(Framedata inputFrame) {
    }

    @Override
    public boolean acceptProvidedExtensionAsServer(String inputExtension) {
        return true;
    }

    @Override
    public boolean acceptProvidedExtensionAsClient(String inputExtension) {
        return true;
    }

    @Override
    public void isFrameValid(Framedata inputFrame) throws InvalidDataException {
        if (inputFrame.isRSV1() || inputFrame.isRSV2() || inputFrame.isRSV3()) {
            throw new InvalidFrameException("bad rsv RSV1: " + inputFrame.isRSV1() + " RSV2: " + inputFrame.isRSV2() + " RSV3: " + inputFrame.isRSV3());
        }
    }

    @Override
    public String getProvidedExtensionAsClient() {
        return "";
    }

    @Override
    public String getProvidedExtensionAsServer() {
        return "";
    }

    @Override
    public IExtension copyInstance() {
        return new DefaultExtension();
    }

    @Override
    public void reset() {
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    public int hashCode() {
        return this.getClass().hashCode();
    }

    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass();
    }
}

