/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.java_websocket.framing;

import org.java_websocket.enums.Opcode;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.DataFrame;
import org.java_websocket.util.Charsetfunctions;

public class TextFrame
extends DataFrame {
    public TextFrame() {
        super(Opcode.TEXT);
    }

    @Override
    public void isValid() throws InvalidDataException {
        super.isValid();
        if (!Charsetfunctions.isValidUTF8(this.getPayloadData())) {
            throw new InvalidDataException(1007, "Received text is no valid utf8 string!");
        }
    }
}

