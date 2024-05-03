/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.framing;

import org.java_websocket.enums.Opcode;
import org.java_websocket.framing.ControlFrame;

public class PingFrame
extends ControlFrame {
    public PingFrame() {
        super(Opcode.PING);
    }
}

