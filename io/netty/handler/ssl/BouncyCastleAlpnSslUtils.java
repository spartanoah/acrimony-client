/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.SslUtils;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.SuppressJava6Requirement;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.function.BiFunction;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

@SuppressJava6Requirement(reason="Usage guarded by java version check")
final class BouncyCastleAlpnSslUtils {
    private static final InternalLogger logger;
    private static final Class BC_SSL_PARAMETERS;
    private static final Method SET_PARAMETERS;
    private static final Method SET_APPLICATION_PROTOCOLS;
    private static final Method GET_APPLICATION_PROTOCOL;
    private static final Method GET_HANDSHAKE_APPLICATION_PROTOCOL;
    private static final Method SET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR;
    private static final Method GET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR;
    private static final Class BC_APPLICATION_PROTOCOL_SELECTOR;
    private static final Method BC_APPLICATION_PROTOCOL_SELECTOR_SELECT;

    private BouncyCastleAlpnSslUtils() {
    }

    static String getApplicationProtocol(SSLEngine sslEngine) {
        try {
            return (String)GET_APPLICATION_PROTOCOL.invoke(sslEngine, new Object[0]);
        } catch (UnsupportedOperationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    static void setApplicationProtocols(SSLEngine engine, List<String> supportedProtocols) {
        SSLParameters parameters = engine.getSSLParameters();
        String[] protocolArray = supportedProtocols.toArray(EmptyArrays.EMPTY_STRINGS);
        try {
            Object bcSslParameters = BC_SSL_PARAMETERS.newInstance();
            SET_APPLICATION_PROTOCOLS.invoke(bcSslParameters, new Object[]{protocolArray});
            SET_PARAMETERS.invoke(engine, bcSslParameters);
        } catch (UnsupportedOperationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        engine.setSSLParameters(parameters);
    }

    static String getHandshakeApplicationProtocol(SSLEngine sslEngine) {
        try {
            return (String)GET_HANDSHAKE_APPLICATION_PROTOCOL.invoke(sslEngine, new Object[0]);
        } catch (UnsupportedOperationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    static void setHandshakeApplicationProtocolSelector(SSLEngine engine, final BiFunction<SSLEngine, List<String>, String> selector) {
        try {
            Object selectorProxyInstance = Proxy.newProxyInstance(BouncyCastleAlpnSslUtils.class.getClassLoader(), new Class[]{BC_APPLICATION_PROTOCOL_SELECTOR}, new InvocationHandler(){

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.getName().equals("select")) {
                        try {
                            return selector.apply((SSLEngine)args[0], (List)args[1]);
                        } catch (ClassCastException e) {
                            throw new RuntimeException("BCApplicationProtocolSelector select method parameter of invalid type.", e);
                        }
                    }
                    throw new UnsupportedOperationException(String.format("Method '%s' not supported.", method.getName()));
                }
            });
            SET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR.invoke(engine, selectorProxyInstance);
        } catch (UnsupportedOperationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    static BiFunction<SSLEngine, List<String>, String> getHandshakeApplicationProtocolSelector(SSLEngine engine) {
        try {
            final Object selector = GET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR.invoke(engine, new Object[0]);
            return new BiFunction<SSLEngine, List<String>, String>(){

                @Override
                public String apply(SSLEngine sslEngine, List<String> strings) {
                    try {
                        return (String)BC_APPLICATION_PROTOCOL_SELECTOR_SELECT.invoke(selector, sslEngine, strings);
                    } catch (Exception e) {
                        throw new RuntimeException("Could not call getHandshakeApplicationProtocolSelector", e);
                    }
                }
            };
        } catch (UnsupportedOperationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    static {
        Method getHandshakeApplicationProtocolSelector;
        Method setHandshakeApplicationProtocolSelector;
        Method getHandshakeApplicationProtocol;
        Method getApplicationProtocol;
        Method setApplicationProtocols;
        Method setParameters;
        Method bcApplicationProtocolSelectorSelect;
        Class<?> bcApplicationProtocolSelector;
        Class<?> bcSslParameters;
        logger = InternalLoggerFactory.getInstance(BouncyCastleAlpnSslUtils.class);
        try {
            Class<?> bcSslEngine;
            final Class<?> testBCSslEngine = bcSslEngine = Class.forName("org.bouncycastle.jsse.BCSSLEngine");
            bcSslParameters = Class.forName("org.bouncycastle.jsse.BCSSLParameters");
            Object bcSslParametersInstance = bcSslParameters.newInstance();
            final Class<?> testBCSslParameters = bcSslParameters;
            final Class<?> testBCApplicationProtocolSelector = bcApplicationProtocolSelector = Class.forName("org.bouncycastle.jsse.BCApplicationProtocolSelector");
            bcApplicationProtocolSelectorSelect = AccessController.doPrivileged(new PrivilegedExceptionAction<Method>(){

                @Override
                public Method run() throws Exception {
                    return testBCApplicationProtocolSelector.getMethod("select", Object.class, List.class);
                }
            });
            SSLContext context = SslUtils.getSSLContext("BCJSSE");
            SSLEngine engine = context.createSSLEngine();
            setParameters = AccessController.doPrivileged(new PrivilegedExceptionAction<Method>(){

                @Override
                public Method run() throws Exception {
                    return testBCSslEngine.getMethod("setParameters", testBCSslParameters);
                }
            });
            setParameters.invoke(engine, bcSslParametersInstance);
            setApplicationProtocols = AccessController.doPrivileged(new PrivilegedExceptionAction<Method>(){

                @Override
                public Method run() throws Exception {
                    return testBCSslParameters.getMethod("setApplicationProtocols", String[].class);
                }
            });
            setApplicationProtocols.invoke(bcSslParametersInstance, new Object[]{EmptyArrays.EMPTY_STRINGS});
            getApplicationProtocol = AccessController.doPrivileged(new PrivilegedExceptionAction<Method>(){

                @Override
                public Method run() throws Exception {
                    return testBCSslEngine.getMethod("getApplicationProtocol", new Class[0]);
                }
            });
            getApplicationProtocol.invoke(engine, new Object[0]);
            getHandshakeApplicationProtocol = AccessController.doPrivileged(new PrivilegedExceptionAction<Method>(){

                @Override
                public Method run() throws Exception {
                    return testBCSslEngine.getMethod("getHandshakeApplicationProtocol", new Class[0]);
                }
            });
            getHandshakeApplicationProtocol.invoke(engine, new Object[0]);
            setHandshakeApplicationProtocolSelector = AccessController.doPrivileged(new PrivilegedExceptionAction<Method>(){

                @Override
                public Method run() throws Exception {
                    return testBCSslEngine.getMethod("setBCHandshakeApplicationProtocolSelector", testBCApplicationProtocolSelector);
                }
            });
            getHandshakeApplicationProtocolSelector = AccessController.doPrivileged(new PrivilegedExceptionAction<Method>(){

                @Override
                public Method run() throws Exception {
                    return testBCSslEngine.getMethod("getBCHandshakeApplicationProtocolSelector", new Class[0]);
                }
            });
            getHandshakeApplicationProtocolSelector.invoke(engine, new Object[0]);
        } catch (Throwable t) {
            logger.error("Unable to initialize BouncyCastleAlpnSslUtils.", t);
            bcSslParameters = null;
            setParameters = null;
            setApplicationProtocols = null;
            getApplicationProtocol = null;
            getHandshakeApplicationProtocol = null;
            setHandshakeApplicationProtocolSelector = null;
            getHandshakeApplicationProtocolSelector = null;
            bcApplicationProtocolSelectorSelect = null;
            bcApplicationProtocolSelector = null;
        }
        BC_SSL_PARAMETERS = bcSslParameters;
        SET_PARAMETERS = setParameters;
        SET_APPLICATION_PROTOCOLS = setApplicationProtocols;
        GET_APPLICATION_PROTOCOL = getApplicationProtocol;
        GET_HANDSHAKE_APPLICATION_PROTOCOL = getHandshakeApplicationProtocol;
        SET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR = setHandshakeApplicationProtocolSelector;
        GET_HANDSHAKE_APPLICATION_PROTOCOL_SELECTOR = getHandshakeApplicationProtocolSelector;
        BC_APPLICATION_PROTOCOL_SELECTOR_SELECT = bcApplicationProtocolSelectorSelect;
        BC_APPLICATION_PROTOCOL_SELECTOR = bcApplicationProtocolSelector;
    }
}

