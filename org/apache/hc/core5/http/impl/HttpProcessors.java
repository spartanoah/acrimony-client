/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.impl;

import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.http.protocol.HttpProcessorBuilder;
import org.apache.hc.core5.http.protocol.RequestConnControl;
import org.apache.hc.core5.http.protocol.RequestContent;
import org.apache.hc.core5.http.protocol.RequestExpectContinue;
import org.apache.hc.core5.http.protocol.RequestTargetHost;
import org.apache.hc.core5.http.protocol.RequestUserAgent;
import org.apache.hc.core5.http.protocol.RequestValidateHost;
import org.apache.hc.core5.http.protocol.ResponseConnControl;
import org.apache.hc.core5.http.protocol.ResponseContent;
import org.apache.hc.core5.http.protocol.ResponseDate;
import org.apache.hc.core5.http.protocol.ResponseServer;
import org.apache.hc.core5.util.TextUtils;
import org.apache.hc.core5.util.VersionInfo;

public final class HttpProcessors {
    private static final String SOFTWARE = "Apache-HttpCore";

    public static HttpProcessorBuilder customServer(String serverInfo) {
        return HttpProcessorBuilder.create().addAll(new ResponseDate(), new ResponseServer(!TextUtils.isBlank(serverInfo) ? serverInfo : VersionInfo.getSoftwareInfo(SOFTWARE, "org.apache.hc.core5", HttpProcessors.class)), new ResponseContent(), new ResponseConnControl()).addAll(new RequestValidateHost());
    }

    public static HttpProcessor server(String serverInfo) {
        return HttpProcessors.customServer(serverInfo).build();
    }

    public static HttpProcessor server() {
        return HttpProcessors.customServer(null).build();
    }

    public static HttpProcessorBuilder customClient(String agentInfo) {
        return HttpProcessorBuilder.create().addAll(new RequestContent(), new RequestTargetHost(), new RequestConnControl(), new RequestUserAgent(!TextUtils.isBlank(agentInfo) ? agentInfo : VersionInfo.getSoftwareInfo(SOFTWARE, "org.apache.hc.core5", HttpProcessors.class)), new RequestExpectContinue());
    }

    public static HttpProcessor client(String agentInfo) {
        return HttpProcessors.customClient(agentInfo).build();
    }

    public static HttpProcessor client() {
        return HttpProcessors.customClient(null).build();
    }
}

