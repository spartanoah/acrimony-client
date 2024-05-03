/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.codec.binary;

import java.io.InputStream;
import org.apache.commons.codec.CodecPolicy;
import org.apache.commons.codec.binary.Base16;
import org.apache.commons.codec.binary.BaseNCodecInputStream;

public class Base16InputStream
extends BaseNCodecInputStream {
    public Base16InputStream(InputStream in) {
        this(in, false);
    }

    public Base16InputStream(InputStream in, boolean doEncode) {
        this(in, doEncode, false);
    }

    public Base16InputStream(InputStream in, boolean doEncode, boolean lowerCase) {
        this(in, doEncode, lowerCase, CodecPolicy.LENIENT);
    }

    public Base16InputStream(InputStream in, boolean doEncode, boolean lowerCase, CodecPolicy decodingPolicy) {
        super(in, new Base16(lowerCase, decodingPolicy), doEncode);
    }
}

