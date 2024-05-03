/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

final class MqttCodecUtil {
    private static final char[] TOPIC_WILDCARDS = new char[]{'#', '+'};
    static final AttributeKey<MqttVersion> MQTT_VERSION_KEY = AttributeKey.valueOf("NETTY_CODEC_MQTT_VERSION");

    static MqttVersion getMqttVersion(ChannelHandlerContext ctx) {
        Attribute<MqttVersion> attr = ctx.channel().attr(MQTT_VERSION_KEY);
        MqttVersion version = attr.get();
        if (version == null) {
            return MqttVersion.MQTT_3_1_1;
        }
        return version;
    }

    static void setMqttVersion(ChannelHandlerContext ctx, MqttVersion version) {
        Attribute<MqttVersion> attr = ctx.channel().attr(MQTT_VERSION_KEY);
        attr.set(version);
    }

    static boolean isValidPublishTopicName(String topicName) {
        for (char c : TOPIC_WILDCARDS) {
            if (topicName.indexOf(c) < 0) continue;
            return false;
        }
        return true;
    }

    static boolean isValidMessageId(int messageId) {
        return messageId != 0;
    }

    static boolean isValidClientId(MqttVersion mqttVersion, int maxClientIdLength, String clientId) {
        if (mqttVersion == MqttVersion.MQTT_3_1) {
            return clientId != null && clientId.length() >= 1 && clientId.length() <= maxClientIdLength;
        }
        if (mqttVersion == MqttVersion.MQTT_3_1_1 || mqttVersion == MqttVersion.MQTT_5) {
            return clientId != null;
        }
        throw new IllegalArgumentException((Object)((Object)mqttVersion) + " is unknown mqtt version");
    }

    static MqttFixedHeader validateFixedHeader(ChannelHandlerContext ctx, MqttFixedHeader mqttFixedHeader) {
        switch (mqttFixedHeader.messageType()) {
            case PUBREL: 
            case SUBSCRIBE: 
            case UNSUBSCRIBE: {
                if (mqttFixedHeader.qosLevel() != MqttQoS.AT_LEAST_ONCE) {
                    throw new DecoderException(mqttFixedHeader.messageType().name() + " message must have QoS 1");
                }
                return mqttFixedHeader;
            }
            case AUTH: {
                if (MqttCodecUtil.getMqttVersion(ctx) != MqttVersion.MQTT_5) {
                    throw new DecoderException("AUTH message requires at least MQTT 5");
                }
                return mqttFixedHeader;
            }
        }
        return mqttFixedHeader;
    }

    static MqttFixedHeader resetUnusedFields(MqttFixedHeader mqttFixedHeader) {
        switch (mqttFixedHeader.messageType()) {
            case CONNECT: 
            case CONNACK: 
            case PUBACK: 
            case PUBREC: 
            case PUBCOMP: 
            case SUBACK: 
            case UNSUBACK: 
            case PINGREQ: 
            case PINGRESP: 
            case DISCONNECT: {
                if (mqttFixedHeader.isDup() || mqttFixedHeader.qosLevel() != MqttQoS.AT_MOST_ONCE || mqttFixedHeader.isRetain()) {
                    return new MqttFixedHeader(mqttFixedHeader.messageType(), false, MqttQoS.AT_MOST_ONCE, false, mqttFixedHeader.remainingLength());
                }
                return mqttFixedHeader;
            }
            case PUBREL: 
            case SUBSCRIBE: 
            case UNSUBSCRIBE: {
                if (mqttFixedHeader.isRetain()) {
                    return new MqttFixedHeader(mqttFixedHeader.messageType(), mqttFixedHeader.isDup(), mqttFixedHeader.qosLevel(), false, mqttFixedHeader.remainingLength());
                }
                return mqttFixedHeader;
            }
        }
        return mqttFixedHeader;
    }

    private MqttCodecUtil() {
    }
}

