/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.haproxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.haproxy.HAProxyTLV;
import io.netty.util.internal.StringUtil;
import java.util.Collections;
import java.util.List;

public final class HAProxySSLTLV
extends HAProxyTLV {
    private final int verify;
    private final List<HAProxyTLV> tlvs;
    private final byte clientBitField;

    public HAProxySSLTLV(int verify, byte clientBitField, List<HAProxyTLV> tlvs) {
        this(verify, clientBitField, tlvs, Unpooled.EMPTY_BUFFER);
    }

    HAProxySSLTLV(int verify, byte clientBitField, List<HAProxyTLV> tlvs, ByteBuf rawContent) {
        super(HAProxyTLV.Type.PP2_TYPE_SSL, (byte)32, rawContent);
        this.verify = verify;
        this.tlvs = Collections.unmodifiableList(tlvs);
        this.clientBitField = clientBitField;
    }

    public boolean isPP2ClientCertConn() {
        return (this.clientBitField & 2) != 0;
    }

    public boolean isPP2ClientSSL() {
        return (this.clientBitField & 1) != 0;
    }

    public boolean isPP2ClientCertSess() {
        return (this.clientBitField & 4) != 0;
    }

    public byte client() {
        return this.clientBitField;
    }

    public int verify() {
        return this.verify;
    }

    public List<HAProxyTLV> encapsulatedTLVs() {
        return this.tlvs;
    }

    @Override
    int contentNumBytes() {
        int tlvNumBytes = 0;
        for (int i = 0; i < this.tlvs.size(); ++i) {
            tlvNumBytes += this.tlvs.get(i).totalNumBytes();
        }
        return 5 + tlvNumBytes;
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + "(type: " + (Object)((Object)this.type()) + ", typeByteValue: " + this.typeByteValue() + ", client: " + this.client() + ", verify: " + this.verify() + ", numEncapsulatedTlvs: " + this.tlvs.size() + ')';
    }
}

