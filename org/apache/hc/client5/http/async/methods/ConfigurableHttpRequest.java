/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.async.methods;

import java.net.URI;
import org.apache.hc.client5.http.config.Configurable;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHttpRequest;

public class ConfigurableHttpRequest
extends BasicHttpRequest
implements Configurable {
    private static final long serialVersionUID = 1L;
    private RequestConfig requestConfig;

    public ConfigurableHttpRequest(String method, String path) {
        super(method, path);
    }

    public ConfigurableHttpRequest(String method, HttpHost host, String path) {
        super(method, host, path);
    }

    public ConfigurableHttpRequest(String method, URI requestUri) {
        super(method, requestUri);
    }

    @Override
    public RequestConfig getConfig() {
        return this.requestConfig;
    }

    public void setConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
    }
}

