/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import org.apache.hc.core5.http.impl.BasicHttpTransportMetrics;
import org.apache.hc.core5.http.impl.nio.AbstractContentDecoder;
import org.apache.hc.core5.http.nio.FileContentDecoder;
import org.apache.hc.core5.http.nio.SessionInputBuffer;
import org.apache.hc.core5.util.Args;

public class IdentityDecoder
extends AbstractContentDecoder
implements FileContentDecoder {
    public IdentityDecoder(ReadableByteChannel channel, SessionInputBuffer buffer, BasicHttpTransportMetrics metrics) {
        super(channel, buffer, metrics);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        Args.notNull(dst, "Byte buffer");
        if (this.isCompleted()) {
            return -1;
        }
        int bytesRead = this.buffer.hasData() ? this.buffer.read(dst) : this.readFromChannel(dst);
        if (bytesRead == -1) {
            this.setCompleted();
        }
        return bytesRead;
    }

    @Override
    public long transfer(FileChannel dst, long position, long count) throws IOException {
        long bytesRead;
        if (dst == null) {
            return 0L;
        }
        if (this.isCompleted()) {
            return 0L;
        }
        if (this.buffer.hasData()) {
            int maxLen = this.buffer.length();
            dst.position(position);
            bytesRead = this.buffer.read(dst, count < (long)maxLen ? (int)count : maxLen);
        } else {
            if (this.channel.isOpen()) {
                if (position > dst.size()) {
                    throw new IOException("Position past end of file [" + position + " > " + dst.size() + "]");
                }
                bytesRead = dst.transferFrom(this.channel, position, count);
                if (count > 0L && bytesRead == 0L) {
                    bytesRead = this.buffer.fill(this.channel);
                }
            } else {
                bytesRead = -1L;
            }
            if (bytesRead > 0L) {
                this.metrics.incrementBytesTransferred(bytesRead);
            }
        }
        if (bytesRead == -1L) {
            this.setCompleted();
        }
        return bytesRead;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[identity; completed: ");
        sb.append(this.completed);
        sb.append("]");
        return sb.toString();
    }
}

