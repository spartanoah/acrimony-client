/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.dns;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.dns.DnsOpCode;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsRecordDecoder;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.handler.codec.dns.DnsResponseCode;
import io.netty.handler.codec.dns.DnsSection;
import io.netty.util.internal.ObjectUtil;
import java.net.SocketAddress;

abstract class DnsResponseDecoder<A extends SocketAddress> {
    private final DnsRecordDecoder recordDecoder;

    DnsResponseDecoder(DnsRecordDecoder recordDecoder) {
        this.recordDecoder = ObjectUtil.checkNotNull(recordDecoder, "recordDecoder");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final DnsResponse decode(A sender, A recipient, ByteBuf buffer) throws Exception {
        int id = buffer.readUnsignedShort();
        int flags = buffer.readUnsignedShort();
        if (flags >> 15 == 0) {
            throw new CorruptedFrameException("not a response");
        }
        DnsResponse response = this.newResponse(sender, recipient, id, DnsOpCode.valueOf((byte)(flags >> 11 & 0xF)), DnsResponseCode.valueOf((byte)(flags & 0xF)));
        response.setRecursionDesired((flags >> 8 & 1) == 1);
        response.setAuthoritativeAnswer((flags >> 10 & 1) == 1);
        response.setTruncated((flags >> 9 & 1) == 1);
        response.setRecursionAvailable((flags >> 7 & 1) == 1);
        response.setZ(flags >> 4 & 7);
        boolean success = false;
        try {
            int questionCount = buffer.readUnsignedShort();
            int answerCount = buffer.readUnsignedShort();
            int authorityRecordCount = buffer.readUnsignedShort();
            int additionalRecordCount = buffer.readUnsignedShort();
            this.decodeQuestions(response, buffer, questionCount);
            if (!this.decodeRecords(response, DnsSection.ANSWER, buffer, answerCount)) {
                success = true;
                DnsResponse dnsResponse = response;
                return dnsResponse;
            }
            if (!this.decodeRecords(response, DnsSection.AUTHORITY, buffer, authorityRecordCount)) {
                success = true;
                DnsResponse dnsResponse = response;
                return dnsResponse;
            }
            this.decodeRecords(response, DnsSection.ADDITIONAL, buffer, additionalRecordCount);
            success = true;
            DnsResponse dnsResponse = response;
            return dnsResponse;
        } finally {
            if (!success) {
                response.release();
            }
        }
    }

    protected abstract DnsResponse newResponse(A var1, A var2, int var3, DnsOpCode var4, DnsResponseCode var5) throws Exception;

    private void decodeQuestions(DnsResponse response, ByteBuf buf, int questionCount) throws Exception {
        for (int i = questionCount; i > 0; --i) {
            response.addRecord(DnsSection.QUESTION, this.recordDecoder.decodeQuestion(buf));
        }
    }

    private boolean decodeRecords(DnsResponse response, DnsSection section, ByteBuf buf, int count) throws Exception {
        for (int i = count; i > 0; --i) {
            Object r = this.recordDecoder.decodeRecord(buf);
            if (r == null) {
                return false;
            }
            response.addRecord(section, (DnsRecord)r);
        }
        return true;
    }
}

