/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.base64.Base64Dialect;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.util.NetUtil;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;

final class SslUtils {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SslUtils.class);
    static final Set<String> TLSV13_CIPHERS = Collections.unmodifiableSet(new LinkedHashSet<String>(Arrays.asList("TLS_AES_256_GCM_SHA384", "TLS_CHACHA20_POLY1305_SHA256", "TLS_AES_128_GCM_SHA256", "TLS_AES_128_CCM_8_SHA256", "TLS_AES_128_CCM_SHA256")));
    static final int GMSSL_PROTOCOL_VERSION = 257;
    static final String INVALID_CIPHER = "SSL_NULL_WITH_NULL_NULL";
    static final int SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC = 20;
    static final int SSL_CONTENT_TYPE_ALERT = 21;
    static final int SSL_CONTENT_TYPE_HANDSHAKE = 22;
    static final int SSL_CONTENT_TYPE_APPLICATION_DATA = 23;
    static final int SSL_CONTENT_TYPE_EXTENSION_HEARTBEAT = 24;
    static final int SSL_RECORD_HEADER_LENGTH = 5;
    static final int NOT_ENOUGH_DATA = -1;
    static final int NOT_ENCRYPTED = -2;
    static final String[] DEFAULT_CIPHER_SUITES;
    static final String[] DEFAULT_TLSV13_CIPHER_SUITES;
    static final String[] TLSV13_CIPHER_SUITES;
    private static final boolean TLSV1_3_JDK_SUPPORTED;
    private static final boolean TLSV1_3_JDK_DEFAULT_ENABLED;

    static boolean isTLSv13SupportedByJDK(Provider provider) {
        if (provider == null) {
            return TLSV1_3_JDK_SUPPORTED;
        }
        return SslUtils.isTLSv13SupportedByJDK0(provider);
    }

    private static boolean isTLSv13SupportedByJDK0(Provider provider) {
        try {
            return SslUtils.arrayContains(SslUtils.newInitContext(provider).getSupportedSSLParameters().getProtocols(), "TLSv1.3");
        } catch (Throwable cause) {
            logger.debug("Unable to detect if JDK SSLEngine with provider {} supports TLSv1.3, assuming no", (Object)provider, (Object)cause);
            return false;
        }
    }

    static boolean isTLSv13EnabledByJDK(Provider provider) {
        if (provider == null) {
            return TLSV1_3_JDK_DEFAULT_ENABLED;
        }
        return SslUtils.isTLSv13EnabledByJDK0(provider);
    }

    private static boolean isTLSv13EnabledByJDK0(Provider provider) {
        try {
            return SslUtils.arrayContains(SslUtils.newInitContext(provider).getDefaultSSLParameters().getProtocols(), "TLSv1.3");
        } catch (Throwable cause) {
            logger.debug("Unable to detect if JDK SSLEngine with provider {} enables TLSv1.3 by default, assuming no", (Object)provider, (Object)cause);
            return false;
        }
    }

    private static SSLContext newInitContext(Provider provider) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = provider == null ? SSLContext.getInstance("TLS") : SSLContext.getInstance("TLS", provider);
        context.init(null, new TrustManager[0], null);
        return context;
    }

    static SSLContext getSSLContext(String provider) throws NoSuchAlgorithmException, KeyManagementException, NoSuchProviderException {
        SSLContext context = StringUtil.isNullOrEmpty((String)provider) ? SSLContext.getInstance(SslUtils.getTlsVersion()) : SSLContext.getInstance(SslUtils.getTlsVersion(), provider);
        context.init(null, new TrustManager[0], null);
        return context;
    }

    private static String getTlsVersion() {
        return TLSV1_3_JDK_SUPPORTED ? "TLSv1.3" : "TLSv1.2";
    }

    static boolean arrayContains(String[] array, String value) {
        for (String v : array) {
            if (!value.equals(v)) continue;
            return true;
        }
        return false;
    }

    static void addIfSupported(Set<String> supported, List<String> enabled, String ... names) {
        for (String n : names) {
            if (!supported.contains(n)) continue;
            enabled.add(n);
        }
    }

    static void useFallbackCiphersIfDefaultIsEmpty(List<String> defaultCiphers, Iterable<String> fallbackCiphers) {
        if (defaultCiphers.isEmpty()) {
            for (String cipher : fallbackCiphers) {
                if (cipher.startsWith("SSL_") || cipher.contains("_RC4_")) continue;
                defaultCiphers.add(cipher);
            }
        }
    }

    static void useFallbackCiphersIfDefaultIsEmpty(List<String> defaultCiphers, String ... fallbackCiphers) {
        SslUtils.useFallbackCiphersIfDefaultIsEmpty(defaultCiphers, Arrays.asList(fallbackCiphers));
    }

    static SSLHandshakeException toSSLHandshakeException(Throwable e) {
        if (e instanceof SSLHandshakeException) {
            return (SSLHandshakeException)e;
        }
        return (SSLHandshakeException)new SSLHandshakeException(e.getMessage()).initCause(e);
    }

    static int getEncryptedPacketLength(ByteBuf buffer, int offset) {
        boolean tls;
        int packetLength = 0;
        switch (buffer.getUnsignedByte(offset)) {
            case 20: 
            case 21: 
            case 22: 
            case 23: 
            case 24: {
                tls = true;
                break;
            }
            default: {
                tls = false;
            }
        }
        if (tls) {
            short majorVersion = buffer.getUnsignedByte(offset + 1);
            if (majorVersion == 3 || buffer.getShort(offset + 1) == 257) {
                packetLength = SslUtils.unsignedShortBE(buffer, offset + 3) + 5;
                if (packetLength <= 5) {
                    tls = false;
                }
            } else {
                tls = false;
            }
        }
        if (!tls) {
            int headerLength = (buffer.getUnsignedByte(offset) & 0x80) != 0 ? 2 : 3;
            short majorVersion = buffer.getUnsignedByte(offset + headerLength + 1);
            if (majorVersion == 2 || majorVersion == 3) {
                int n = packetLength = headerLength == 2 ? (SslUtils.shortBE(buffer, offset) & Short.MAX_VALUE) + 2 : (SslUtils.shortBE(buffer, offset) & 0x3FFF) + 3;
                if (packetLength <= headerLength) {
                    return -1;
                }
            } else {
                return -2;
            }
        }
        return packetLength;
    }

    private static int unsignedShortBE(ByteBuf buffer, int offset) {
        return buffer.order() == ByteOrder.BIG_ENDIAN ? buffer.getUnsignedShort(offset) : buffer.getUnsignedShortLE(offset);
    }

    private static short shortBE(ByteBuf buffer, int offset) {
        return buffer.order() == ByteOrder.BIG_ENDIAN ? buffer.getShort(offset) : buffer.getShortLE(offset);
    }

    private static short unsignedByte(byte b) {
        return (short)(b & 0xFF);
    }

    private static int unsignedShortBE(ByteBuffer buffer, int offset) {
        return SslUtils.shortBE(buffer, offset) & 0xFFFF;
    }

    private static short shortBE(ByteBuffer buffer, int offset) {
        return buffer.order() == ByteOrder.BIG_ENDIAN ? buffer.getShort(offset) : ByteBufUtil.swapShort(buffer.getShort(offset));
    }

    static int getEncryptedPacketLength(ByteBuffer[] buffers, int offset) {
        ByteBuffer buffer = buffers[offset];
        if (buffer.remaining() >= 5) {
            return SslUtils.getEncryptedPacketLength(buffer);
        }
        ByteBuffer tmp = ByteBuffer.allocate(5);
        do {
            if ((buffer = buffers[offset++].duplicate()).remaining() > tmp.remaining()) {
                buffer.limit(buffer.position() + tmp.remaining());
            }
            tmp.put(buffer);
        } while (tmp.hasRemaining());
        tmp.flip();
        return SslUtils.getEncryptedPacketLength(tmp);
    }

    private static int getEncryptedPacketLength(ByteBuffer buffer) {
        boolean tls;
        int packetLength = 0;
        int pos = buffer.position();
        switch (SslUtils.unsignedByte(buffer.get(pos))) {
            case 20: 
            case 21: 
            case 22: 
            case 23: 
            case 24: {
                tls = true;
                break;
            }
            default: {
                tls = false;
            }
        }
        if (tls) {
            short majorVersion = SslUtils.unsignedByte(buffer.get(pos + 1));
            if (majorVersion == 3 || buffer.getShort(pos + 1) == 257) {
                packetLength = SslUtils.unsignedShortBE(buffer, pos + 3) + 5;
                if (packetLength <= 5) {
                    tls = false;
                }
            } else {
                tls = false;
            }
        }
        if (!tls) {
            int headerLength = (SslUtils.unsignedByte(buffer.get(pos)) & 0x80) != 0 ? 2 : 3;
            short majorVersion = SslUtils.unsignedByte(buffer.get(pos + headerLength + 1));
            if (majorVersion == 2 || majorVersion == 3) {
                int n = packetLength = headerLength == 2 ? (SslUtils.shortBE(buffer, pos) & Short.MAX_VALUE) + 2 : (SslUtils.shortBE(buffer, pos) & 0x3FFF) + 3;
                if (packetLength <= headerLength) {
                    return -1;
                }
            } else {
                return -2;
            }
        }
        return packetLength;
    }

    static void handleHandshakeFailure(ChannelHandlerContext ctx, Throwable cause, boolean notify) {
        ctx.flush();
        if (notify) {
            ctx.fireUserEventTriggered(new SslHandshakeCompletionEvent(cause));
        }
        ctx.close();
    }

    static void zeroout(ByteBuf buffer) {
        if (!buffer.isReadOnly()) {
            buffer.setZero(0, buffer.capacity());
        }
    }

    static void zerooutAndRelease(ByteBuf buffer) {
        SslUtils.zeroout(buffer);
        buffer.release();
    }

    static ByteBuf toBase64(ByteBufAllocator allocator, ByteBuf src) {
        ByteBuf dst = Base64.encode((ByteBuf)src, (int)src.readerIndex(), (int)src.readableBytes(), (boolean)true, (Base64Dialect)Base64Dialect.STANDARD, (ByteBufAllocator)allocator);
        src.readerIndex(src.writerIndex());
        return dst;
    }

    static boolean isValidHostNameForSNI(String hostname) {
        return hostname != null && hostname.indexOf(46) > 0 && !hostname.endsWith(".") && !NetUtil.isValidIpV4Address(hostname) && !NetUtil.isValidIpV6Address(hostname);
    }

    static boolean isTLSv13Cipher(String cipher) {
        return TLSV13_CIPHERS.contains(cipher);
    }

    static boolean isEmpty(Object[] arr) {
        return arr == null || arr.length == 0;
    }

    private SslUtils() {
    }

    static {
        TLSV13_CIPHER_SUITES = new String[]{"TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384"};
        TLSV1_3_JDK_SUPPORTED = SslUtils.isTLSv13SupportedByJDK0(null);
        TLSV1_3_JDK_DEFAULT_ENABLED = SslUtils.isTLSv13EnabledByJDK0(null);
        DEFAULT_TLSV13_CIPHER_SUITES = TLSV1_3_JDK_SUPPORTED ? TLSV13_CIPHER_SUITES : EmptyArrays.EMPTY_STRINGS;
        ArrayList<String> defaultCiphers = new ArrayList<String>();
        defaultCiphers.add("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384");
        defaultCiphers.add("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256");
        defaultCiphers.add("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        defaultCiphers.add("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");
        defaultCiphers.add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA");
        defaultCiphers.add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA");
        defaultCiphers.add("TLS_RSA_WITH_AES_128_GCM_SHA256");
        defaultCiphers.add("TLS_RSA_WITH_AES_128_CBC_SHA");
        defaultCiphers.add("TLS_RSA_WITH_AES_256_CBC_SHA");
        Collections.addAll(defaultCiphers, DEFAULT_TLSV13_CIPHER_SUITES);
        DEFAULT_CIPHER_SUITES = defaultCiphers.toArray(EmptyArrays.EMPTY_STRINGS);
    }
}

