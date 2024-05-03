/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.classic;

import java.io.IOException;
import java.net.Socket;
import org.apache.hc.client5.http.AuthenticationStrategy;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.RouteInfo;
import org.apache.hc.client5.http.auth.AuthExchange;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.ChallengeType;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultAuthenticationStrategy;
import org.apache.hc.client5.http.impl.TunnelRefusedException;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicSchemeFactory;
import org.apache.hc.client5.http.impl.auth.DigestSchemeFactory;
import org.apache.hc.client5.http.impl.auth.HttpAuthenticator;
import org.apache.hc.client5.http.impl.auth.KerberosSchemeFactory;
import org.apache.hc.client5.http.impl.auth.NTLMSchemeFactory;
import org.apache.hc.client5.http.impl.auth.SPNegoSchemeFactory;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.client5.http.protocol.RequestClientConnControl;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.io.HttpConnectionFactory;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.DefaultHttpProcessor;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.http.protocol.RequestTargetHost;
import org.apache.hc.core5.http.protocol.RequestUserAgent;
import org.apache.hc.core5.util.Args;

public class ProxyClient {
    private final HttpConnectionFactory<ManagedHttpClientConnection> connFactory;
    private final RequestConfig requestConfig;
    private final HttpProcessor httpProcessor;
    private final HttpRequestExecutor requestExec;
    private final AuthenticationStrategy proxyAuthStrategy;
    private final HttpAuthenticator authenticator;
    private final AuthExchange proxyAuthExchange;
    private final Lookup<AuthSchemeFactory> authSchemeRegistry;
    private final ConnectionReuseStrategy reuseStrategy;

    public ProxyClient(HttpConnectionFactory<ManagedHttpClientConnection> connFactory, Http1Config h1Config, CharCodingConfig charCodingConfig, RequestConfig requestConfig) {
        this.connFactory = connFactory != null ? connFactory : new ManagedHttpClientConnectionFactory(h1Config, charCodingConfig, null, null);
        this.requestConfig = requestConfig != null ? requestConfig : RequestConfig.DEFAULT;
        this.httpProcessor = new DefaultHttpProcessor(new RequestTargetHost(), new RequestClientConnControl(), new RequestUserAgent());
        this.requestExec = new HttpRequestExecutor();
        this.proxyAuthStrategy = new DefaultAuthenticationStrategy();
        this.authenticator = new HttpAuthenticator();
        this.proxyAuthExchange = new AuthExchange();
        this.authSchemeRegistry = RegistryBuilder.create().register("Basic", BasicSchemeFactory.INSTANCE).register("Digest", (BasicSchemeFactory)((Object)DigestSchemeFactory.INSTANCE)).register("NTLM", (BasicSchemeFactory)((Object)NTLMSchemeFactory.INSTANCE)).register("Negotiate", (BasicSchemeFactory)((Object)SPNegoSchemeFactory.DEFAULT)).register("Kerberos", (BasicSchemeFactory)((Object)KerberosSchemeFactory.DEFAULT)).build();
        this.reuseStrategy = new DefaultConnectionReuseStrategy();
    }

    public ProxyClient(RequestConfig requestConfig) {
        this(null, null, null, requestConfig);
    }

    public ProxyClient() {
        this(null, null, null, null);
    }

    public Socket tunnel(HttpHost proxy, HttpHost target, Credentials credentials) throws IOException, HttpException {
        HttpEntity entity;
        int status;
        ClassicHttpResponse response;
        Args.notNull(proxy, "Proxy host");
        Args.notNull(target, "Target host");
        Args.notNull(credentials, "Credentials");
        HttpHost host = target;
        if (host.getPort() <= 0) {
            host = new HttpHost(host.getSchemeName(), host.getHostName(), 80);
        }
        HttpRoute route = new HttpRoute(host, null, proxy, false, RouteInfo.TunnelType.TUNNELLED, RouteInfo.LayerType.PLAIN);
        ManagedHttpClientConnection conn = this.connFactory.createConnection(null);
        BasicHttpContext context = new BasicHttpContext();
        BasicClassicHttpRequest connect = new BasicClassicHttpRequest("CONNECT", host.toHostString());
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(proxy), credentials);
        context.setAttribute("http.request", connect);
        context.setAttribute("http.route", route);
        context.setAttribute("http.auth.credentials-provider", credsProvider);
        context.setAttribute("http.authscheme-registry", this.authSchemeRegistry);
        context.setAttribute("http.request-config", this.requestConfig);
        this.requestExec.preProcess(connect, this.httpProcessor, context);
        while (true) {
            if (!conn.isOpen()) {
                Socket socket = new Socket(proxy.getHostName(), proxy.getPort());
                conn.bind(socket);
            }
            this.authenticator.addAuthResponse(proxy, ChallengeType.PROXY, connect, this.proxyAuthExchange, context);
            response = this.requestExec.execute(connect, conn, context);
            status = response.getCode();
            if (status < 200) {
                throw new HttpException("Unexpected response to CONNECT request: " + response);
            }
            if (!this.authenticator.isChallenged(proxy, ChallengeType.PROXY, response, this.proxyAuthExchange, context) || !this.authenticator.updateAuthState(proxy, ChallengeType.PROXY, response, this.proxyAuthStrategy, this.proxyAuthExchange, context)) break;
            if (this.reuseStrategy.keepAlive(connect, response, context)) {
                entity = response.getEntity();
                EntityUtils.consume(entity);
            } else {
                conn.close();
            }
            connect.removeHeaders("Proxy-Authorization");
        }
        status = response.getCode();
        if (status > 299) {
            entity = response.getEntity();
            String responseMessage = entity != null ? EntityUtils.toString(entity) : null;
            conn.close();
            throw new TunnelRefusedException("CONNECT refused by proxy: " + new StatusLine(response), responseMessage);
        }
        return conn.getSocket();
    }
}

