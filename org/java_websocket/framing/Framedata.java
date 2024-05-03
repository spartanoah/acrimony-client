/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.framing;

import java.nio.ByteBuffer;
import org.java_websocket.enums.Opcode;

public interface Framedata {
    public boolean isFin();

    public boolean isRSV1();

    public boolean isRSV2();

    public boolean isRSV3();

    public boolean getTransfereMasked();

    public Opcode getOpcode();

    public ByteBuffer getPayloadData();

    public void append(Framedata var1);
}

