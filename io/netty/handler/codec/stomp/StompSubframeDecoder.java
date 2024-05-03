/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.stomp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.stomp.DefaultLastStompContentSubframe;
import io.netty.handler.codec.stomp.DefaultStompContentSubframe;
import io.netty.handler.codec.stomp.DefaultStompHeadersSubframe;
import io.netty.handler.codec.stomp.LastStompContentSubframe;
import io.netty.handler.codec.stomp.StompCommand;
import io.netty.handler.codec.stomp.StompHeaders;
import io.netty.util.ByteProcessor;
import io.netty.util.internal.AppendableCharSequence;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import java.util.List;

public class StompSubframeDecoder
extends ReplayingDecoder<State> {
    private static final int DEFAULT_CHUNK_SIZE = 8132;
    private static final int DEFAULT_MAX_LINE_LENGTH = 1024;
    private final Utf8LineParser commandParser;
    private final HeaderParser headerParser;
    private final int maxChunkSize;
    private int alreadyReadChunkSize;
    private LastStompContentSubframe lastContent;
    private long contentLength = -1L;

    public StompSubframeDecoder() {
        this(1024, 8132);
    }

    public StompSubframeDecoder(boolean validateHeaders) {
        this(1024, 8132, validateHeaders);
    }

    public StompSubframeDecoder(int maxLineLength, int maxChunkSize) {
        this(maxLineLength, maxChunkSize, false);
    }

    public StompSubframeDecoder(int maxLineLength, int maxChunkSize, boolean validateHeaders) {
        super(State.SKIP_CONTROL_CHARACTERS);
        ObjectUtil.checkPositive(maxLineLength, "maxLineLength");
        ObjectUtil.checkPositive(maxChunkSize, "maxChunkSize");
        this.maxChunkSize = maxChunkSize;
        this.commandParser = new Utf8LineParser(new AppendableCharSequence(16), maxLineLength);
        this.headerParser = new HeaderParser(new AppendableCharSequence(128), maxLineLength, validateHeaders);
    }

    /*
     * Unable to fully structure code
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (1.$SwitchMap$io$netty$handler$codec$stomp$StompSubframeDecoder$State[((State)this.state()).ordinal()]) {
            case 1: {
                StompSubframeDecoder.skipControlCharacters(in);
                this.checkpoint(State.READ_HEADERS);
            }
            case 2: {
                command = StompCommand.UNKNOWN;
                frame = null;
                try {
                    command = this.readCommand(in);
                    frame = new DefaultStompHeadersSubframe(command);
                    this.checkpoint(this.readHeaders(in, frame.headers()));
                    out.add(frame);
                    break;
                } catch (Exception e) {
                    if (frame == null) {
                        frame = new DefaultStompHeadersSubframe(command);
                    }
                    frame.setDecoderResult(DecoderResult.failure(e));
                    out.add(frame);
                    this.checkpoint(State.BAD_FRAME);
                    return;
                }
            }
            case 3: {
                in.skipBytes(this.actualReadableBytes());
                return;
            }
        }
        try {
            switch (1.$SwitchMap$io$netty$handler$codec$stomp$StompSubframeDecoder$State[((State)this.state()).ordinal()]) {
                case 4: {
                    toRead = in.readableBytes();
                    if (toRead == 0) {
                        return;
                    }
                    if (toRead > this.maxChunkSize) {
                        toRead = this.maxChunkSize;
                    }
                    if (this.contentLength < 0L) ** GOTO lbl47
                    remainingLength = (int)(this.contentLength - (long)this.alreadyReadChunkSize);
                    if (toRead > remainingLength) {
                        toRead = remainingLength;
                    }
                    chunkBuffer = ByteBufUtil.readBytes(ctx.alloc(), in, toRead);
                    if ((long)(this.alreadyReadChunkSize += toRead) < this.contentLength) ** GOTO lbl44
                    this.lastContent = new DefaultLastStompContentSubframe(chunkBuffer);
                    this.checkpoint(State.FINALIZE_FRAME_READ);
                    ** GOTO lbl61
lbl44:
                    // 1 sources

                    out.add(new DefaultStompContentSubframe(chunkBuffer));
                    return;
lbl47:
                    // 1 sources

                    nulIndex = ByteBufUtil.indexOf(in, in.readerIndex(), in.writerIndex(), (byte)0);
                    if (nulIndex != in.readerIndex()) ** GOTO lbl51
                    this.checkpoint(State.FINALIZE_FRAME_READ);
                    ** GOTO lbl61
lbl51:
                    // 1 sources

                    toRead = nulIndex > 0 ? nulIndex - in.readerIndex() : in.writerIndex() - in.readerIndex();
                    chunkBuffer = ByteBufUtil.readBytes(ctx.alloc(), in, toRead);
                    this.alreadyReadChunkSize += toRead;
                    if (nulIndex > 0) {
                        this.lastContent = new DefaultLastStompContentSubframe(chunkBuffer);
                        this.checkpoint(State.FINALIZE_FRAME_READ);
                    } else {
                        out.add(new DefaultStompContentSubframe(chunkBuffer));
                        return;
                    }
                }
lbl61:
                // 4 sources

                case 5: {
                    StompSubframeDecoder.skipNullCharacter(in);
                    if (this.lastContent == null) {
                        this.lastContent = LastStompContentSubframe.EMPTY_LAST_CONTENT;
                    }
                    out.add(this.lastContent);
                    this.resetDecoder();
                }
            }
        } catch (Exception e) {
            errorContent = new DefaultLastStompContentSubframe(Unpooled.EMPTY_BUFFER);
            errorContent.setDecoderResult(DecoderResult.failure(e));
            out.add(errorContent);
            this.checkpoint(State.BAD_FRAME);
        }
    }

    private StompCommand readCommand(ByteBuf in) {
        AppendableCharSequence commandSequence = this.commandParser.parse(in);
        if (commandSequence == null) {
            throw new DecoderException("Failed to read command from channel");
        }
        String commandStr = commandSequence.toString();
        try {
            return StompCommand.valueOf(commandStr);
        } catch (IllegalArgumentException iae) {
            throw new DecoderException("Cannot to parse command " + commandStr);
        }
    }

    private State readHeaders(ByteBuf buffer, StompHeaders headers) {
        boolean headerRead;
        while (headerRead = this.headerParser.parseHeader(headers, buffer)) {
        }
        if (headers.contains(StompHeaders.CONTENT_LENGTH)) {
            this.contentLength = StompSubframeDecoder.getContentLength(headers);
            if (this.contentLength == 0L) {
                return State.FINALIZE_FRAME_READ;
            }
        }
        return State.READ_CONTENT;
    }

    private static long getContentLength(StompHeaders headers) {
        long contentLength = headers.getLong(StompHeaders.CONTENT_LENGTH, 0L);
        if (contentLength < 0L) {
            throw new DecoderException(StompHeaders.CONTENT_LENGTH + " must be non-negative");
        }
        return contentLength;
    }

    private static void skipNullCharacter(ByteBuf buffer) {
        byte b = buffer.readByte();
        if (b != 0) {
            throw new IllegalStateException("unexpected byte in buffer " + b + " while expecting NULL byte");
        }
    }

    private static void skipControlCharacters(ByteBuf buffer) {
        byte b;
        while ((b = buffer.readByte()) == 13 || b == 10) {
        }
        buffer.readerIndex(buffer.readerIndex() - 1);
    }

    private void resetDecoder() {
        this.checkpoint(State.SKIP_CONTROL_CHARACTERS);
        this.contentLength = -1L;
        this.alreadyReadChunkSize = 0;
        this.lastContent = null;
    }

    private static final class HeaderParser
    extends Utf8LineParser {
        private final boolean validateHeaders;
        private String name;
        private boolean valid;

        HeaderParser(AppendableCharSequence charSeq, int maxLineLength, boolean validateHeaders) {
            super(charSeq, maxLineLength);
            this.validateHeaders = validateHeaders;
        }

        boolean parseHeader(StompHeaders headers, ByteBuf buf) {
            AppendableCharSequence value = super.parse(buf);
            if (value == null || this.name == null && value.length() == 0) {
                return false;
            }
            if (this.valid) {
                headers.add(this.name, value.toString());
            } else if (this.validateHeaders) {
                if (StringUtil.isNullOrEmpty((String)this.name)) {
                    throw new IllegalArgumentException("received an invalid header line '" + value + '\'');
                }
                String line = this.name + ':' + value;
                throw new IllegalArgumentException("a header value or name contains a prohibited character ':', " + line);
            }
            return true;
        }

        @Override
        public boolean process(byte nextByte) throws Exception {
            if (nextByte == 58) {
                if (this.name == null) {
                    AppendableCharSequence charSeq = this.charSequence();
                    if (charSeq.length() != 0) {
                        this.name = charSeq.substring(0, charSeq.length());
                        charSeq.reset();
                        this.valid = true;
                        return true;
                    }
                    this.name = "";
                } else {
                    this.valid = false;
                }
            }
            return super.process(nextByte);
        }

        @Override
        protected void reset() {
            this.name = null;
            this.valid = false;
            super.reset();
        }
    }

    private static class Utf8LineParser
    implements ByteProcessor {
        private final AppendableCharSequence charSeq;
        private final int maxLineLength;
        private int lineLength;
        private char interim;
        private boolean nextRead;

        Utf8LineParser(AppendableCharSequence charSeq, int maxLineLength) {
            this.charSeq = ObjectUtil.checkNotNull(charSeq, "charSeq");
            this.maxLineLength = maxLineLength;
        }

        AppendableCharSequence parse(ByteBuf byteBuf) {
            this.reset();
            int offset = byteBuf.forEachByte(this);
            if (offset == -1) {
                return null;
            }
            byteBuf.readerIndex(offset + 1);
            return this.charSeq;
        }

        AppendableCharSequence charSequence() {
            return this.charSeq;
        }

        @Override
        public boolean process(byte nextByte) throws Exception {
            if (nextByte == 13) {
                ++this.lineLength;
                return true;
            }
            if (nextByte == 10) {
                return false;
            }
            if (++this.lineLength > this.maxLineLength) {
                throw new TooLongFrameException("An STOMP line is larger than " + this.maxLineLength + " bytes.");
            }
            if (this.nextRead) {
                this.interim = (char)(this.interim | (nextByte & 0x3F) << 6);
                this.nextRead = false;
            } else if (this.interim != '\u0000') {
                this.charSeq.append((char)(this.interim | nextByte & 0x3F));
                this.interim = '\u0000';
            } else if (nextByte >= 0) {
                this.charSeq.append((char)nextByte);
            } else if ((nextByte & 0xE0) == 192) {
                this.interim = (char)((nextByte & 0x1F) << 6);
            } else {
                this.interim = (char)((nextByte & 0xF) << 12);
                this.nextRead = true;
            }
            return true;
        }

        protected void reset() {
            this.charSeq.reset();
            this.lineLength = 0;
            this.interim = '\u0000';
            this.nextRead = false;
        }
    }

    static enum State {
        SKIP_CONTROL_CHARACTERS,
        READ_HEADERS,
        READ_CONTENT,
        FINALIZE_FRAME_READ,
        BAD_FRAME,
        INVALID_CHUNK;

    }
}

