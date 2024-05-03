/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.HttpsURLConnection;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.net.ssl.LaxHostnameVerifier;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;
import org.apache.logging.log4j.core.net.ssl.SslConfigurationFactory;
import org.apache.logging.log4j.core.util.AuthorizationProvider;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.Strings;

public class UrlConnectionFactory {
    private static final int DEFAULT_TIMEOUT = 60000;
    private static final int connectTimeoutMillis = 60000;
    private static final int readTimeoutMillis = 60000;
    private static final String JSON = "application/json";
    private static final String XML = "application/xml";
    private static final String PROPERTIES = "text/x-java-properties";
    private static final String TEXT = "text/plain";
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final String JAR = "jar";
    private static final String DEFAULT_ALLOWED_PROTOCOLS = "https, file, jar";
    private static final String NO_PROTOCOLS = "_none";
    public static final String ALLOWED_PROTOCOLS = "log4j2.Configuration.allowedProtocols";

    public static <T extends URLConnection> T createConnection(URL url, long lastModifiedMillis, SslConfiguration sslConfiguration, AuthorizationProvider authorizationProvider) throws IOException {
        URLConnection urlConnection;
        PropertiesUtil props = PropertiesUtil.getProperties();
        List<String> allowed = Arrays.asList(Strings.splitList(props.getStringProperty(ALLOWED_PROTOCOLS, DEFAULT_ALLOWED_PROTOCOLS).toLowerCase(Locale.ROOT)));
        if (allowed.size() == 1 && NO_PROTOCOLS.equals(allowed.get(0))) {
            throw new ProtocolException("No external protocols have been enabled");
        }
        String protocol = url.getProtocol();
        if (protocol == null) {
            throw new ProtocolException("No protocol was specified on " + url.toString());
        }
        if (!allowed.contains(protocol)) {
            throw new ProtocolException("Protocol " + protocol + " has not been enabled as an allowed protocol");
        }
        if (url.getProtocol().equals(HTTP) || url.getProtocol().equals(HTTPS)) {
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            if (authorizationProvider != null) {
                authorizationProvider.addAuthorization(httpURLConnection);
            }
            httpURLConnection.setAllowUserInteraction(false);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(60000);
            httpURLConnection.setReadTimeout(60000);
            String[] fileParts = url.getFile().split("\\.");
            String type = fileParts[fileParts.length - 1].trim();
            String contentType = UrlConnectionFactory.isXml(type) ? XML : (UrlConnectionFactory.isJson(type) ? JSON : (UrlConnectionFactory.isProperties(type) ? PROPERTIES : TEXT));
            httpURLConnection.setRequestProperty("Content-Type", contentType);
            if (lastModifiedMillis > 0L) {
                httpURLConnection.setIfModifiedSince(lastModifiedMillis);
            }
            if (url.getProtocol().equals(HTTPS) && sslConfiguration != null) {
                ((HttpsURLConnection)httpURLConnection).setSSLSocketFactory(sslConfiguration.getSslSocketFactory());
                if (!sslConfiguration.isVerifyHostName()) {
                    ((HttpsURLConnection)httpURLConnection).setHostnameVerifier(LaxHostnameVerifier.INSTANCE);
                }
            }
            urlConnection = httpURLConnection;
        } else if (url.getProtocol().equals(JAR)) {
            urlConnection = url.openConnection();
            urlConnection.setUseCaches(false);
        } else {
            urlConnection = url.openConnection();
        }
        return (T)urlConnection;
    }

    public static URLConnection createConnection(URL url) throws IOException {
        URLConnection urlConnection = null;
        if (url.getProtocol().equals(HTTPS) || url.getProtocol().equals(HTTP)) {
            AuthorizationProvider provider = ConfigurationFactory.authorizationProvider(PropertiesUtil.getProperties());
            urlConnection = UrlConnectionFactory.createConnection(url, 0L, SslConfigurationFactory.getSslConfiguration(), provider);
        } else {
            urlConnection = url.openConnection();
            if (urlConnection instanceof JarURLConnection) {
                urlConnection.setUseCaches(false);
            }
        }
        return urlConnection;
    }

    private static boolean isXml(String type) {
        return type.equalsIgnoreCase("xml");
    }

    private static boolean isJson(String type) {
        return type.equalsIgnoreCase("json") || type.equalsIgnoreCase("jsn");
    }

    private static boolean isProperties(String type) {
        return type.equalsIgnoreCase("properties");
    }
}

