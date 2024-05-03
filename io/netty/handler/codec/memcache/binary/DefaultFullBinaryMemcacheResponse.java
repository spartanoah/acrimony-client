/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.memcache.binary;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.memcache.binary.DefaultBinaryMemcacheResponse;
import io.netty.handler.codec.memcache.binary.FullBinaryMemcacheResponse;
import io.netty.util.internal.ObjectUtil;

public class DefaultFullBinaryMemcacheResponse
extends DefaultBinaryMemcacheResponse
implements FullBinaryMemcacheResponse {
    private final ByteBuf content;

    public DefaultFullBinaryMemcacheResponse(ByteBuf key, ByteBuf extras) {
        this(key, extras, Unpooled.buffer(0));
    }

    public DefaultFullBinaryMemcacheResponse(ByteBuf key, ByteBuf extras, ByteBuf content) {
        super(key, extras);
        this.content = ObjectUtil.checkNotNull(content, "content");
        this.setTotalBodyLength(this.keyLength() + this.extrasLength() + content.readableBytes());
    }

    @Override
    public ByteBuf content() {
        return this.content;
    }

    @Override
    public FullBinaryMemcacheResponse retain() {
        super.retain();
        return this;
    }

    @Override
    public FullBinaryMemcacheResponse retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public FullBinaryMemcacheResponse touch() {
        super.touch();
        return this;
    }

    @Override
    public FullBinaryMemcacheResponse touch(Object hint) {
        super.touch(hint);
        this.content.touch(hint);
        return this;
    }

    @Override
    protected void deallocate() {
        super.deallocate();
        this.content.release();
    }

    @Override
    public FullBinaryMemcacheResponse copy() {
        ByteBuf extras;
        ByteBuf key = this.key();
        if (key != null) {
            key = key.copy();
        }
        if ((extras = this.extras()) != null) {
            extras = extras.copy();
        }
        return this.newInstance(key, extras, this.content().copy());
    }

    @Override
    public FullBinaryMemcacheResponse duplicate() {
        ByteBuf extras;
        ByteBuf key = this.key();
        if (key != null) {
            key = key.duplicate();
        }
        if ((extras = this.extras()) != null) {
            extras = extras.duplicate();
        }
        return this.newInstance(key, extras, this.content().duplicate());
    }

    @Override
    public FullBinaryMemcacheResponse retainedDuplicate() {
        return this.replace(this.content().retainedDuplicate());
    }

    @Override
    public FullBinaryMemcacheResponse replace(ByteBuf content) {
        ByteBuf extras;
        ByteBuf key = this.key();
        if (key != null) {
            key = key.retainedDuplicate();
        }
        if ((extras = this.extras()) != null) {
            extras = extras.retainedDuplicate();
        }
        return this.newInstance(key, extras, content);
    }

    private FullBinaryMemcacheResponse newInstance(ByteBuf key, ByteBuf extras, ByteBuf content) {
        DefaultFullBinaryMemcacheResponse newInstance = new DefaultFullBinaryMemcacheResponse(key, extras, content);
        this.copyMeta(newInstance);
        return newInstance;
    }
}

