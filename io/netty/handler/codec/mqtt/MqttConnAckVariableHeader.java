/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.util.internal.StringUtil;

public final class MqttConnAckVariableHeader {
    private final MqttConnectReturnCode connectReturnCode;
    private final boolean sessionPresent;
    private final MqttProperties properties;

    public MqttConnAckVariableHeader(MqttConnectReturnCode connectReturnCode, boolean sessionPresent) {
        this(connectReturnCode, sessionPresent, MqttProperties.NO_PROPERTIES);
    }

    public MqttConnAckVariableHeader(MqttConnectReturnCode connectReturnCode, boolean sessionPresent, MqttProperties properties) {
        this.connectReturnCode = connectReturnCode;
        this.sessionPresent = sessionPresent;
        this.properties = MqttProperties.withEmptyDefaults(properties);
    }

    public MqttConnectReturnCode connectReturnCode() {
        return this.connectReturnCode;
    }

    public boolean isSessionPresent() {
        return this.sessionPresent;
    }

    public MqttProperties properties() {
        return this.properties;
    }

    public String toString() {
        return StringUtil.simpleClassName(this) + '[' + "connectReturnCode=" + (Object)((Object)this.connectReturnCode) + ", sessionPresent=" + this.sessionPresent + ']';
    }
}

