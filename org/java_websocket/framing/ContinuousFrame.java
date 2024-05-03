/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.framing;

import org.java_websocket.enums.Opcode;
import org.java_websocket.framing.DataFrame;

public class ContinuousFrame
extends DataFrame {
    public ContinuousFrame() {
        super(Opcode.CONTINUOUS);
    }
}

