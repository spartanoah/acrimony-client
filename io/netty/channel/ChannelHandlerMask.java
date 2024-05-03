/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.channel;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.WeakHashMap;

final class ChannelHandlerMask {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ChannelHandlerMask.class);
    static final int MASK_EXCEPTION_CAUGHT = 1;
    static final int MASK_CHANNEL_REGISTERED = 2;
    static final int MASK_CHANNEL_UNREGISTERED = 4;
    static final int MASK_CHANNEL_ACTIVE = 8;
    static final int MASK_CHANNEL_INACTIVE = 16;
    static final int MASK_CHANNEL_READ = 32;
    static final int MASK_CHANNEL_READ_COMPLETE = 64;
    static final int MASK_USER_EVENT_TRIGGERED = 128;
    static final int MASK_CHANNEL_WRITABILITY_CHANGED = 256;
    static final int MASK_BIND = 512;
    static final int MASK_CONNECT = 1024;
    static final int MASK_DISCONNECT = 2048;
    static final int MASK_CLOSE = 4096;
    static final int MASK_DEREGISTER = 8192;
    static final int MASK_READ = 16384;
    static final int MASK_WRITE = 32768;
    static final int MASK_FLUSH = 65536;
    static final int MASK_ONLY_INBOUND = 510;
    private static final int MASK_ALL_INBOUND = 511;
    static final int MASK_ONLY_OUTBOUND = 130560;
    private static final int MASK_ALL_OUTBOUND = 130561;
    private static final FastThreadLocal<Map<Class<? extends ChannelHandler>, Integer>> MASKS = new FastThreadLocal<Map<Class<? extends ChannelHandler>, Integer>>(){

        @Override
        protected Map<Class<? extends ChannelHandler>, Integer> initialValue() {
            return new WeakHashMap<Class<? extends ChannelHandler>, Integer>(32);
        }
    };

    static int mask(Class<? extends ChannelHandler> clazz) {
        Map<Class<? extends ChannelHandler>, Integer> cache = MASKS.get();
        Integer mask = cache.get(clazz);
        if (mask == null) {
            mask = ChannelHandlerMask.mask0(clazz);
            cache.put(clazz, mask);
        }
        return mask;
    }

    private static int mask0(Class<? extends ChannelHandler> handlerType) {
        int mask = 1;
        try {
            if (ChannelInboundHandler.class.isAssignableFrom(handlerType)) {
                mask |= 0x1FF;
                if (ChannelHandlerMask.isSkippable(handlerType, "channelRegistered", ChannelHandlerContext.class)) {
                    mask &= 0xFFFFFFFD;
                }
                if (ChannelHandlerMask.isSkippable(handlerType, "channelUnregistered", ChannelHandlerContext.class)) {
                    mask &= 0xFFFFFFFB;
                }
                if (ChannelHandlerMask.isSkippable(handlerType, "channelActive", ChannelHandlerContext.class)) {
                    mask &= 0xFFFFFFF7;
                }
                if (ChannelHandlerMask.isSkippable(handlerType, "channelInactive", ChannelHandlerContext.class)) {
                    mask &= 0xFFFFFFEF;
                }
                if (ChannelHandlerMask.isSkippable(handlerType, "channelRead", ChannelHandlerContext.class, Object.class)) {
                    mask &= 0xFFFFFFDF;
                }
                if (ChannelHandlerMask.isSkippable(handlerType, "channelReadComplete", ChannelHandlerContext.class)) {
                    mask &= 0xFFFFFFBF;
                }
                if (ChannelHandlerMask.isSkippable(handlerType, "channelWritabilityChanged", ChannelHandlerContext.class)) {
                    mask &= 0xFFFFFEFF;
                }
                if (ChannelHandlerMask.isSkippable(handlerType, "userEventTriggered", ChannelHandlerContext.class, Object.class)) {
                    mask &= 0xFFFFFF7F;
                }
            }
            if (ChannelOutboundHandler.class.isAssignableFrom(handlerType)) {
                mask |= 0x1FE01;
                if (ChannelHandlerMask.isSkippable(handlerType, "bind", ChannelHandlerContext.class, SocketAddress.class, ChannelPromise.class)) {
                    mask &= 0xFFFFFDFF;
                }
                if (ChannelHandlerMask.isSkippable(handlerType, "connect", ChannelHandlerContext.class, SocketAddress.class, SocketAddress.class, ChannelPromise.class)) {
                    mask &= 0xFFFFFBFF;
                }
                if (ChannelHandlerMask.isSkippable(handlerType, "disconnect", ChannelHandlerContext.class, ChannelPromise.class)) {
                    mask &= 0xFFFFF7FF;
                }
                if (ChannelHandlerMask.isSkippable(handlerType, "close", ChannelHandlerContext.class, ChannelPromise.class)) {
                    mask &= 0xFFFFEFFF;
                }
                if (ChannelHandlerMask.isSkippable(handlerType, "deregister", ChannelHandlerContext.class, ChannelPromise.class)) {
                    mask &= 0xFFFFDFFF;
                }
                if (ChannelHandlerMask.isSkippable(handlerType, "read", ChannelHandlerContext.class)) {
                    mask &= 0xFFFFBFFF;
                }
                if (ChannelHandlerMask.isSkippable(handlerType, "write", ChannelHandlerContext.class, Object.class, ChannelPromise.class)) {
                    mask &= 0xFFFF7FFF;
                }
                if (ChannelHandlerMask.isSkippable(handlerType, "flush", ChannelHandlerContext.class)) {
                    mask &= 0xFFFEFFFF;
                }
            }
            if (ChannelHandlerMask.isSkippable(handlerType, "exceptionCaught", ChannelHandlerContext.class, Throwable.class)) {
                mask &= 0xFFFFFFFE;
            }
        } catch (Exception e) {
            PlatformDependent.throwException(e);
        }
        return mask;
    }

    private static boolean isSkippable(final Class<?> handlerType, final String methodName, final Class<?> ... paramTypes) throws Exception {
        return AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>(){

            @Override
            public Boolean run() throws Exception {
                Method m;
                try {
                    m = handlerType.getMethod(methodName, paramTypes);
                } catch (NoSuchMethodException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Class {} missing method {}, assume we can not skip execution", handlerType, methodName, e);
                    }
                    return false;
                }
                return m.isAnnotationPresent(Skip.class);
            }
        });
    }

    private ChannelHandlerMask() {
    }

    @Target(value={ElementType.METHOD})
    @Retention(value=RetentionPolicy.RUNTIME)
    static @interface Skip {
    }
}

