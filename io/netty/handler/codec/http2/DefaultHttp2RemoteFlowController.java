/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionAdapter;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2RemoteFlowController;
import io.netty.handler.codec.http2.Http2Stream;
import io.netty.handler.codec.http2.Http2StreamVisitor;
import io.netty.handler.codec.http2.StreamByteDistributor;
import io.netty.handler.codec.http2.WeightedFairQueueByteDistributor;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.util.ArrayDeque;
import java.util.Deque;

public class DefaultHttp2RemoteFlowController
implements Http2RemoteFlowController {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultHttp2RemoteFlowController.class);
    private static final int MIN_WRITABLE_CHUNK = 32768;
    private final Http2Connection connection;
    private final Http2Connection.PropertyKey stateKey;
    private final StreamByteDistributor streamByteDistributor;
    private final FlowState connectionState;
    private int initialWindowSize = 65535;
    private WritabilityMonitor monitor;
    private ChannelHandlerContext ctx;

    public DefaultHttp2RemoteFlowController(Http2Connection connection) {
        this(connection, (Http2RemoteFlowController.Listener)null);
    }

    public DefaultHttp2RemoteFlowController(Http2Connection connection, StreamByteDistributor streamByteDistributor) {
        this(connection, streamByteDistributor, null);
    }

    public DefaultHttp2RemoteFlowController(Http2Connection connection, Http2RemoteFlowController.Listener listener) {
        this(connection, new WeightedFairQueueByteDistributor(connection), listener);
    }

    public DefaultHttp2RemoteFlowController(Http2Connection connection, StreamByteDistributor streamByteDistributor, Http2RemoteFlowController.Listener listener) {
        this.connection = ObjectUtil.checkNotNull(connection, "connection");
        this.streamByteDistributor = ObjectUtil.checkNotNull(streamByteDistributor, "streamWriteDistributor");
        this.stateKey = connection.newKey();
        this.connectionState = new FlowState(connection.connectionStream());
        connection.connectionStream().setProperty(this.stateKey, this.connectionState);
        this.listener(listener);
        this.monitor.windowSize(this.connectionState, this.initialWindowSize);
        connection.addListener(new Http2ConnectionAdapter(){

            @Override
            public void onStreamAdded(Http2Stream stream) {
                stream.setProperty(DefaultHttp2RemoteFlowController.this.stateKey, new FlowState(stream));
            }

            @Override
            public void onStreamActive(Http2Stream stream) {
                DefaultHttp2RemoteFlowController.this.monitor.windowSize(DefaultHttp2RemoteFlowController.this.state(stream), DefaultHttp2RemoteFlowController.this.initialWindowSize);
            }

            @Override
            public void onStreamClosed(Http2Stream stream) {
                DefaultHttp2RemoteFlowController.this.state(stream).cancel(Http2Error.STREAM_CLOSED, null);
            }

            @Override
            public void onStreamHalfClosed(Http2Stream stream) {
                if (Http2Stream.State.HALF_CLOSED_LOCAL == stream.state()) {
                    DefaultHttp2RemoteFlowController.this.state(stream).cancel(Http2Error.STREAM_CLOSED, null);
                }
            }
        });
    }

    @Override
    public void channelHandlerContext(ChannelHandlerContext ctx) throws Http2Exception {
        this.ctx = ObjectUtil.checkNotNull(ctx, "ctx");
        this.channelWritabilityChanged();
        if (this.isChannelWritable()) {
            this.writePendingBytes();
        }
    }

    @Override
    public ChannelHandlerContext channelHandlerContext() {
        return this.ctx;
    }

    @Override
    public void initialWindowSize(int newWindowSize) throws Http2Exception {
        assert (this.ctx == null || this.ctx.executor().inEventLoop());
        this.monitor.initialWindowSize(newWindowSize);
    }

    @Override
    public int initialWindowSize() {
        return this.initialWindowSize;
    }

    @Override
    public int windowSize(Http2Stream stream) {
        return this.state(stream).windowSize();
    }

    @Override
    public boolean isWritable(Http2Stream stream) {
        return this.monitor.isWritable(this.state(stream));
    }

    @Override
    public void channelWritabilityChanged() throws Http2Exception {
        this.monitor.channelWritabilityChange();
    }

    @Override
    public void updateDependencyTree(int childStreamId, int parentStreamId, short weight, boolean exclusive) {
        assert (weight >= 1 && weight <= 256) : "Invalid weight";
        assert (childStreamId != parentStreamId) : "A stream cannot depend on itself";
        assert (childStreamId > 0 && parentStreamId >= 0) : "childStreamId must be > 0. parentStreamId must be >= 0.";
        this.streamByteDistributor.updateDependencyTree(childStreamId, parentStreamId, weight, exclusive);
    }

    private boolean isChannelWritable() {
        return this.ctx != null && this.isChannelWritable0();
    }

    private boolean isChannelWritable0() {
        return this.ctx.channel().isWritable();
    }

    @Override
    public void listener(Http2RemoteFlowController.Listener listener) {
        this.monitor = listener == null ? new WritabilityMonitor() : new ListenerWritabilityMonitor(listener);
    }

    @Override
    public void incrementWindowSize(Http2Stream stream, int delta) throws Http2Exception {
        assert (this.ctx == null || this.ctx.executor().inEventLoop());
        this.monitor.incrementWindowSize(this.state(stream), delta);
    }

    @Override
    public void addFlowControlled(Http2Stream stream, Http2RemoteFlowController.FlowControlled frame) {
        assert (this.ctx == null || this.ctx.executor().inEventLoop());
        ObjectUtil.checkNotNull(frame, "frame");
        try {
            this.monitor.enqueueFrame(this.state(stream), frame);
        } catch (Throwable t) {
            frame.error(this.ctx, t);
        }
    }

    @Override
    public boolean hasFlowControlled(Http2Stream stream) {
        return this.state(stream).hasFrame();
    }

    private FlowState state(Http2Stream stream) {
        return (FlowState)stream.getProperty(this.stateKey);
    }

    private int connectionWindowSize() {
        return this.connectionState.windowSize();
    }

    private int minUsableChannelBytes() {
        return Math.max(this.ctx.channel().config().getWriteBufferLowWaterMark(), 32768);
    }

    private int maxUsableChannelBytes() {
        int channelWritableBytes = (int)Math.min(Integer.MAX_VALUE, this.ctx.channel().bytesBeforeUnwritable());
        int usableBytes = channelWritableBytes > 0 ? Math.max(channelWritableBytes, this.minUsableChannelBytes()) : 0;
        return Math.min(this.connectionState.windowSize(), usableBytes);
    }

    private int writableBytes() {
        return Math.min(this.connectionWindowSize(), this.maxUsableChannelBytes());
    }

    @Override
    public void writePendingBytes() throws Http2Exception {
        this.monitor.writePendingBytes();
    }

    private final class ListenerWritabilityMonitor
    extends WritabilityMonitor
    implements Http2StreamVisitor {
        private final Http2RemoteFlowController.Listener listener;

        ListenerWritabilityMonitor(Http2RemoteFlowController.Listener listener) {
            this.listener = listener;
        }

        @Override
        public boolean visit(Http2Stream stream) throws Http2Exception {
            FlowState state = DefaultHttp2RemoteFlowController.this.state(stream);
            if (this.isWritable(state) != state.markedWritability()) {
                this.notifyWritabilityChanged(state);
            }
            return true;
        }

        @Override
        void windowSize(FlowState state, int initialWindowSize) {
            super.windowSize(state, initialWindowSize);
            try {
                this.checkStateWritability(state);
            } catch (Http2Exception e) {
                throw new RuntimeException("Caught unexpected exception from window", e);
            }
        }

        @Override
        void incrementWindowSize(FlowState state, int delta) throws Http2Exception {
            super.incrementWindowSize(state, delta);
            this.checkStateWritability(state);
        }

        @Override
        void initialWindowSize(int newWindowSize) throws Http2Exception {
            super.initialWindowSize(newWindowSize);
            if (this.isWritableConnection()) {
                this.checkAllWritabilityChanged();
            }
        }

        @Override
        void enqueueFrame(FlowState state, Http2RemoteFlowController.FlowControlled frame) throws Http2Exception {
            super.enqueueFrame(state, frame);
            this.checkConnectionThenStreamWritabilityChanged(state);
        }

        @Override
        void stateCancelled(FlowState state) {
            try {
                this.checkConnectionThenStreamWritabilityChanged(state);
            } catch (Http2Exception e) {
                throw new RuntimeException("Caught unexpected exception from checkAllWritabilityChanged", e);
            }
        }

        @Override
        void channelWritabilityChange() throws Http2Exception {
            if (DefaultHttp2RemoteFlowController.this.connectionState.markedWritability() != DefaultHttp2RemoteFlowController.this.isChannelWritable()) {
                this.checkAllWritabilityChanged();
            }
        }

        private void checkStateWritability(FlowState state) throws Http2Exception {
            if (this.isWritable(state) != state.markedWritability()) {
                if (state == DefaultHttp2RemoteFlowController.this.connectionState) {
                    this.checkAllWritabilityChanged();
                } else {
                    this.notifyWritabilityChanged(state);
                }
            }
        }

        private void notifyWritabilityChanged(FlowState state) {
            state.markedWritability(!state.markedWritability());
            try {
                this.listener.writabilityChanged(state.stream);
            } catch (Throwable cause) {
                logger.error("Caught Throwable from listener.writabilityChanged", cause);
            }
        }

        private void checkConnectionThenStreamWritabilityChanged(FlowState state) throws Http2Exception {
            if (this.isWritableConnection() != DefaultHttp2RemoteFlowController.this.connectionState.markedWritability()) {
                this.checkAllWritabilityChanged();
            } else if (this.isWritable(state) != state.markedWritability()) {
                this.notifyWritabilityChanged(state);
            }
        }

        private void checkAllWritabilityChanged() throws Http2Exception {
            DefaultHttp2RemoteFlowController.this.connectionState.markedWritability(this.isWritableConnection());
            DefaultHttp2RemoteFlowController.this.connection.forEachActiveStream(this);
        }
    }

    private class WritabilityMonitor
    implements StreamByteDistributor.Writer {
        private boolean inWritePendingBytes;
        private long totalPendingBytes;

        private WritabilityMonitor() {
        }

        @Override
        public final void write(Http2Stream stream, int numBytes) {
            DefaultHttp2RemoteFlowController.this.state(stream).writeAllocatedBytes(numBytes);
        }

        void channelWritabilityChange() throws Http2Exception {
        }

        void stateCancelled(FlowState state) {
        }

        void windowSize(FlowState state, int initialWindowSize) {
            state.windowSize(initialWindowSize);
        }

        void incrementWindowSize(FlowState state, int delta) throws Http2Exception {
            state.incrementStreamWindow(delta);
        }

        void enqueueFrame(FlowState state, Http2RemoteFlowController.FlowControlled frame) throws Http2Exception {
            state.enqueueFrame(frame);
        }

        final void incrementPendingBytes(int delta) {
            this.totalPendingBytes += (long)delta;
        }

        final boolean isWritable(FlowState state) {
            return this.isWritableConnection() && state.isWritable();
        }

        final void writePendingBytes() throws Http2Exception {
            if (this.inWritePendingBytes) {
                return;
            }
            this.inWritePendingBytes = true;
            try {
                int bytesToWrite = DefaultHttp2RemoteFlowController.this.writableBytes();
                while (DefaultHttp2RemoteFlowController.this.streamByteDistributor.distribute(bytesToWrite, this) && (bytesToWrite = DefaultHttp2RemoteFlowController.this.writableBytes()) > 0 && DefaultHttp2RemoteFlowController.this.isChannelWritable0()) {
                }
            } finally {
                this.inWritePendingBytes = false;
            }
        }

        void initialWindowSize(int newWindowSize) throws Http2Exception {
            ObjectUtil.checkPositiveOrZero(newWindowSize, "newWindowSize");
            final int delta = newWindowSize - DefaultHttp2RemoteFlowController.this.initialWindowSize;
            DefaultHttp2RemoteFlowController.this.initialWindowSize = newWindowSize;
            DefaultHttp2RemoteFlowController.this.connection.forEachActiveStream(new Http2StreamVisitor(){

                @Override
                public boolean visit(Http2Stream stream) throws Http2Exception {
                    DefaultHttp2RemoteFlowController.this.state(stream).incrementStreamWindow(delta);
                    return true;
                }
            });
            if (delta > 0 && DefaultHttp2RemoteFlowController.this.isChannelWritable()) {
                this.writePendingBytes();
            }
        }

        final boolean isWritableConnection() {
            return (long)DefaultHttp2RemoteFlowController.this.connectionState.windowSize() - this.totalPendingBytes > 0L && DefaultHttp2RemoteFlowController.this.isChannelWritable();
        }
    }

    private final class FlowState
    implements StreamByteDistributor.StreamState {
        private final Http2Stream stream;
        private final Deque<Http2RemoteFlowController.FlowControlled> pendingWriteQueue;
        private int window;
        private long pendingBytes;
        private boolean markedWritable;
        private boolean writing;
        private boolean cancelled;

        FlowState(Http2Stream stream) {
            this.stream = stream;
            this.pendingWriteQueue = new ArrayDeque<Http2RemoteFlowController.FlowControlled>(2);
        }

        boolean isWritable() {
            return (long)this.windowSize() > this.pendingBytes() && !this.cancelled;
        }

        @Override
        public Http2Stream stream() {
            return this.stream;
        }

        boolean markedWritability() {
            return this.markedWritable;
        }

        void markedWritability(boolean isWritable) {
            this.markedWritable = isWritable;
        }

        @Override
        public int windowSize() {
            return this.window;
        }

        void windowSize(int initialWindowSize) {
            this.window = initialWindowSize;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        int writeAllocatedBytes(int allocated) {
            int writtenBytes;
            int initialAllocated = allocated;
            Throwable cause = null;
            try {
                int maxBytes;
                Http2RemoteFlowController.FlowControlled frame;
                assert (!this.writing);
                this.writing = true;
                boolean writeOccurred = false;
                while (!(this.cancelled || (frame = this.peek()) == null || (maxBytes = Math.min(allocated, this.writableWindow())) <= 0 && frame.size() > 0)) {
                    writeOccurred = true;
                    int initialFrameSize = frame.size();
                    try {
                        frame.write(DefaultHttp2RemoteFlowController.this.ctx, Math.max(0, maxBytes));
                        if (frame.size() != 0) continue;
                        this.pendingWriteQueue.remove();
                        frame.writeComplete();
                    } finally {
                        allocated -= initialFrameSize - frame.size();
                    }
                }
                if (!writeOccurred) {
                    int n = -1;
                    return n;
                }
            } catch (Throwable t) {
                this.cancelled = true;
                cause = t;
            } finally {
                this.writing = false;
                int writtenBytes2 = initialAllocated - allocated;
                this.decrementPendingBytes(writtenBytes2, false);
                this.decrementFlowControlWindow(writtenBytes2);
                if (this.cancelled) {
                    this.cancel(Http2Error.INTERNAL_ERROR, cause);
                }
            }
            return writtenBytes;
        }

        int incrementStreamWindow(int delta) throws Http2Exception {
            if (delta > 0 && Integer.MAX_VALUE - delta < this.window) {
                throw Http2Exception.streamError(this.stream.id(), Http2Error.FLOW_CONTROL_ERROR, "Window size overflow for stream: %d", this.stream.id());
            }
            this.window += delta;
            DefaultHttp2RemoteFlowController.this.streamByteDistributor.updateStreamableBytes(this);
            return this.window;
        }

        private int writableWindow() {
            return Math.min(this.window, DefaultHttp2RemoteFlowController.this.connectionWindowSize());
        }

        @Override
        public long pendingBytes() {
            return this.pendingBytes;
        }

        void enqueueFrame(Http2RemoteFlowController.FlowControlled frame) {
            Http2RemoteFlowController.FlowControlled last = this.pendingWriteQueue.peekLast();
            if (last == null) {
                this.enqueueFrameWithoutMerge(frame);
                return;
            }
            int lastSize = last.size();
            if (last.merge(DefaultHttp2RemoteFlowController.this.ctx, frame)) {
                this.incrementPendingBytes(last.size() - lastSize, true);
                return;
            }
            this.enqueueFrameWithoutMerge(frame);
        }

        private void enqueueFrameWithoutMerge(Http2RemoteFlowController.FlowControlled frame) {
            this.pendingWriteQueue.offer(frame);
            this.incrementPendingBytes(frame.size(), true);
        }

        @Override
        public boolean hasFrame() {
            return !this.pendingWriteQueue.isEmpty();
        }

        private Http2RemoteFlowController.FlowControlled peek() {
            return this.pendingWriteQueue.peek();
        }

        void cancel(Http2Error error, Throwable cause) {
            this.cancelled = true;
            if (this.writing) {
                return;
            }
            Http2RemoteFlowController.FlowControlled frame = this.pendingWriteQueue.poll();
            if (frame != null) {
                Http2Exception exception = Http2Exception.streamError(this.stream.id(), error, cause, "Stream closed before write could take place", new Object[0]);
                do {
                    this.writeError(frame, exception);
                } while ((frame = this.pendingWriteQueue.poll()) != null);
            }
            DefaultHttp2RemoteFlowController.this.streamByteDistributor.updateStreamableBytes(this);
            DefaultHttp2RemoteFlowController.this.monitor.stateCancelled(this);
        }

        private void incrementPendingBytes(int numBytes, boolean updateStreamableBytes) {
            this.pendingBytes += (long)numBytes;
            DefaultHttp2RemoteFlowController.this.monitor.incrementPendingBytes(numBytes);
            if (updateStreamableBytes) {
                DefaultHttp2RemoteFlowController.this.streamByteDistributor.updateStreamableBytes(this);
            }
        }

        private void decrementPendingBytes(int bytes, boolean updateStreamableBytes) {
            this.incrementPendingBytes(-bytes, updateStreamableBytes);
        }

        private void decrementFlowControlWindow(int bytes) {
            try {
                int negativeBytes = -bytes;
                DefaultHttp2RemoteFlowController.this.connectionState.incrementStreamWindow(negativeBytes);
                this.incrementStreamWindow(negativeBytes);
            } catch (Http2Exception e) {
                throw new IllegalStateException("Invalid window state when writing frame: " + e.getMessage(), e);
            }
        }

        private void writeError(Http2RemoteFlowController.FlowControlled frame, Http2Exception cause) {
            assert (DefaultHttp2RemoteFlowController.this.ctx != null);
            this.decrementPendingBytes(frame.size(), true);
            frame.error(DefaultHttp2RemoteFlowController.this.ctx, cause);
        }
    }
}

