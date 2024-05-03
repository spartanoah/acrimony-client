/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdAndPropertiesVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;

public final class MqttSubAckMessage
extends MqttMessage {
    public MqttSubAckMessage(MqttFixedHeader mqttFixedHeader, MqttMessageIdAndPropertiesVariableHeader variableHeader, MqttSubAckPayload payload) {
        super(mqttFixedHeader, variableHeader, payload);
    }

    public MqttSubAckMessage(MqttFixedHeader mqttFixedHeader, MqttMessageIdVariableHeader variableHeader, MqttSubAckPayload payload) {
        this(mqttFixedHeader, variableHeader.withDefaultEmptyProperties(), payload);
    }

    @Override
    public MqttMessageIdVariableHeader variableHeader() {
        return (MqttMessageIdVariableHeader)super.variableHeader();
    }

    public MqttMessageIdAndPropertiesVariableHeader idAndPropertiesVariableHeader() {
        return (MqttMessageIdAndPropertiesVariableHeader)super.variableHeader();
    }

    @Override
    public MqttSubAckPayload payload() {
        return (MqttSubAckPayload)super.payload();
    }
}

