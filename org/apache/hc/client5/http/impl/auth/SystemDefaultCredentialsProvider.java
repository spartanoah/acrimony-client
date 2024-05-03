/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.impl.auth;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.NTCredentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.SAFE)
public class SystemDefaultCredentialsProvider
implements CredentialsStore {
    private final BasicCredentialsProvider internal = new BasicCredentialsProvider();

    @Override
    public void setCredentials(AuthScope authScope, Credentials credentials) {
        this.internal.setCredentials(authScope, credentials);
    }

    private static PasswordAuthentication getSystemCreds(String protocol, AuthScope authScope, Authenticator.RequestorType requestorType, HttpClientContext context) {
        URL targetHostURL;
        HttpRequest request = context != null ? context.getRequest() : null;
        try {
            URI uri = request != null ? request.getUri() : null;
            targetHostURL = uri != null ? uri.toURL() : null;
        } catch (MalformedURLException | URISyntaxException ignore) {
            targetHostURL = null;
        }
        return Authenticator.requestPasswordAuthentication(authScope.getHost(), null, authScope.getPort(), protocol, authScope.getRealm(), authScope.getSchemeName(), targetHostURL, requestorType);
    }

    @Override
    public Credentials getCredentials(AuthScope authScope, HttpContext context) {
        Args.notNull(authScope, "Auth scope");
        Credentials localcreds = this.internal.getCredentials(authScope, context);
        if (localcreds != null) {
            return localcreds;
        }
        String host = authScope.getHost();
        if (host != null) {
            HttpClientContext clientContext;
            HttpClientContext httpClientContext = clientContext = context != null ? HttpClientContext.adapt(context) : null;
            String protocol = authScope.getProtocol() != null ? authScope.getProtocol() : (authScope.getPort() == 443 ? URIScheme.HTTPS.id : URIScheme.HTTP.id);
            PasswordAuthentication systemcreds = SystemDefaultCredentialsProvider.getSystemCreds(protocol, authScope, Authenticator.RequestorType.SERVER, clientContext);
            if (systemcreds == null) {
                systemcreds = SystemDefaultCredentialsProvider.getSystemCreds(protocol, authScope, Authenticator.RequestorType.PROXY, clientContext);
            }
            if (systemcreds == null && (systemcreds = SystemDefaultCredentialsProvider.getProxyCredentials("http", authScope)) == null) {
                systemcreds = SystemDefaultCredentialsProvider.getProxyCredentials("https", authScope);
            }
            if (systemcreds != null) {
                String domain = System.getProperty("http.auth.ntlm.domain");
                if (domain != null) {
                    return new NTCredentials(systemcreds.getUserName(), systemcreds.getPassword(), null, domain);
                }
                if ("NTLM".equalsIgnoreCase(authScope.getSchemeName())) {
                    return new NTCredentials(systemcreds.getUserName(), systemcreds.getPassword(), null, null);
                }
                return new UsernamePasswordCredentials(systemcreds.getUserName(), systemcreds.getPassword());
            }
        }
        return null;
    }

    private static PasswordAuthentication getProxyCredentials(String protocol, AuthScope authScope) {
        String proxyHost = System.getProperty(protocol + ".proxyHost");
        if (proxyHost == null) {
            return null;
        }
        String proxyPort = System.getProperty(protocol + ".proxyPort");
        if (proxyPort == null) {
            return null;
        }
        try {
            AuthScope systemScope = new AuthScope(proxyHost, Integer.parseInt(proxyPort));
            if (authScope.match(systemScope) >= 0) {
                String proxyUser = System.getProperty(protocol + ".proxyUser");
                if (proxyUser == null) {
                    return null;
                }
                String proxyPassword = System.getProperty(protocol + ".proxyPassword");
                return new PasswordAuthentication(proxyUser, proxyPassword != null ? proxyPassword.toCharArray() : new char[]{});
            }
        } catch (NumberFormatException ex) {
            // empty catch block
        }
        return null;
    }

    @Override
    public void clear() {
        this.internal.clear();
    }
}

