/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class InetSocketAddressSerializer
extends StdScalarSerializer<InetSocketAddress> {
    public InetSocketAddressSerializer() {
        super(InetSocketAddress.class);
    }

    @Override
    public void serialize(InetSocketAddress value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        InetAddress addr = value.getAddress();
        String str = addr == null ? value.getHostName() : addr.toString().trim();
        int ix = str.indexOf(47);
        if (ix >= 0) {
            str = ix == 0 ? (addr instanceof Inet6Address ? "[" + str.substring(1) + "]" : str.substring(1)) : str.substring(0, ix);
        }
        jgen.writeString(str + ":" + value.getPort());
    }

    @Override
    public void serializeWithType(InetSocketAddress value, JsonGenerator g, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId((Object)value, InetSocketAddress.class, JsonToken.VALUE_STRING));
        this.serialize(value, g, provider);
        typeSer.writeTypeSuffix(g, typeIdDef);
    }
}

