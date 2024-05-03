/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.util.internal.ReflectionUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.reflect.Field;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;

public abstract class SslMasterKeyHandler
extends ChannelInboundHandlerAdapter {
    private static final InternalLogger logger;
    private static final Class<?> SSL_SESSIONIMPL_CLASS;
    private static final Field SSL_SESSIONIMPL_MASTER_SECRET_FIELD;
    public static final String SYSTEM_PROP_KEY = "io.netty.ssl.masterKeyHandler";
    private static final Throwable UNAVAILABILITY_CAUSE;

    protected SslMasterKeyHandler() {
    }

    public static void ensureSunSslEngineAvailability() {
        if (UNAVAILABILITY_CAUSE != null) {
            throw new IllegalStateException("Failed to find SSLSessionImpl on classpath", UNAVAILABILITY_CAUSE);
        }
    }

    public static Throwable sunSslEngineUnavailabilityCause() {
        return UNAVAILABILITY_CAUSE;
    }

    public static boolean isSunSslEngineAvailable() {
        return UNAVAILABILITY_CAUSE == null;
    }

    protected abstract void accept(SecretKey var1, SSLSession var2);

    @Override
    public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt == SslHandshakeCompletionEvent.SUCCESS && this.masterKeyHandlerEnabled()) {
            SslHandler handler = ctx.pipeline().get(SslHandler.class);
            SSLEngine engine = handler.engine();
            SSLSession sslSession = engine.getSession();
            if (SslMasterKeyHandler.isSunSslEngineAvailable() && sslSession.getClass().equals(SSL_SESSIONIMPL_CLASS)) {
                SecretKey secretKey;
                try {
                    secretKey = (SecretKey)SSL_SESSIONIMPL_MASTER_SECRET_FIELD.get(sslSession);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Failed to access the field 'masterSecret' via reflection.", e);
                }
                this.accept(secretKey, sslSession);
            } else if (OpenSsl.isAvailable() && engine instanceof ReferenceCountedOpenSslEngine) {
                SecretKeySpec secretKey = ((ReferenceCountedOpenSslEngine)engine).masterKey();
                this.accept(secretKey, sslSession);
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    protected boolean masterKeyHandlerEnabled() {
        return SystemPropertyUtil.getBoolean(SYSTEM_PROP_KEY, false);
    }

    public static SslMasterKeyHandler newWireSharkSslMasterKeyHandler() {
        return new WiresharkSslMasterKeyHandler();
    }

    static {
        Throwable cause;
        logger = InternalLoggerFactory.getInstance(SslMasterKeyHandler.class);
        Class<?> clazz = null;
        Field field = null;
        try {
            clazz = Class.forName("sun.security.ssl.SSLSessionImpl");
            field = clazz.getDeclaredField("masterSecret");
            cause = ReflectionUtil.trySetAccessible(field, true);
        } catch (Throwable e) {
            cause = e;
            if (logger.isTraceEnabled()) {
                logger.debug("sun.security.ssl.SSLSessionImpl is unavailable.", e);
            }
            logger.debug("sun.security.ssl.SSLSessionImpl is unavailable: {}", (Object)e.getMessage());
        }
        UNAVAILABILITY_CAUSE = cause;
        SSL_SESSIONIMPL_CLASS = clazz;
        SSL_SESSIONIMPL_MASTER_SECRET_FIELD = field;
    }

    private static final class WiresharkSslMasterKeyHandler
    extends SslMasterKeyHandler {
        private static final InternalLogger wireshark_logger = InternalLoggerFactory.getInstance("io.netty.wireshark");

        private WiresharkSslMasterKeyHandler() {
        }

        @Override
        protected void accept(SecretKey masterKey, SSLSession session) {
            if (masterKey.getEncoded().length != 48) {
                throw new IllegalArgumentException("An invalid length master key was provided.");
            }
            byte[] sessionId = session.getId();
            wireshark_logger.warn("RSA Session-ID:{} Master-Key:{}", (Object)ByteBufUtil.hexDump((byte[])sessionId).toLowerCase(), (Object)ByteBufUtil.hexDump((byte[])masterKey.getEncoded()).toLowerCase());
        }
    }
}

