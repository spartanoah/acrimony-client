/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.framing;

import org.java_websocket.enums.Opcode;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.FramedataImpl1;

public abstract class DataFrame
extends FramedataImpl1 {
    public DataFrame(Opcode opcode) {
        super(opcode);
    }

    @Override
    public void isValid() throws InvalidDataException {
    }
}

