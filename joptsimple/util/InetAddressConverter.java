/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package joptsimple.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class InetAddressConverter
implements ValueConverter<InetAddress> {
    @Override
    public InetAddress convert(String value) {
        try {
            return InetAddress.getByName(value);
        } catch (UnknownHostException e) {
            throw new ValueConversionException("Cannot convert value [" + value + " into an InetAddress", e);
        }
    }

    @Override
    public Class<InetAddress> valueType() {
        return InetAddress.class;
    }

    @Override
    public String valuePattern() {
        return null;
    }
}

