/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.ssl;

import io.netty.handler.ssl.JdkAlpnSslUtils;
import io.netty.handler.ssl.JdkApplicationProtocolNegotiator;
import io.netty.handler.ssl.JdkSslEngine;
import io.netty.handler.ssl.SslUtils;
import io.netty.util.internal.SuppressJava6Requirement;
import java.nio.ByteBuffer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

@SuppressJava6Requirement(reason="Usage guarded by java version check")
class JdkAlpnSslEngine
extends JdkSslEngine {
    private final JdkApplicationProtocolNegotiator.ProtocolSelectionListener selectionListener;
    private final AlpnSelector alpnSelector;

    JdkAlpnSslEngine(SSLEngine engine, JdkApplicationProtocolNegotiator applicationNegotiator, boolean isServer, BiConsumer<SSLEngine, AlpnSelector> setHandshakeApplicationProtocolSelector, BiConsumer<SSLEngine, List<String>> setApplicationProtocols) {
        super(engine);
        if (isServer) {
            this.selectionListener = null;
            this.alpnSelector = new AlpnSelector(applicationNegotiator.protocolSelectorFactory().newSelector(this, new LinkedHashSet<String>(applicationNegotiator.protocols())));
            setHandshakeApplicationProtocolSelector.accept(engine, this.alpnSelector);
        } else {
            this.selectionListener = applicationNegotiator.protocolListenerFactory().newListener(this, applicationNegotiator.protocols());
            this.alpnSelector = null;
            setApplicationProtocols.accept(engine, applicationNegotiator.protocols());
        }
    }

    JdkAlpnSslEngine(SSLEngine engine, JdkApplicationProtocolNegotiator applicationNegotiator, boolean isServer) {
        this(engine, applicationNegotiator, isServer, new BiConsumer<SSLEngine, AlpnSelector>(){

            @Override
            public void accept(SSLEngine e, AlpnSelector s) {
                JdkAlpnSslUtils.setHandshakeApplicationProtocolSelector(e, s);
            }
        }, new BiConsumer<SSLEngine, List<String>>(){

            @Override
            public void accept(SSLEngine e, List<String> p) {
                JdkAlpnSslUtils.setApplicationProtocols(e, p);
            }
        });
    }

    private SSLEngineResult verifyProtocolSelection(SSLEngineResult result) throws SSLException {
        if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
            if (this.alpnSelector == null) {
                try {
                    String protocol = this.getApplicationProtocol();
                    assert (protocol != null);
                    if (protocol.isEmpty()) {
                        this.selectionListener.unsupported();
                    }
                    this.selectionListener.selected(protocol);
                } catch (Throwable e) {
                    throw SslUtils.toSSLHandshakeException(e);
                }
            } else {
                assert (this.selectionListener == null);
                this.alpnSelector.checkUnsupported();
            }
        }
        return result;
    }

    @Override
    public SSLEngineResult wrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        return this.verifyProtocolSelection(super.wrap(src, dst));
    }

    @Override
    public SSLEngineResult wrap(ByteBuffer[] srcs, ByteBuffer dst) throws SSLException {
        return this.verifyProtocolSelection(super.wrap(srcs, dst));
    }

    @Override
    public SSLEngineResult wrap(ByteBuffer[] srcs, int offset, int len, ByteBuffer dst) throws SSLException {
        return this.verifyProtocolSelection(super.wrap(srcs, offset, len, dst));
    }

    @Override
    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer dst) throws SSLException {
        return this.verifyProtocolSelection(super.unwrap(src, dst));
    }

    @Override
    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts) throws SSLException {
        return this.verifyProtocolSelection(super.unwrap(src, dsts));
    }

    @Override
    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dst, int offset, int len) throws SSLException {
        return this.verifyProtocolSelection(super.unwrap(src, dst, offset, len));
    }

    @Override
    void setNegotiatedApplicationProtocol(String applicationProtocol) {
    }

    @Override
    public String getNegotiatedApplicationProtocol() {
        String protocol = this.getApplicationProtocol();
        if (protocol != null) {
            return protocol.isEmpty() ? null : protocol;
        }
        return null;
    }

    @Override
    public String getApplicationProtocol() {
        return JdkAlpnSslUtils.getApplicationProtocol(this.getWrappedEngine());
    }

    @Override
    public String getHandshakeApplicationProtocol() {
        return JdkAlpnSslUtils.getHandshakeApplicationProtocol(this.getWrappedEngine());
    }

    @Override
    public void setHandshakeApplicationProtocolSelector(BiFunction<SSLEngine, List<String>, String> selector) {
        JdkAlpnSslUtils.setHandshakeApplicationProtocolSelector(this.getWrappedEngine(), selector);
    }

    @Override
    public BiFunction<SSLEngine, List<String>, String> getHandshakeApplicationProtocolSelector() {
        return JdkAlpnSslUtils.getHandshakeApplicationProtocolSelector(this.getWrappedEngine());
    }

    final class AlpnSelector
    implements BiFunction<SSLEngine, List<String>, String> {
        private final JdkApplicationProtocolNegotiator.ProtocolSelector selector;
        private boolean called;

        AlpnSelector(JdkApplicationProtocolNegotiator.ProtocolSelector selector) {
            this.selector = selector;
        }

        @Override
        public String apply(SSLEngine sslEngine, List<String> strings) {
            assert (!this.called);
            this.called = true;
            try {
                String selected = this.selector.select(strings);
                return selected == null ? "" : selected;
            } catch (Exception cause) {
                return null;
            }
        }

        void checkUnsupported() {
            if (this.called) {
                return;
            }
            String protocol = JdkAlpnSslEngine.this.getApplicationProtocol();
            assert (protocol != null);
            if (protocol.isEmpty()) {
                this.selector.unsupported();
            }
        }
    }
}

