/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.http2;

import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.DefaultHttp2ConnectionDecoder;
import io.netty.handler.codec.http2.DefaultHttp2ConnectionEncoder;
import io.netty.handler.codec.http2.DefaultHttp2FrameReader;
import io.netty.handler.codec.http2.DefaultHttp2FrameWriter;
import io.netty.handler.codec.http2.DefaultHttp2HeadersDecoder;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2ControlFrameLimitEncoder;
import io.netty.handler.codec.http2.Http2EmptyDataFrameConnectionDecoder;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2FrameReader;
import io.netty.handler.codec.http2.Http2FrameWriter;
import io.netty.handler.codec.http2.Http2HeadersEncoder;
import io.netty.handler.codec.http2.Http2InboundFrameLogger;
import io.netty.handler.codec.http2.Http2OutboundFrameLogger;
import io.netty.handler.codec.http2.Http2PromisedRequestVerifier;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.StreamBufferingEncoder;
import io.netty.util.internal.ObjectUtil;

public abstract class AbstractHttp2ConnectionHandlerBuilder<T extends Http2ConnectionHandler, B extends AbstractHttp2ConnectionHandlerBuilder<T, B>> {
    private static final Http2HeadersEncoder.SensitivityDetector DEFAULT_HEADER_SENSITIVITY_DETECTOR = Http2HeadersEncoder.NEVER_SENSITIVE;
    private Http2Settings initialSettings = Http2Settings.defaultSettings();
    private Http2FrameListener frameListener;
    private long gracefulShutdownTimeoutMillis = Http2CodecUtil.DEFAULT_GRACEFUL_SHUTDOWN_TIMEOUT_MILLIS;
    private boolean decoupleCloseAndGoAway;
    private Boolean isServer;
    private Integer maxReservedStreams;
    private Http2Connection connection;
    private Http2ConnectionDecoder decoder;
    private Http2ConnectionEncoder encoder;
    private Boolean validateHeaders;
    private Http2FrameLogger frameLogger;
    private Http2HeadersEncoder.SensitivityDetector headerSensitivityDetector;
    private Boolean encoderEnforceMaxConcurrentStreams;
    private Boolean encoderIgnoreMaxHeaderListSize;
    private Http2PromisedRequestVerifier promisedRequestVerifier = Http2PromisedRequestVerifier.ALWAYS_VERIFY;
    private boolean autoAckSettingsFrame = true;
    private boolean autoAckPingFrame = true;
    private int maxQueuedControlFrames = 10000;
    private int maxConsecutiveEmptyFrames = 2;

    protected Http2Settings initialSettings() {
        return this.initialSettings;
    }

    protected B initialSettings(Http2Settings settings) {
        this.initialSettings = ObjectUtil.checkNotNull(settings, "settings");
        return this.self();
    }

    protected Http2FrameListener frameListener() {
        return this.frameListener;
    }

    protected B frameListener(Http2FrameListener frameListener) {
        this.frameListener = ObjectUtil.checkNotNull(frameListener, "frameListener");
        return this.self();
    }

    protected long gracefulShutdownTimeoutMillis() {
        return this.gracefulShutdownTimeoutMillis;
    }

    protected B gracefulShutdownTimeoutMillis(long gracefulShutdownTimeoutMillis) {
        if (gracefulShutdownTimeoutMillis < -1L) {
            throw new IllegalArgumentException("gracefulShutdownTimeoutMillis: " + gracefulShutdownTimeoutMillis + " (expected: -1 for indefinite or >= 0)");
        }
        this.gracefulShutdownTimeoutMillis = gracefulShutdownTimeoutMillis;
        return this.self();
    }

    protected boolean isServer() {
        return this.isServer != null ? this.isServer : true;
    }

    protected B server(boolean isServer) {
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("server", "connection", this.connection);
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("server", "codec", this.decoder);
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("server", "codec", this.encoder);
        this.isServer = isServer;
        return this.self();
    }

    protected int maxReservedStreams() {
        return this.maxReservedStreams != null ? this.maxReservedStreams : 100;
    }

    protected B maxReservedStreams(int maxReservedStreams) {
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("server", "connection", this.connection);
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("server", "codec", this.decoder);
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("server", "codec", this.encoder);
        this.maxReservedStreams = ObjectUtil.checkPositiveOrZero(maxReservedStreams, "maxReservedStreams");
        return this.self();
    }

    protected Http2Connection connection() {
        return this.connection;
    }

    protected B connection(Http2Connection connection) {
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("connection", "maxReservedStreams", this.maxReservedStreams);
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("connection", "server", this.isServer);
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("connection", "codec", this.decoder);
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("connection", "codec", this.encoder);
        this.connection = ObjectUtil.checkNotNull(connection, "connection");
        return this.self();
    }

    protected Http2ConnectionDecoder decoder() {
        return this.decoder;
    }

    protected Http2ConnectionEncoder encoder() {
        return this.encoder;
    }

    protected B codec(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder) {
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("codec", "server", this.isServer);
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("codec", "maxReservedStreams", this.maxReservedStreams);
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("codec", "connection", this.connection);
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("codec", "frameLogger", this.frameLogger);
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("codec", "validateHeaders", this.validateHeaders);
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("codec", "headerSensitivityDetector", this.headerSensitivityDetector);
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint("codec", "encoderEnforceMaxConcurrentStreams", this.encoderEnforceMaxConcurrentStreams);
        ObjectUtil.checkNotNull(decoder, "decoder");
        ObjectUtil.checkNotNull(encoder, "encoder");
        if (decoder.connection() != encoder.connection()) {
            throw new IllegalArgumentException("The specified encoder and decoder have different connections.");
        }
        this.decoder = decoder;
        this.encoder = encoder;
        return this.self();
    }

    protected boolean isValidateHeaders() {
        return this.validateHeaders != null ? this.validateHeaders : true;
    }

    protected B validateHeaders(boolean validateHeaders) {
        this.enforceNonCodecConstraints("validateHeaders");
        this.validateHeaders = validateHeaders;
        return this.self();
    }

    protected Http2FrameLogger frameLogger() {
        return this.frameLogger;
    }

    protected B frameLogger(Http2FrameLogger frameLogger) {
        this.enforceNonCodecConstraints("frameLogger");
        this.frameLogger = ObjectUtil.checkNotNull(frameLogger, "frameLogger");
        return this.self();
    }

    protected boolean encoderEnforceMaxConcurrentStreams() {
        return this.encoderEnforceMaxConcurrentStreams != null ? this.encoderEnforceMaxConcurrentStreams : false;
    }

    protected B encoderEnforceMaxConcurrentStreams(boolean encoderEnforceMaxConcurrentStreams) {
        this.enforceNonCodecConstraints("encoderEnforceMaxConcurrentStreams");
        this.encoderEnforceMaxConcurrentStreams = encoderEnforceMaxConcurrentStreams;
        return this.self();
    }

    protected int encoderEnforceMaxQueuedControlFrames() {
        return this.maxQueuedControlFrames;
    }

    protected B encoderEnforceMaxQueuedControlFrames(int maxQueuedControlFrames) {
        this.enforceNonCodecConstraints("encoderEnforceMaxQueuedControlFrames");
        this.maxQueuedControlFrames = ObjectUtil.checkPositiveOrZero(maxQueuedControlFrames, "maxQueuedControlFrames");
        return this.self();
    }

    protected Http2HeadersEncoder.SensitivityDetector headerSensitivityDetector() {
        return this.headerSensitivityDetector != null ? this.headerSensitivityDetector : DEFAULT_HEADER_SENSITIVITY_DETECTOR;
    }

    protected B headerSensitivityDetector(Http2HeadersEncoder.SensitivityDetector headerSensitivityDetector) {
        this.enforceNonCodecConstraints("headerSensitivityDetector");
        this.headerSensitivityDetector = ObjectUtil.checkNotNull(headerSensitivityDetector, "headerSensitivityDetector");
        return this.self();
    }

    protected B encoderIgnoreMaxHeaderListSize(boolean ignoreMaxHeaderListSize) {
        this.enforceNonCodecConstraints("encoderIgnoreMaxHeaderListSize");
        this.encoderIgnoreMaxHeaderListSize = ignoreMaxHeaderListSize;
        return this.self();
    }

    @Deprecated
    protected B initialHuffmanDecodeCapacity(int initialHuffmanDecodeCapacity) {
        return this.self();
    }

    protected B promisedRequestVerifier(Http2PromisedRequestVerifier promisedRequestVerifier) {
        this.enforceNonCodecConstraints("promisedRequestVerifier");
        this.promisedRequestVerifier = ObjectUtil.checkNotNull(promisedRequestVerifier, "promisedRequestVerifier");
        return this.self();
    }

    protected Http2PromisedRequestVerifier promisedRequestVerifier() {
        return this.promisedRequestVerifier;
    }

    protected int decoderEnforceMaxConsecutiveEmptyDataFrames() {
        return this.maxConsecutiveEmptyFrames;
    }

    protected B decoderEnforceMaxConsecutiveEmptyDataFrames(int maxConsecutiveEmptyFrames) {
        this.enforceNonCodecConstraints("maxConsecutiveEmptyFrames");
        this.maxConsecutiveEmptyFrames = ObjectUtil.checkPositiveOrZero(maxConsecutiveEmptyFrames, "maxConsecutiveEmptyFrames");
        return this.self();
    }

    protected B autoAckSettingsFrame(boolean autoAckSettings) {
        this.enforceNonCodecConstraints("autoAckSettingsFrame");
        this.autoAckSettingsFrame = autoAckSettings;
        return this.self();
    }

    protected boolean isAutoAckSettingsFrame() {
        return this.autoAckSettingsFrame;
    }

    protected B autoAckPingFrame(boolean autoAckPingFrame) {
        this.enforceNonCodecConstraints("autoAckPingFrame");
        this.autoAckPingFrame = autoAckPingFrame;
        return this.self();
    }

    protected boolean isAutoAckPingFrame() {
        return this.autoAckPingFrame;
    }

    protected B decoupleCloseAndGoAway(boolean decoupleCloseAndGoAway) {
        this.decoupleCloseAndGoAway = decoupleCloseAndGoAway;
        return this.self();
    }

    protected boolean decoupleCloseAndGoAway() {
        return this.decoupleCloseAndGoAway;
    }

    protected T build() {
        if (this.encoder != null) {
            assert (this.decoder != null);
            return this.buildFromCodec(this.decoder, this.encoder);
        }
        Http2Connection connection = this.connection;
        if (connection == null) {
            connection = new DefaultHttp2Connection(this.isServer(), this.maxReservedStreams());
        }
        return this.buildFromConnection(connection);
    }

    private T buildFromConnection(Http2Connection connection) {
        Http2FrameWriter writer;
        Long maxHeaderListSize = this.initialSettings.maxHeaderListSize();
        Http2FrameReader reader = new DefaultHttp2FrameReader(new DefaultHttp2HeadersDecoder(this.isValidateHeaders(), maxHeaderListSize == null ? 8192L : maxHeaderListSize, -1));
        DefaultHttp2FrameWriter defaultHttp2FrameWriter = writer = this.encoderIgnoreMaxHeaderListSize == null ? new DefaultHttp2FrameWriter(this.headerSensitivityDetector()) : new DefaultHttp2FrameWriter(this.headerSensitivityDetector(), this.encoderIgnoreMaxHeaderListSize);
        if (this.frameLogger != null) {
            reader = new Http2InboundFrameLogger(reader, this.frameLogger);
            writer = new Http2OutboundFrameLogger(writer, this.frameLogger);
        }
        Http2ConnectionEncoder encoder = new DefaultHttp2ConnectionEncoder(connection, writer);
        boolean encoderEnforceMaxConcurrentStreams = this.encoderEnforceMaxConcurrentStreams();
        if (this.maxQueuedControlFrames != 0) {
            encoder = new Http2ControlFrameLimitEncoder(encoder, this.maxQueuedControlFrames);
        }
        if (encoderEnforceMaxConcurrentStreams) {
            if (connection.isServer()) {
                encoder.close();
                reader.close();
                throw new IllegalArgumentException("encoderEnforceMaxConcurrentStreams: " + encoderEnforceMaxConcurrentStreams + " not supported for server");
            }
            encoder = new StreamBufferingEncoder(encoder);
        }
        DefaultHttp2ConnectionDecoder decoder = new DefaultHttp2ConnectionDecoder(connection, encoder, reader, this.promisedRequestVerifier(), this.isAutoAckSettingsFrame(), this.isAutoAckPingFrame());
        return this.buildFromCodec(decoder, encoder);
    }

    private T buildFromCodec(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder) {
        T handler;
        int maxConsecutiveEmptyDataFrames = this.decoderEnforceMaxConsecutiveEmptyDataFrames();
        if (maxConsecutiveEmptyDataFrames > 0) {
            decoder = new Http2EmptyDataFrameConnectionDecoder(decoder, maxConsecutiveEmptyDataFrames);
        }
        try {
            handler = this.build(decoder, encoder, this.initialSettings);
        } catch (Throwable t) {
            encoder.close();
            decoder.close();
            throw new IllegalStateException("failed to build an Http2ConnectionHandler", t);
        }
        ((Http2ConnectionHandler)handler).gracefulShutdownTimeoutMillis(this.gracefulShutdownTimeoutMillis);
        if (((Http2ConnectionHandler)handler).decoder().frameListener() == null) {
            ((Http2ConnectionHandler)handler).decoder().frameListener(this.frameListener);
        }
        return handler;
    }

    protected abstract T build(Http2ConnectionDecoder var1, Http2ConnectionEncoder var2, Http2Settings var3) throws Exception;

    protected final B self() {
        return (B)this;
    }

    private void enforceNonCodecConstraints(String rejected) {
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint(rejected, "server/connection", this.decoder);
        AbstractHttp2ConnectionHandlerBuilder.enforceConstraint(rejected, "server/connection", this.encoder);
    }

    private static void enforceConstraint(String methodName, String rejectorName, Object value) {
        if (value != null) {
            throw new IllegalStateException(methodName + "() cannot be called because " + rejectorName + "() has been called already.");
        }
    }
}

