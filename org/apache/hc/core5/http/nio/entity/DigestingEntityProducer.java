/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.nio.entity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TextUtils;

public class DigestingEntityProducer
implements AsyncEntityProducer {
    private final AsyncEntityProducer wrapped;
    private final MessageDigest digester;
    private volatile byte[] digest;

    public DigestingEntityProducer(String algo, AsyncEntityProducer wrapped) {
        this.wrapped = Args.notNull(wrapped, "Entity consumer");
        try {
            this.digester = MessageDigest.getInstance(algo);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalArgumentException("Unsupported digest algorithm: " + algo);
        }
    }

    @Override
    public boolean isRepeatable() {
        return this.wrapped.isRepeatable();
    }

    @Override
    public long getContentLength() {
        return this.wrapped.getContentLength();
    }

    @Override
    public String getContentType() {
        return this.wrapped.getContentType();
    }

    @Override
    public String getContentEncoding() {
        return this.wrapped.getContentEncoding();
    }

    @Override
    public boolean isChunked() {
        return this.wrapped.isChunked();
    }

    @Override
    public Set<String> getTrailerNames() {
        LinkedHashSet<String> allNames = new LinkedHashSet<String>();
        Set<String> names = this.wrapped.getTrailerNames();
        if (names != null) {
            allNames.addAll(names);
        }
        allNames.add("digest-algo");
        allNames.add("digest");
        return allNames;
    }

    @Override
    public int available() {
        return this.wrapped.available();
    }

    @Override
    public void produce(final DataStreamChannel channel) throws IOException {
        this.wrapped.produce(new DataStreamChannel(){

            @Override
            public void requestOutput() {
                channel.requestOutput();
            }

            @Override
            public int write(ByteBuffer src) throws IOException {
                ByteBuffer dup = src.duplicate();
                int writtenBytes = channel.write(src);
                if (writtenBytes > 0) {
                    dup.limit(dup.position() + writtenBytes);
                    DigestingEntityProducer.this.digester.update(dup);
                }
                return writtenBytes;
            }

            @Override
            public void endStream(List<? extends Header> trailers) throws IOException {
                DigestingEntityProducer.access$102(DigestingEntityProducer.this, DigestingEntityProducer.this.digester.digest());
                ArrayList<? extends Header> allTrailers = new ArrayList<Header>();
                if (trailers != null) {
                    allTrailers.addAll(trailers);
                }
                allTrailers.add(new BasicHeader("digest-algo", DigestingEntityProducer.this.digester.getAlgorithm()));
                allTrailers.add(new BasicHeader("digest", TextUtils.toHexString(DigestingEntityProducer.this.digest)));
                channel.endStream(allTrailers);
            }

            @Override
            public void endStream() throws IOException {
                this.endStream(null);
            }
        });
    }

    @Override
    public void failed(Exception cause) {
        this.wrapped.failed(cause);
    }

    @Override
    public void releaseResources() {
        this.wrapped.releaseResources();
    }

    public byte[] getDigest() {
        return this.digest;
    }

    static /* synthetic */ byte[] access$102(DigestingEntityProducer x0, byte[] x1) {
        x0.digest = x1;
        return x1;
    }
}

