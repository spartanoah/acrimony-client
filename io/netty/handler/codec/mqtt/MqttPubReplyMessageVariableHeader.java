/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.util.internal.StringUtil;

public final class MqttPubReplyMessageVariableHeader
extends MqttMessageIdVariableHeader {
    private final byte reasonCode;
    private final MqttProperties properties;
    public static final byte REASON_CODE_OK = 0;

    public MqttPubReplyMessageVariableHeader(int messageId, byte reasonCode, MqttProperties properties) {
        super(messageId);
        if (messageId < 1 || messageId > 65535) {
            throw new IllegalArgumentException("messageId: " + messageId + " (expected: 1 ~ 65535)");
        }
        this.reasonCode = reasonCode;
        this.properties = MqttProperties.withEmptyDefaults(properties);
    }

    public byte reasonCode() {
        return this.reasonCode;
    }

    public MqttProperties properties() {
        return this.properties;
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + "[messageId=" + this.messageId() + ", reasonCode=" + this.reasonCode + ", properties=" + this.properties + ']';
    }
}

