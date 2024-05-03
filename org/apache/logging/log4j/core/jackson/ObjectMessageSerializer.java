/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import java.io.IOException;
import org.apache.logging.log4j.message.ObjectMessage;

final class ObjectMessageSerializer
extends StdScalarSerializer<ObjectMessage> {
    private static final long serialVersionUID = 1L;

    ObjectMessageSerializer() {
        super(ObjectMessage.class);
    }

    @Override
    public void serialize(ObjectMessage value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeObject(value.getParameter());
    }
}

