/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.util.internal.StringUtil;

public final class MqttReasonCodeAndPropertiesVariableHeader {
    private final byte reasonCode;
    private final MqttProperties properties;
    public static final byte REASON_CODE_OK = 0;

    public MqttReasonCodeAndPropertiesVariableHeader(byte reasonCode, MqttProperties properties) {
        this.reasonCode = reasonCode;
        this.properties = MqttProperties.withEmptyDefaults(properties);
    }

    public byte reasonCode() {
        return this.reasonCode;
    }

    public MqttProperties properties() {
        return this.properties;
    }

    public String toString() {
        return StringUtil.simpleClassName(this) + '[' + "reasonCode=" + this.reasonCode + ", properties=" + this.properties + ']';
    }
}

