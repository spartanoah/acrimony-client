/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.dns;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsRecordEncoder;
import io.netty.handler.codec.dns.DnsSection;
import io.netty.util.internal.ObjectUtil;

final class DnsQueryEncoder {
    private final DnsRecordEncoder recordEncoder;

    DnsQueryEncoder(DnsRecordEncoder recordEncoder) {
        this.recordEncoder = ObjectUtil.checkNotNull(recordEncoder, "recordEncoder");
    }

    void encode(DnsQuery query, ByteBuf out) throws Exception {
        DnsQueryEncoder.encodeHeader(query, out);
        this.encodeQuestions(query, out);
        this.encodeRecords(query, DnsSection.ADDITIONAL, out);
    }

    private static void encodeHeader(DnsQuery query, ByteBuf buf) {
        buf.writeShort(query.id());
        int flags = 0;
        flags |= (query.opCode().byteValue() & 0xFF) << 14;
        if (query.isRecursionDesired()) {
            flags |= 0x100;
        }
        buf.writeShort(flags);
        buf.writeShort(query.count(DnsSection.QUESTION));
        buf.writeShort(0);
        buf.writeShort(0);
        buf.writeShort(query.count(DnsSection.ADDITIONAL));
    }

    private void encodeQuestions(DnsQuery query, ByteBuf buf) throws Exception {
        int count = query.count(DnsSection.QUESTION);
        for (int i = 0; i < count; ++i) {
            this.recordEncoder.encodeQuestion((DnsQuestion)query.recordAt(DnsSection.QUESTION, i), buf);
        }
    }

    private void encodeRecords(DnsQuery query, DnsSection section, ByteBuf buf) throws Exception {
        int count = query.count(section);
        for (int i = 0; i < count; ++i) {
            this.recordEncoder.encodeRecord((DnsRecord)query.recordAt(section, i), buf);
        }
    }
}

