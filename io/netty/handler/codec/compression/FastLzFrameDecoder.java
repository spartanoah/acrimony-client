/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.compression;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.compression.ByteBufChecksum;
import io.netty.handler.codec.compression.DecompressionException;
import io.netty.handler.codec.compression.FastLz;
import io.netty.util.ReferenceCounted;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

public class FastLzFrameDecoder
extends ByteToMessageDecoder {
    private State currentState = State.INIT_BLOCK;
    private final ByteBufChecksum checksum;
    private int chunkLength;
    private int originalLength;
    private boolean isCompressed;
    private boolean hasChecksum;
    private int currentChecksum;

    public FastLzFrameDecoder() {
        this(false);
    }

    public FastLzFrameDecoder(boolean validateChecksums) {
        this(validateChecksums ? new Adler32() : null);
    }

    public FastLzFrameDecoder(Checksum checksum) {
        this.checksum = checksum == null ? null : ByteBufChecksum.wrapChecksum(checksum);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            switch (this.currentState) {
                case INIT_BLOCK: {
                    if (in.readableBytes() < 4) break;
                    int magic = in.readUnsignedMedium();
                    if (magic != 4607066) {
                        throw new DecompressionException("unexpected block identifier");
                    }
                    byte options = in.readByte();
                    this.isCompressed = (options & 1) == 1;
                    this.hasChecksum = (options & 0x10) == 16;
                    this.currentState = State.INIT_BLOCK_PARAMS;
                }
                case INIT_BLOCK_PARAMS: {
                    if (in.readableBytes() < 2 + (this.isCompressed ? 2 : 0) + (this.hasChecksum ? 4 : 0)) break;
                    this.currentChecksum = this.hasChecksum ? in.readInt() : 0;
                    this.chunkLength = in.readUnsignedShort();
                    this.originalLength = this.isCompressed ? in.readUnsignedShort() : this.chunkLength;
                    this.currentState = State.DECOMPRESS_DATA;
                }
                case DECOMPRESS_DATA: {
                    int chunkLength = this.chunkLength;
                    if (in.readableBytes() < chunkLength) break;
                    int idx = in.readerIndex();
                    int originalLength = this.originalLength;
                    ReferenceCounted output = null;
                    try {
                        if (this.isCompressed) {
                            int outputOffset;
                            output = ctx.alloc().buffer(originalLength);
                            int decompressedBytes = FastLz.decompress(in, idx, chunkLength, (ByteBuf)output, outputOffset = ((ByteBuf)output).writerIndex(), originalLength);
                            if (originalLength != decompressedBytes) {
                                throw new DecompressionException(String.format("stream corrupted: originalLength(%d) and actual length(%d) mismatch", originalLength, decompressedBytes));
                            }
                            ((ByteBuf)output).writerIndex(((ByteBuf)output).writerIndex() + decompressedBytes);
                        } else {
                            output = in.retainedSlice(idx, chunkLength);
                        }
                        ByteBufChecksum checksum = this.checksum;
                        if (this.hasChecksum && checksum != null) {
                            checksum.reset();
                            checksum.update((ByteBuf)output, ((ByteBuf)output).readerIndex(), ((ByteBuf)output).readableBytes());
                            int checksumResult = (int)checksum.getValue();
                            if (checksumResult != this.currentChecksum) {
                                throw new DecompressionException(String.format("stream corrupted: mismatching checksum: %d (expected: %d)", checksumResult, this.currentChecksum));
                            }
                        }
                        if (((ByteBuf)output).readableBytes() > 0) {
                            out.add(output);
                        } else {
                            output.release();
                        }
                        output = null;
                        in.skipBytes(chunkLength);
                        this.currentState = State.INIT_BLOCK;
                        break;
                    } finally {
                        if (output != null) {
                            output.release();
                        }
                    }
                }
                case CORRUPTED: {
                    in.skipBytes(in.readableBytes());
                    break;
                }
                default: {
                    throw new IllegalStateException();
                }
            }
        } catch (Exception e) {
            this.currentState = State.CORRUPTED;
            throw e;
        }
    }

    private static enum State {
        INIT_BLOCK,
        INIT_BLOCK_PARAMS,
        DECOMPRESS_DATA,
        CORRUPTED;

    }
}

