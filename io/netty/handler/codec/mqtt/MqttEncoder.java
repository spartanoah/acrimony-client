/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.mqtt.MqttCodecUtil;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttIdentifierRejectedException;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdAndPropertiesVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttPubReplyMessageVariableHeader;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttReasonCodeAndPropertiesVariableHeader;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttSubscribePayload;
import io.netty.handler.codec.mqtt.MqttSubscriptionOption;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubAckPayload;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribePayload;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.netty.util.internal.EmptyArrays;
import java.util.List;

@ChannelHandler.Sharable
public final class MqttEncoder
extends MessageToMessageEncoder<MqttMessage> {
    public static final MqttEncoder INSTANCE = new MqttEncoder();

    private MqttEncoder() {
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, MqttMessage msg, List<Object> out) throws Exception {
        out.add(MqttEncoder.doEncode(ctx, msg));
    }

    static ByteBuf doEncode(ChannelHandlerContext ctx, MqttMessage message) {
        switch (message.fixedHeader().messageType()) {
            case CONNECT: {
                return MqttEncoder.encodeConnectMessage(ctx, (MqttConnectMessage)message);
            }
            case CONNACK: {
                return MqttEncoder.encodeConnAckMessage(ctx, (MqttConnAckMessage)message);
            }
            case PUBLISH: {
                return MqttEncoder.encodePublishMessage(ctx, (MqttPublishMessage)message);
            }
            case SUBSCRIBE: {
                return MqttEncoder.encodeSubscribeMessage(ctx, (MqttSubscribeMessage)message);
            }
            case UNSUBSCRIBE: {
                return MqttEncoder.encodeUnsubscribeMessage(ctx, (MqttUnsubscribeMessage)message);
            }
            case SUBACK: {
                return MqttEncoder.encodeSubAckMessage(ctx, (MqttSubAckMessage)message);
            }
            case UNSUBACK: {
                if (message instanceof MqttUnsubAckMessage) {
                    return MqttEncoder.encodeUnsubAckMessage(ctx, (MqttUnsubAckMessage)message);
                }
                return MqttEncoder.encodeMessageWithOnlySingleByteFixedHeaderAndMessageId(ctx.alloc(), message);
            }
            case PUBACK: 
            case PUBREC: 
            case PUBREL: 
            case PUBCOMP: {
                return MqttEncoder.encodePubReplyMessage(ctx, message);
            }
            case DISCONNECT: 
            case AUTH: {
                return MqttEncoder.encodeReasonCodePlusPropertiesMessage(ctx, message);
            }
            case PINGREQ: 
            case PINGRESP: {
                return MqttEncoder.encodeMessageWithOnlySingleByteFixedHeader(ctx.alloc(), message);
            }
        }
        throw new IllegalArgumentException("Unknown message type: " + message.fixedHeader().messageType().value());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ByteBuf encodeConnectMessage(ChannelHandlerContext ctx, MqttConnectMessage message) {
        byte[] password;
        byte[] passwordBytes;
        byte[] willMessageBytes;
        int payloadBufferSize = 0;
        MqttFixedHeader mqttFixedHeader = message.fixedHeader();
        MqttConnectVariableHeader variableHeader = message.variableHeader();
        MqttConnectPayload payload = message.payload();
        MqttVersion mqttVersion = MqttVersion.fromProtocolNameAndLevel(variableHeader.name(), (byte)variableHeader.version());
        MqttCodecUtil.setMqttVersion(ctx, mqttVersion);
        if (!variableHeader.hasUserName() && variableHeader.hasPassword()) {
            throw new EncoderException("Without a username, the password MUST be not set");
        }
        String clientIdentifier = payload.clientIdentifier();
        if (!MqttCodecUtil.isValidClientId(mqttVersion, 23, clientIdentifier)) {
            throw new MqttIdentifierRejectedException("invalid clientIdentifier: " + clientIdentifier);
        }
        int clientIdentifierBytes = ByteBufUtil.utf8Bytes((CharSequence)clientIdentifier);
        payloadBufferSize += 2 + clientIdentifierBytes;
        String willTopic = payload.willTopic();
        int willTopicBytes = MqttEncoder.nullableUtf8Bytes(willTopic);
        byte[] willMessage = payload.willMessageInBytes();
        byte[] byArray = willMessageBytes = willMessage != null ? willMessage : EmptyArrays.EMPTY_BYTES;
        if (variableHeader.isWillFlag()) {
            payloadBufferSize += 2 + willTopicBytes;
            payloadBufferSize += 2 + willMessageBytes.length;
        }
        String userName = payload.userName();
        int userNameBytes = MqttEncoder.nullableUtf8Bytes(userName);
        if (variableHeader.hasUserName()) {
            payloadBufferSize += 2 + userNameBytes;
        }
        byte[] byArray2 = passwordBytes = (password = payload.passwordInBytes()) != null ? password : EmptyArrays.EMPTY_BYTES;
        if (variableHeader.hasPassword()) {
            payloadBufferSize += 2 + passwordBytes.length;
        }
        byte[] protocolNameBytes = mqttVersion.protocolNameBytes();
        ByteBuf propertiesBuf = MqttEncoder.encodePropertiesIfNeeded(mqttVersion, ctx.alloc(), message.variableHeader().properties());
        try {
            ByteBuf byteBuf;
            ByteBuf willPropertiesBuf;
            if (variableHeader.isWillFlag()) {
                willPropertiesBuf = MqttEncoder.encodePropertiesIfNeeded(mqttVersion, ctx.alloc(), payload.willProperties());
                payloadBufferSize += willPropertiesBuf.readableBytes();
            } else {
                willPropertiesBuf = Unpooled.EMPTY_BUFFER;
            }
            try {
                int variableHeaderBufferSize = 2 + protocolNameBytes.length + 4 + propertiesBuf.readableBytes();
                int variablePartSize = variableHeaderBufferSize + payloadBufferSize;
                int fixedHeaderBufferSize = 1 + MqttEncoder.getVariableLengthInt(variablePartSize);
                ByteBuf buf = ctx.alloc().buffer(fixedHeaderBufferSize + variablePartSize);
                buf.writeByte(MqttEncoder.getFixedHeaderByte1(mqttFixedHeader));
                MqttEncoder.writeVariableLengthInt(buf, variablePartSize);
                buf.writeShort(protocolNameBytes.length);
                buf.writeBytes(protocolNameBytes);
                buf.writeByte(variableHeader.version());
                buf.writeByte(MqttEncoder.getConnVariableHeaderFlag(variableHeader));
                buf.writeShort(variableHeader.keepAliveTimeSeconds());
                buf.writeBytes(propertiesBuf);
                MqttEncoder.writeExactUTF8String(buf, clientIdentifier, clientIdentifierBytes);
                if (variableHeader.isWillFlag()) {
                    buf.writeBytes(willPropertiesBuf);
                    MqttEncoder.writeExactUTF8String(buf, willTopic, willTopicBytes);
                    buf.writeShort(willMessageBytes.length);
                    buf.writeBytes(willMessageBytes, 0, willMessageBytes.length);
                }
                if (variableHeader.hasUserName()) {
                    MqttEncoder.writeExactUTF8String(buf, userName, userNameBytes);
                }
                if (variableHeader.hasPassword()) {
                    buf.writeShort(passwordBytes.length);
                    buf.writeBytes(passwordBytes, 0, passwordBytes.length);
                }
                byteBuf = buf;
            } catch (Throwable throwable) {
                willPropertiesBuf.release();
                throw throwable;
            }
            willPropertiesBuf.release();
            return byteBuf;
        } finally {
            propertiesBuf.release();
        }
    }

    private static int getConnVariableHeaderFlag(MqttConnectVariableHeader variableHeader) {
        int flagByte = 0;
        if (variableHeader.hasUserName()) {
            flagByte |= 0x80;
        }
        if (variableHeader.hasPassword()) {
            flagByte |= 0x40;
        }
        if (variableHeader.isWillRetain()) {
            flagByte |= 0x20;
        }
        flagByte |= (variableHeader.willQos() & 3) << 3;
        if (variableHeader.isWillFlag()) {
            flagByte |= 4;
        }
        if (variableHeader.isCleanSession()) {
            flagByte |= 2;
        }
        return flagByte;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ByteBuf encodeConnAckMessage(ChannelHandlerContext ctx, MqttConnAckMessage message) {
        MqttVersion mqttVersion = MqttCodecUtil.getMqttVersion(ctx);
        ByteBuf propertiesBuf = MqttEncoder.encodePropertiesIfNeeded(mqttVersion, ctx.alloc(), message.variableHeader().properties());
        try {
            ByteBuf buf = ctx.alloc().buffer(4 + propertiesBuf.readableBytes());
            buf.writeByte(MqttEncoder.getFixedHeaderByte1(message.fixedHeader()));
            MqttEncoder.writeVariableLengthInt(buf, 2 + propertiesBuf.readableBytes());
            buf.writeByte(message.variableHeader().isSessionPresent() ? 1 : 0);
            buf.writeByte(message.variableHeader().connectReturnCode().byteValue());
            buf.writeBytes(propertiesBuf);
            ByteBuf byteBuf = buf;
            return byteBuf;
        } finally {
            propertiesBuf.release();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ByteBuf encodeSubscribeMessage(ChannelHandlerContext ctx, MqttSubscribeMessage message) {
        MqttVersion mqttVersion = MqttCodecUtil.getMqttVersion(ctx);
        ByteBuf propertiesBuf = MqttEncoder.encodePropertiesIfNeeded(mqttVersion, ctx.alloc(), message.idAndPropertiesVariableHeader().properties());
        try {
            int variableHeaderBufferSize = 2 + propertiesBuf.readableBytes();
            int payloadBufferSize = 0;
            MqttFixedHeader mqttFixedHeader = message.fixedHeader();
            MqttMessageIdVariableHeader variableHeader = message.variableHeader();
            MqttSubscribePayload payload = message.payload();
            for (MqttTopicSubscription topic : payload.topicSubscriptions()) {
                String topicName = topic.topicName();
                int topicNameBytes = ByteBufUtil.utf8Bytes((CharSequence)topicName);
                payloadBufferSize += 2 + topicNameBytes;
                ++payloadBufferSize;
            }
            int variablePartSize = variableHeaderBufferSize + payloadBufferSize;
            int fixedHeaderBufferSize = 1 + MqttEncoder.getVariableLengthInt(variablePartSize);
            ByteBuf buf = ctx.alloc().buffer(fixedHeaderBufferSize + variablePartSize);
            buf.writeByte(MqttEncoder.getFixedHeaderByte1(mqttFixedHeader));
            MqttEncoder.writeVariableLengthInt(buf, variablePartSize);
            int messageId = variableHeader.messageId();
            buf.writeShort(messageId);
            buf.writeBytes(propertiesBuf);
            for (MqttTopicSubscription topic : payload.topicSubscriptions()) {
                MqttEncoder.writeUnsafeUTF8String(buf, topic.topicName());
                if (mqttVersion == MqttVersion.MQTT_3_1_1 || mqttVersion == MqttVersion.MQTT_3_1) {
                    buf.writeByte(topic.qualityOfService().value());
                    continue;
                }
                MqttSubscriptionOption option = topic.option();
                int optionEncoded = option.retainHandling().value() << 4;
                if (option.isRetainAsPublished()) {
                    optionEncoded |= 8;
                }
                if (option.isNoLocal()) {
                    optionEncoded |= 4;
                }
                buf.writeByte(optionEncoded |= option.qos().value());
            }
            ByteBuf byteBuf = buf;
            return byteBuf;
        } finally {
            propertiesBuf.release();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ByteBuf encodeUnsubscribeMessage(ChannelHandlerContext ctx, MqttUnsubscribeMessage message) {
        MqttVersion mqttVersion = MqttCodecUtil.getMqttVersion(ctx);
        ByteBuf propertiesBuf = MqttEncoder.encodePropertiesIfNeeded(mqttVersion, ctx.alloc(), message.idAndPropertiesVariableHeader().properties());
        try {
            int variableHeaderBufferSize = 2 + propertiesBuf.readableBytes();
            int payloadBufferSize = 0;
            MqttFixedHeader mqttFixedHeader = message.fixedHeader();
            MqttMessageIdVariableHeader variableHeader = message.variableHeader();
            MqttUnsubscribePayload payload = message.payload();
            for (String topicName : payload.topics()) {
                int topicNameBytes = ByteBufUtil.utf8Bytes((CharSequence)topicName);
                payloadBufferSize += 2 + topicNameBytes;
            }
            int variablePartSize = variableHeaderBufferSize + payloadBufferSize;
            int fixedHeaderBufferSize = 1 + MqttEncoder.getVariableLengthInt(variablePartSize);
            ByteBuf buf = ctx.alloc().buffer(fixedHeaderBufferSize + variablePartSize);
            buf.writeByte(MqttEncoder.getFixedHeaderByte1(mqttFixedHeader));
            MqttEncoder.writeVariableLengthInt(buf, variablePartSize);
            int messageId = variableHeader.messageId();
            buf.writeShort(messageId);
            buf.writeBytes(propertiesBuf);
            for (String topicName : payload.topics()) {
                MqttEncoder.writeUnsafeUTF8String(buf, topicName);
            }
            ByteBuf byteBuf = buf;
            return byteBuf;
        } finally {
            propertiesBuf.release();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ByteBuf encodeSubAckMessage(ChannelHandlerContext ctx, MqttSubAckMessage message) {
        MqttVersion mqttVersion = MqttCodecUtil.getMqttVersion(ctx);
        ByteBuf propertiesBuf = MqttEncoder.encodePropertiesIfNeeded(mqttVersion, ctx.alloc(), message.idAndPropertiesVariableHeader().properties());
        try {
            int variableHeaderBufferSize = 2 + propertiesBuf.readableBytes();
            int payloadBufferSize = message.payload().grantedQoSLevels().size();
            int variablePartSize = variableHeaderBufferSize + payloadBufferSize;
            int fixedHeaderBufferSize = 1 + MqttEncoder.getVariableLengthInt(variablePartSize);
            ByteBuf buf = ctx.alloc().buffer(fixedHeaderBufferSize + variablePartSize);
            buf.writeByte(MqttEncoder.getFixedHeaderByte1(message.fixedHeader()));
            MqttEncoder.writeVariableLengthInt(buf, variablePartSize);
            buf.writeShort(message.variableHeader().messageId());
            buf.writeBytes(propertiesBuf);
            Object object = message.payload().reasonCodes().iterator();
            while (object.hasNext()) {
                int code = object.next();
                buf.writeByte(code);
            }
            object = buf;
            return object;
        } finally {
            propertiesBuf.release();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ByteBuf encodeUnsubAckMessage(ChannelHandlerContext ctx, MqttUnsubAckMessage message) {
        if (message.variableHeader() instanceof MqttMessageIdAndPropertiesVariableHeader) {
            MqttVersion mqttVersion = MqttCodecUtil.getMqttVersion(ctx);
            ByteBuf propertiesBuf = MqttEncoder.encodePropertiesIfNeeded(mqttVersion, ctx.alloc(), message.idAndPropertiesVariableHeader().properties());
            try {
                int variableHeaderBufferSize = 2 + propertiesBuf.readableBytes();
                MqttUnsubAckPayload payload = message.payload();
                int payloadBufferSize = payload == null ? 0 : payload.unsubscribeReasonCodes().size();
                int variablePartSize = variableHeaderBufferSize + payloadBufferSize;
                int fixedHeaderBufferSize = 1 + MqttEncoder.getVariableLengthInt(variablePartSize);
                ByteBuf buf = ctx.alloc().buffer(fixedHeaderBufferSize + variablePartSize);
                buf.writeByte(MqttEncoder.getFixedHeaderByte1(message.fixedHeader()));
                MqttEncoder.writeVariableLengthInt(buf, variablePartSize);
                buf.writeShort(message.variableHeader().messageId());
                buf.writeBytes(propertiesBuf);
                if (payload != null) {
                    for (Short reasonCode : payload.unsubscribeReasonCodes()) {
                        buf.writeByte(reasonCode.shortValue());
                    }
                }
                ByteBuf byteBuf = buf;
                return byteBuf;
            } finally {
                propertiesBuf.release();
            }
        }
        return MqttEncoder.encodeMessageWithOnlySingleByteFixedHeaderAndMessageId(ctx.alloc(), message);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ByteBuf encodePublishMessage(ChannelHandlerContext ctx, MqttPublishMessage message) {
        MqttVersion mqttVersion = MqttCodecUtil.getMqttVersion(ctx);
        MqttFixedHeader mqttFixedHeader = message.fixedHeader();
        MqttPublishVariableHeader variableHeader = message.variableHeader();
        ByteBuf payload = message.payload().duplicate();
        String topicName = variableHeader.topicName();
        int topicNameBytes = ByteBufUtil.utf8Bytes((CharSequence)topicName);
        ByteBuf propertiesBuf = MqttEncoder.encodePropertiesIfNeeded(mqttVersion, ctx.alloc(), message.variableHeader().properties());
        try {
            int variableHeaderBufferSize = 2 + topicNameBytes + (mqttFixedHeader.qosLevel().value() > 0 ? 2 : 0) + propertiesBuf.readableBytes();
            int payloadBufferSize = payload.readableBytes();
            int variablePartSize = variableHeaderBufferSize + payloadBufferSize;
            int fixedHeaderBufferSize = 1 + MqttEncoder.getVariableLengthInt(variablePartSize);
            ByteBuf buf = ctx.alloc().buffer(fixedHeaderBufferSize + variablePartSize);
            buf.writeByte(MqttEncoder.getFixedHeaderByte1(mqttFixedHeader));
            MqttEncoder.writeVariableLengthInt(buf, variablePartSize);
            MqttEncoder.writeExactUTF8String(buf, topicName, topicNameBytes);
            if (mqttFixedHeader.qosLevel().value() > 0) {
                buf.writeShort(variableHeader.packetId());
            }
            buf.writeBytes(propertiesBuf);
            buf.writeBytes(payload);
            ByteBuf byteBuf = buf;
            return byteBuf;
        } finally {
            propertiesBuf.release();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ByteBuf encodePubReplyMessage(ChannelHandlerContext ctx, MqttMessage message) {
        if (message.variableHeader() instanceof MqttPubReplyMessageVariableHeader) {
            int variableHeaderBufferSize;
            boolean includeReasonCode;
            ByteBuf propertiesBuf;
            MqttFixedHeader mqttFixedHeader = message.fixedHeader();
            MqttPubReplyMessageVariableHeader variableHeader = (MqttPubReplyMessageVariableHeader)message.variableHeader();
            int msgId = variableHeader.messageId();
            MqttVersion mqttVersion = MqttCodecUtil.getMqttVersion(ctx);
            if (!(mqttVersion != MqttVersion.MQTT_5 || variableHeader.reasonCode() == 0 && variableHeader.properties().isEmpty())) {
                propertiesBuf = MqttEncoder.encodeProperties(ctx.alloc(), variableHeader.properties());
                includeReasonCode = true;
                variableHeaderBufferSize = 3 + propertiesBuf.readableBytes();
            } else {
                propertiesBuf = Unpooled.EMPTY_BUFFER;
                includeReasonCode = false;
                variableHeaderBufferSize = 2;
            }
            try {
                int fixedHeaderBufferSize = 1 + MqttEncoder.getVariableLengthInt(variableHeaderBufferSize);
                ByteBuf buf = ctx.alloc().buffer(fixedHeaderBufferSize + variableHeaderBufferSize);
                buf.writeByte(MqttEncoder.getFixedHeaderByte1(mqttFixedHeader));
                MqttEncoder.writeVariableLengthInt(buf, variableHeaderBufferSize);
                buf.writeShort(msgId);
                if (includeReasonCode) {
                    buf.writeByte(variableHeader.reasonCode());
                }
                buf.writeBytes(propertiesBuf);
                ByteBuf byteBuf = buf;
                return byteBuf;
            } finally {
                propertiesBuf.release();
            }
        }
        return MqttEncoder.encodeMessageWithOnlySingleByteFixedHeaderAndMessageId(ctx.alloc(), message);
    }

    private static ByteBuf encodeMessageWithOnlySingleByteFixedHeaderAndMessageId(ByteBufAllocator byteBufAllocator, MqttMessage message) {
        MqttFixedHeader mqttFixedHeader = message.fixedHeader();
        MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader)message.variableHeader();
        int msgId = variableHeader.messageId();
        int variableHeaderBufferSize = 2;
        int fixedHeaderBufferSize = 1 + MqttEncoder.getVariableLengthInt(variableHeaderBufferSize);
        ByteBuf buf = byteBufAllocator.buffer(fixedHeaderBufferSize + variableHeaderBufferSize);
        buf.writeByte(MqttEncoder.getFixedHeaderByte1(mqttFixedHeader));
        MqttEncoder.writeVariableLengthInt(buf, variableHeaderBufferSize);
        buf.writeShort(msgId);
        return buf;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ByteBuf encodeReasonCodePlusPropertiesMessage(ChannelHandlerContext ctx, MqttMessage message) {
        if (message.variableHeader() instanceof MqttReasonCodeAndPropertiesVariableHeader) {
            int variableHeaderBufferSize;
            boolean includeReasonCode;
            ByteBuf propertiesBuf;
            MqttVersion mqttVersion = MqttCodecUtil.getMqttVersion(ctx);
            MqttFixedHeader mqttFixedHeader = message.fixedHeader();
            MqttReasonCodeAndPropertiesVariableHeader variableHeader = (MqttReasonCodeAndPropertiesVariableHeader)message.variableHeader();
            if (!(mqttVersion != MqttVersion.MQTT_5 || variableHeader.reasonCode() == 0 && variableHeader.properties().isEmpty())) {
                propertiesBuf = MqttEncoder.encodeProperties(ctx.alloc(), variableHeader.properties());
                includeReasonCode = true;
                variableHeaderBufferSize = 1 + propertiesBuf.readableBytes();
            } else {
                propertiesBuf = Unpooled.EMPTY_BUFFER;
                includeReasonCode = false;
                variableHeaderBufferSize = 0;
            }
            try {
                int fixedHeaderBufferSize = 1 + MqttEncoder.getVariableLengthInt(variableHeaderBufferSize);
                ByteBuf buf = ctx.alloc().buffer(fixedHeaderBufferSize + variableHeaderBufferSize);
                buf.writeByte(MqttEncoder.getFixedHeaderByte1(mqttFixedHeader));
                MqttEncoder.writeVariableLengthInt(buf, variableHeaderBufferSize);
                if (includeReasonCode) {
                    buf.writeByte(variableHeader.reasonCode());
                }
                buf.writeBytes(propertiesBuf);
                ByteBuf byteBuf = buf;
                return byteBuf;
            } finally {
                propertiesBuf.release();
            }
        }
        return MqttEncoder.encodeMessageWithOnlySingleByteFixedHeader(ctx.alloc(), message);
    }

    private static ByteBuf encodeMessageWithOnlySingleByteFixedHeader(ByteBufAllocator byteBufAllocator, MqttMessage message) {
        MqttFixedHeader mqttFixedHeader = message.fixedHeader();
        ByteBuf buf = byteBufAllocator.buffer(2);
        buf.writeByte(MqttEncoder.getFixedHeaderByte1(mqttFixedHeader));
        buf.writeByte(0);
        return buf;
    }

    private static ByteBuf encodePropertiesIfNeeded(MqttVersion mqttVersion, ByteBufAllocator byteBufAllocator, MqttProperties mqttProperties) {
        if (mqttVersion == MqttVersion.MQTT_5) {
            return MqttEncoder.encodeProperties(byteBufAllocator, mqttProperties);
        }
        return Unpooled.EMPTY_BUFFER;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ByteBuf encodeProperties(ByteBufAllocator byteBufAllocator, MqttProperties mqttProperties) {
        ByteBuf byteBuf;
        ByteBuf propertiesHeaderBuf = byteBufAllocator.buffer();
        ByteBuf propertiesBuf = byteBufAllocator.buffer();
        try {
            block14: for (MqttProperties.MqttProperty mqttProperty : mqttProperties.listAll()) {
                MqttProperties.MqttPropertyType propertyType = MqttProperties.MqttPropertyType.valueOf(mqttProperty.propertyId);
                switch (propertyType) {
                    case PAYLOAD_FORMAT_INDICATOR: 
                    case REQUEST_PROBLEM_INFORMATION: 
                    case REQUEST_RESPONSE_INFORMATION: 
                    case MAXIMUM_QOS: 
                    case RETAIN_AVAILABLE: 
                    case WILDCARD_SUBSCRIPTION_AVAILABLE: 
                    case SUBSCRIPTION_IDENTIFIER_AVAILABLE: 
                    case SHARED_SUBSCRIPTION_AVAILABLE: {
                        MqttEncoder.writeVariableLengthInt(propertiesBuf, mqttProperty.propertyId);
                        byte bytePropValue = ((Integer)((MqttProperties.IntegerProperty)mqttProperty).value).byteValue();
                        propertiesBuf.writeByte(bytePropValue);
                        break;
                    }
                    case SERVER_KEEP_ALIVE: 
                    case RECEIVE_MAXIMUM: 
                    case TOPIC_ALIAS_MAXIMUM: 
                    case TOPIC_ALIAS: {
                        MqttEncoder.writeVariableLengthInt(propertiesBuf, mqttProperty.propertyId);
                        short twoBytesInPropValue = ((Integer)((MqttProperties.IntegerProperty)mqttProperty).value).shortValue();
                        propertiesBuf.writeShort(twoBytesInPropValue);
                        break;
                    }
                    case PUBLICATION_EXPIRY_INTERVAL: 
                    case SESSION_EXPIRY_INTERVAL: 
                    case WILL_DELAY_INTERVAL: 
                    case MAXIMUM_PACKET_SIZE: {
                        MqttEncoder.writeVariableLengthInt(propertiesBuf, mqttProperty.propertyId);
                        int fourBytesIntPropValue = (Integer)((MqttProperties.IntegerProperty)mqttProperty).value;
                        propertiesBuf.writeInt(fourBytesIntPropValue);
                        break;
                    }
                    case SUBSCRIPTION_IDENTIFIER: {
                        MqttEncoder.writeVariableLengthInt(propertiesBuf, mqttProperty.propertyId);
                        int vbi = (Integer)((MqttProperties.IntegerProperty)mqttProperty).value;
                        MqttEncoder.writeVariableLengthInt(propertiesBuf, vbi);
                        break;
                    }
                    case CONTENT_TYPE: 
                    case RESPONSE_TOPIC: 
                    case ASSIGNED_CLIENT_IDENTIFIER: 
                    case AUTHENTICATION_METHOD: 
                    case RESPONSE_INFORMATION: 
                    case SERVER_REFERENCE: 
                    case REASON_STRING: {
                        MqttEncoder.writeVariableLengthInt(propertiesBuf, mqttProperty.propertyId);
                        MqttEncoder.writeEagerUTF8String(propertiesBuf, (String)((MqttProperties.StringProperty)mqttProperty).value);
                        break;
                    }
                    case USER_PROPERTY: {
                        List pairs = (List)((MqttProperties.UserProperties)mqttProperty).value;
                        for (MqttProperties.StringPair pair : pairs) {
                            MqttEncoder.writeVariableLengthInt(propertiesBuf, mqttProperty.propertyId);
                            MqttEncoder.writeEagerUTF8String(propertiesBuf, pair.key);
                            MqttEncoder.writeEagerUTF8String(propertiesBuf, pair.value);
                        }
                        continue block14;
                    }
                    case CORRELATION_DATA: 
                    case AUTHENTICATION_DATA: {
                        MqttEncoder.writeVariableLengthInt(propertiesBuf, mqttProperty.propertyId);
                        byte[] binaryPropValue = (byte[])((MqttProperties.BinaryProperty)mqttProperty).value;
                        propertiesBuf.writeShort(binaryPropValue.length);
                        propertiesBuf.writeBytes(binaryPropValue, 0, binaryPropValue.length);
                        break;
                    }
                    default: {
                        throw new EncoderException("Unknown property type: " + (Object)((Object)propertyType));
                    }
                }
            }
            MqttEncoder.writeVariableLengthInt(propertiesHeaderBuf, propertiesBuf.readableBytes());
            propertiesHeaderBuf.writeBytes(propertiesBuf);
            byteBuf = propertiesHeaderBuf;
        } catch (Throwable throwable) {
            try {
                propertiesBuf.release();
                throw throwable;
            } catch (RuntimeException e) {
                propertiesHeaderBuf.release();
                throw e;
            }
        }
        propertiesBuf.release();
        return byteBuf;
    }

    private static int getFixedHeaderByte1(MqttFixedHeader header) {
        int ret = 0;
        ret |= header.messageType().value() << 4;
        if (header.isDup()) {
            ret |= 8;
        }
        ret |= header.qosLevel().value() << 1;
        if (header.isRetain()) {
            ret |= 1;
        }
        return ret;
    }

    private static void writeVariableLengthInt(ByteBuf buf, int num) {
        do {
            int digit = num % 128;
            if ((num /= 128) > 0) {
                digit |= 0x80;
            }
            buf.writeByte(digit);
        } while (num > 0);
    }

    private static int nullableUtf8Bytes(String s) {
        return s == null ? 0 : ByteBufUtil.utf8Bytes((CharSequence)s);
    }

    private static int nullableMaxUtf8Bytes(String s) {
        return s == null ? 0 : ByteBufUtil.utf8MaxBytes((CharSequence)s);
    }

    private static void writeExactUTF8String(ByteBuf buf, String s, int utf8Length) {
        buf.ensureWritable(utf8Length + 2);
        buf.writeShort(utf8Length);
        if (utf8Length > 0) {
            int writtenUtf8Length = ByteBufUtil.reserveAndWriteUtf8((ByteBuf)buf, (CharSequence)s, (int)utf8Length);
            assert (writtenUtf8Length == utf8Length);
        }
    }

    private static void writeEagerUTF8String(ByteBuf buf, String s) {
        int maxUtf8Length = MqttEncoder.nullableMaxUtf8Bytes(s);
        buf.ensureWritable(maxUtf8Length + 2);
        int writerIndex = buf.writerIndex();
        int startUtf8String = writerIndex + 2;
        buf.writerIndex(startUtf8String);
        int utf8Length = s != null ? ByteBufUtil.reserveAndWriteUtf8((ByteBuf)buf, (CharSequence)s, (int)maxUtf8Length) : 0;
        buf.setShort(writerIndex, utf8Length);
    }

    private static void writeUnsafeUTF8String(ByteBuf buf, String s) {
        int writerIndex = buf.writerIndex();
        int startUtf8String = writerIndex + 2;
        buf.writerIndex(startUtf8String);
        int utf8Length = s != null ? ByteBufUtil.reserveAndWriteUtf8((ByteBuf)buf, (CharSequence)s, (int)0) : 0;
        buf.setShort(writerIndex, utf8Length);
    }

    private static int getVariableLengthInt(int num) {
        int count = 0;
        do {
            ++count;
        } while ((num /= 128) > 0);
        return count;
    }
}

