/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.mqtt.MqttCodecUtil;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttIdentifierRejectedException;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdAndPropertiesVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttPubReplyMessageVariableHeader;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttReasonCodeAndPropertiesVariableHeader;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribePayload;
import io.netty.handler.codec.mqtt.MqttSubscriptionOption;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.handler.codec.mqtt.MqttUnsubAckPayload;
import io.netty.handler.codec.mqtt.MqttUnsubscribePayload;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;
import java.util.ArrayList;
import java.util.List;

public final class MqttDecoder
extends ReplayingDecoder<DecoderState> {
    private MqttFixedHeader mqttFixedHeader;
    private Object variableHeader;
    private int bytesRemainingInVariablePart;
    private final int maxBytesInMessage;
    private final int maxClientIdLength;

    public MqttDecoder() {
        this(8092, 23);
    }

    public MqttDecoder(int maxBytesInMessage) {
        this(maxBytesInMessage, 23);
    }

    public MqttDecoder(int maxBytesInMessage, int maxClientIdLength) {
        super(DecoderState.READ_FIXED_HEADER);
        this.maxBytesInMessage = ObjectUtil.checkPositive(maxBytesInMessage, "maxBytesInMessage");
        this.maxClientIdLength = ObjectUtil.checkPositive(maxClientIdLength, "maxClientIdLength");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        switch ((DecoderState)((Object)this.state())) {
            case READ_FIXED_HEADER: {
                try {
                    this.mqttFixedHeader = MqttDecoder.decodeFixedHeader(ctx, buffer);
                    this.bytesRemainingInVariablePart = this.mqttFixedHeader.remainingLength();
                    this.checkpoint(DecoderState.READ_VARIABLE_HEADER);
                } catch (Exception cause) {
                    out.add(this.invalidMessage(cause));
                    return;
                }
            }
            case READ_VARIABLE_HEADER: {
                try {
                    Result<?> decodedVariableHeader = this.decodeVariableHeader(ctx, buffer, this.mqttFixedHeader);
                    this.variableHeader = ((Result)decodedVariableHeader).value;
                    if (this.bytesRemainingInVariablePart > this.maxBytesInMessage) {
                        buffer.skipBytes(this.actualReadableBytes());
                        throw new TooLongFrameException("too large message: " + this.bytesRemainingInVariablePart + " bytes");
                    }
                    this.bytesRemainingInVariablePart -= ((Result)decodedVariableHeader).numberOfBytesConsumed;
                    this.checkpoint(DecoderState.READ_PAYLOAD);
                } catch (Exception cause) {
                    out.add(this.invalidMessage(cause));
                    return;
                }
            }
            case READ_PAYLOAD: {
                try {
                    Result<?> decodedPayload = MqttDecoder.decodePayload(ctx, buffer, this.mqttFixedHeader.messageType(), this.bytesRemainingInVariablePart, this.maxClientIdLength, this.variableHeader);
                    this.bytesRemainingInVariablePart -= ((Result)decodedPayload).numberOfBytesConsumed;
                    if (this.bytesRemainingInVariablePart != 0) {
                        throw new DecoderException("non-zero remaining payload bytes: " + this.bytesRemainingInVariablePart + " (" + (Object)((Object)this.mqttFixedHeader.messageType()) + ')');
                    }
                    this.checkpoint(DecoderState.READ_FIXED_HEADER);
                    MqttMessage message = MqttMessageFactory.newMessage(this.mqttFixedHeader, this.variableHeader, ((Result)decodedPayload).value);
                    this.mqttFixedHeader = null;
                    this.variableHeader = null;
                    out.add(message);
                    break;
                } catch (Exception cause) {
                    out.add(this.invalidMessage(cause));
                    return;
                }
            }
            case BAD_MESSAGE: {
                buffer.skipBytes(this.actualReadableBytes());
                break;
            }
            default: {
                throw new Error();
            }
        }
    }

    private MqttMessage invalidMessage(Throwable cause) {
        this.checkpoint(DecoderState.BAD_MESSAGE);
        return MqttMessageFactory.newInvalidMessage(this.mqttFixedHeader, this.variableHeader, cause);
    }

    private static MqttFixedHeader decodeFixedHeader(ChannelHandlerContext ctx, ByteBuf buffer) {
        short digit;
        short b1 = buffer.readUnsignedByte();
        MqttMessageType messageType = MqttMessageType.valueOf(b1 >> 4);
        boolean dupFlag = (b1 & 8) == 8;
        int qosLevel = (b1 & 6) >> 1;
        boolean retain = (b1 & 1) != 0;
        switch (messageType) {
            case PUBLISH: {
                if (qosLevel != 3) break;
                throw new DecoderException("Illegal QOS Level in fixed header of PUBLISH message (" + qosLevel + ')');
            }
            case PUBREL: 
            case SUBSCRIBE: 
            case UNSUBSCRIBE: {
                if (dupFlag) {
                    throw new DecoderException("Illegal BIT 3 in fixed header of " + (Object)((Object)messageType) + " message, must be 0, found 1");
                }
                if (qosLevel != 1) {
                    throw new DecoderException("Illegal QOS Level in fixed header of " + (Object)((Object)messageType) + " message, must be 1, found " + qosLevel);
                }
                if (!retain) break;
                throw new DecoderException("Illegal BIT 0 in fixed header of " + (Object)((Object)messageType) + " message, must be 0, found 1");
            }
            case AUTH: 
            case CONNACK: 
            case CONNECT: 
            case DISCONNECT: 
            case PINGREQ: 
            case PINGRESP: 
            case PUBACK: 
            case PUBCOMP: 
            case PUBREC: 
            case SUBACK: 
            case UNSUBACK: {
                if (dupFlag) {
                    throw new DecoderException("Illegal BIT 3 in fixed header of " + (Object)((Object)messageType) + " message, must be 0, found 1");
                }
                if (qosLevel != 0) {
                    throw new DecoderException("Illegal BIT 2 or 1 in fixed header of " + (Object)((Object)messageType) + " message, must be 0, found " + qosLevel);
                }
                if (!retain) break;
                throw new DecoderException("Illegal BIT 0 in fixed header of " + (Object)((Object)messageType) + " message, must be 0, found 1");
            }
            default: {
                throw new DecoderException("Unknown message type, do not know how to validate fixed header");
            }
        }
        int remainingLength = 0;
        int multiplier = 1;
        int loops = 0;
        do {
            digit = buffer.readUnsignedByte();
            remainingLength += (digit & 0x7F) * multiplier;
            multiplier *= 128;
        } while ((digit & 0x80) != 0 && ++loops < 4);
        if (loops == 4 && (digit & 0x80) != 0) {
            throw new DecoderException("remaining length exceeds 4 digits (" + (Object)((Object)messageType) + ')');
        }
        MqttFixedHeader decodedFixedHeader = new MqttFixedHeader(messageType, dupFlag, MqttQoS.valueOf(qosLevel), retain, remainingLength);
        return MqttCodecUtil.validateFixedHeader(ctx, MqttCodecUtil.resetUnusedFields(decodedFixedHeader));
    }

    private Result<?> decodeVariableHeader(ChannelHandlerContext ctx, ByteBuf buffer, MqttFixedHeader mqttFixedHeader) {
        switch (mqttFixedHeader.messageType()) {
            case CONNECT: {
                return MqttDecoder.decodeConnectionVariableHeader(ctx, buffer);
            }
            case CONNACK: {
                return MqttDecoder.decodeConnAckVariableHeader(ctx, buffer);
            }
            case SUBSCRIBE: 
            case UNSUBSCRIBE: 
            case SUBACK: 
            case UNSUBACK: {
                return MqttDecoder.decodeMessageIdAndPropertiesVariableHeader(ctx, buffer);
            }
            case PUBREL: 
            case PUBACK: 
            case PUBCOMP: 
            case PUBREC: {
                return this.decodePubReplyMessage(buffer);
            }
            case PUBLISH: {
                return this.decodePublishVariableHeader(ctx, buffer, mqttFixedHeader);
            }
            case AUTH: 
            case DISCONNECT: {
                return this.decodeReasonCodeAndPropertiesVariableHeader(buffer);
            }
            case PINGREQ: 
            case PINGRESP: {
                return new Result<Object>(null, 0);
            }
        }
        throw new DecoderException("Unknown message type: " + (Object)((Object)mqttFixedHeader.messageType()));
    }

    private static Result<MqttConnectVariableHeader> decodeConnectionVariableHeader(ChannelHandlerContext ctx, ByteBuf buffer) {
        MqttProperties properties;
        boolean cleanSession;
        Result<String> protoString = MqttDecoder.decodeString(buffer);
        int numberOfBytesConsumed = ((Result)protoString).numberOfBytesConsumed;
        byte protocolLevel = buffer.readByte();
        ++numberOfBytesConsumed;
        MqttVersion version = MqttVersion.fromProtocolNameAndLevel((String)((Result)protoString).value, protocolLevel);
        MqttCodecUtil.setMqttVersion(ctx, version);
        short b1 = buffer.readUnsignedByte();
        ++numberOfBytesConsumed;
        int keepAlive = MqttDecoder.decodeMsbLsb(buffer);
        numberOfBytesConsumed += 2;
        boolean hasUserName = (b1 & 0x80) == 128;
        boolean hasPassword = (b1 & 0x40) == 64;
        boolean willRetain = (b1 & 0x20) == 32;
        int willQos = (b1 & 0x18) >> 3;
        boolean willFlag = (b1 & 4) == 4;
        boolean bl = cleanSession = (b1 & 2) == 2;
        if (version == MqttVersion.MQTT_3_1_1 || version == MqttVersion.MQTT_5) {
            boolean zeroReservedFlag;
            boolean bl2 = zeroReservedFlag = (b1 & 1) == 0;
            if (!zeroReservedFlag) {
                throw new DecoderException("non-zero reserved flag");
            }
        }
        if (version == MqttVersion.MQTT_5) {
            Result<MqttProperties> propertiesResult = MqttDecoder.decodeProperties(buffer);
            properties = (MqttProperties)((Result)propertiesResult).value;
            numberOfBytesConsumed += ((Result)propertiesResult).numberOfBytesConsumed;
        } else {
            properties = MqttProperties.NO_PROPERTIES;
        }
        MqttConnectVariableHeader mqttConnectVariableHeader = new MqttConnectVariableHeader(version.protocolName(), version.protocolLevel(), hasUserName, hasPassword, willRetain, willQos, willFlag, cleanSession, keepAlive, properties);
        return new Result<MqttConnectVariableHeader>(mqttConnectVariableHeader, numberOfBytesConsumed);
    }

    private static Result<MqttConnAckVariableHeader> decodeConnAckVariableHeader(ChannelHandlerContext ctx, ByteBuf buffer) {
        MqttProperties properties;
        MqttVersion mqttVersion = MqttCodecUtil.getMqttVersion(ctx);
        boolean sessionPresent = (buffer.readUnsignedByte() & 1) == 1;
        byte returnCode = buffer.readByte();
        int numberOfBytesConsumed = 2;
        if (mqttVersion == MqttVersion.MQTT_5) {
            Result<MqttProperties> propertiesResult = MqttDecoder.decodeProperties(buffer);
            properties = (MqttProperties)((Result)propertiesResult).value;
            numberOfBytesConsumed += ((Result)propertiesResult).numberOfBytesConsumed;
        } else {
            properties = MqttProperties.NO_PROPERTIES;
        }
        MqttConnAckVariableHeader mqttConnAckVariableHeader = new MqttConnAckVariableHeader(MqttConnectReturnCode.valueOf(returnCode), sessionPresent, properties);
        return new Result<MqttConnAckVariableHeader>(mqttConnAckVariableHeader, numberOfBytesConsumed);
    }

    private static Result<MqttMessageIdAndPropertiesVariableHeader> decodeMessageIdAndPropertiesVariableHeader(ChannelHandlerContext ctx, ByteBuf buffer) {
        int mqtt5Consumed;
        MqttMessageIdAndPropertiesVariableHeader mqttVariableHeader;
        MqttVersion mqttVersion = MqttCodecUtil.getMqttVersion(ctx);
        int packetId = MqttDecoder.decodeMessageId(buffer);
        if (mqttVersion == MqttVersion.MQTT_5) {
            Result<MqttProperties> properties = MqttDecoder.decodeProperties(buffer);
            mqttVariableHeader = new MqttMessageIdAndPropertiesVariableHeader(packetId, (MqttProperties)((Result)properties).value);
            mqtt5Consumed = ((Result)properties).numberOfBytesConsumed;
        } else {
            mqttVariableHeader = new MqttMessageIdAndPropertiesVariableHeader(packetId, MqttProperties.NO_PROPERTIES);
            mqtt5Consumed = 0;
        }
        return new Result<MqttMessageIdAndPropertiesVariableHeader>(mqttVariableHeader, 2 + mqtt5Consumed);
    }

    private Result<MqttPubReplyMessageVariableHeader> decodePubReplyMessage(ByteBuf buffer) {
        int consumed;
        MqttPubReplyMessageVariableHeader mqttPubAckVariableHeader;
        int packetId = MqttDecoder.decodeMessageId(buffer);
        int packetIdNumberOfBytesConsumed = 2;
        if (this.bytesRemainingInVariablePart > 3) {
            byte reasonCode = buffer.readByte();
            Result<MqttProperties> properties = MqttDecoder.decodeProperties(buffer);
            mqttPubAckVariableHeader = new MqttPubReplyMessageVariableHeader(packetId, reasonCode, (MqttProperties)((Result)properties).value);
            consumed = 3 + ((Result)properties).numberOfBytesConsumed;
        } else if (this.bytesRemainingInVariablePart > 2) {
            byte reasonCode = buffer.readByte();
            mqttPubAckVariableHeader = new MqttPubReplyMessageVariableHeader(packetId, reasonCode, MqttProperties.NO_PROPERTIES);
            consumed = 3;
        } else {
            mqttPubAckVariableHeader = new MqttPubReplyMessageVariableHeader(packetId, 0, MqttProperties.NO_PROPERTIES);
            consumed = 2;
        }
        return new Result<MqttPubReplyMessageVariableHeader>(mqttPubAckVariableHeader, consumed);
    }

    private Result<MqttReasonCodeAndPropertiesVariableHeader> decodeReasonCodeAndPropertiesVariableHeader(ByteBuf buffer) {
        int consumed;
        MqttProperties properties;
        byte reasonCode;
        if (this.bytesRemainingInVariablePart > 1) {
            reasonCode = buffer.readByte();
            Result<MqttProperties> propertiesResult = MqttDecoder.decodeProperties(buffer);
            properties = (MqttProperties)((Result)propertiesResult).value;
            consumed = 1 + ((Result)propertiesResult).numberOfBytesConsumed;
        } else if (this.bytesRemainingInVariablePart > 0) {
            reasonCode = buffer.readByte();
            properties = MqttProperties.NO_PROPERTIES;
            consumed = 1;
        } else {
            reasonCode = 0;
            properties = MqttProperties.NO_PROPERTIES;
            consumed = 0;
        }
        MqttReasonCodeAndPropertiesVariableHeader mqttReasonAndPropsVariableHeader = new MqttReasonCodeAndPropertiesVariableHeader(reasonCode, properties);
        return new Result<MqttReasonCodeAndPropertiesVariableHeader>(mqttReasonAndPropsVariableHeader, consumed);
    }

    private Result<MqttPublishVariableHeader> decodePublishVariableHeader(ChannelHandlerContext ctx, ByteBuf buffer, MqttFixedHeader mqttFixedHeader) {
        MqttProperties properties;
        MqttVersion mqttVersion = MqttCodecUtil.getMqttVersion(ctx);
        Result<String> decodedTopic = MqttDecoder.decodeString(buffer);
        if (!MqttCodecUtil.isValidPublishTopicName((String)((Result)decodedTopic).value)) {
            throw new DecoderException("invalid publish topic name: " + (String)((Result)decodedTopic).value + " (contains wildcards)");
        }
        int numberOfBytesConsumed = ((Result)decodedTopic).numberOfBytesConsumed;
        int messageId = -1;
        if (mqttFixedHeader.qosLevel().value() > 0) {
            messageId = MqttDecoder.decodeMessageId(buffer);
            numberOfBytesConsumed += 2;
        }
        if (mqttVersion == MqttVersion.MQTT_5) {
            Result<MqttProperties> propertiesResult = MqttDecoder.decodeProperties(buffer);
            properties = (MqttProperties)((Result)propertiesResult).value;
            numberOfBytesConsumed += ((Result)propertiesResult).numberOfBytesConsumed;
        } else {
            properties = MqttProperties.NO_PROPERTIES;
        }
        MqttPublishVariableHeader mqttPublishVariableHeader = new MqttPublishVariableHeader((String)((Result)decodedTopic).value, messageId, properties);
        return new Result<MqttPublishVariableHeader>(mqttPublishVariableHeader, numberOfBytesConsumed);
    }

    private static int decodeMessageId(ByteBuf buffer) {
        int messageId = MqttDecoder.decodeMsbLsb(buffer);
        if (!MqttCodecUtil.isValidMessageId(messageId)) {
            throw new DecoderException("invalid messageId: " + messageId);
        }
        return messageId;
    }

    private static Result<?> decodePayload(ChannelHandlerContext ctx, ByteBuf buffer, MqttMessageType messageType, int bytesRemainingInVariablePart, int maxClientIdLength, Object variableHeader) {
        switch (messageType) {
            case CONNECT: {
                return MqttDecoder.decodeConnectionPayload(buffer, maxClientIdLength, (MqttConnectVariableHeader)variableHeader);
            }
            case SUBSCRIBE: {
                return MqttDecoder.decodeSubscribePayload(buffer, bytesRemainingInVariablePart);
            }
            case SUBACK: {
                return MqttDecoder.decodeSubackPayload(buffer, bytesRemainingInVariablePart);
            }
            case UNSUBSCRIBE: {
                return MqttDecoder.decodeUnsubscribePayload(buffer, bytesRemainingInVariablePart);
            }
            case UNSUBACK: {
                return MqttDecoder.decodeUnsubAckPayload(ctx, buffer, bytesRemainingInVariablePart);
            }
            case PUBLISH: {
                return MqttDecoder.decodePublishPayload(buffer, bytesRemainingInVariablePart);
            }
        }
        return new Result<Object>(null, 0);
    }

    private static Result<MqttConnectPayload> decodeConnectionPayload(ByteBuf buffer, int maxClientIdLength, MqttConnectVariableHeader mqttConnectVariableHeader) {
        MqttProperties willProperties;
        Result<String> decodedClientId = MqttDecoder.decodeString(buffer);
        String decodedClientIdValue = (String)((Result)decodedClientId).value;
        MqttVersion mqttVersion = MqttVersion.fromProtocolNameAndLevel(mqttConnectVariableHeader.name(), (byte)mqttConnectVariableHeader.version());
        if (!MqttCodecUtil.isValidClientId(mqttVersion, maxClientIdLength, decodedClientIdValue)) {
            throw new MqttIdentifierRejectedException("invalid clientIdentifier: " + decodedClientIdValue);
        }
        int numberOfBytesConsumed = ((Result)decodedClientId).numberOfBytesConsumed;
        Result<String> decodedWillTopic = null;
        byte[] decodedWillMessage = null;
        if (mqttConnectVariableHeader.isWillFlag()) {
            if (mqttVersion == MqttVersion.MQTT_5) {
                Result<MqttProperties> propertiesResult = MqttDecoder.decodeProperties(buffer);
                willProperties = (MqttProperties)((Result)propertiesResult).value;
                numberOfBytesConsumed += ((Result)propertiesResult).numberOfBytesConsumed;
            } else {
                willProperties = MqttProperties.NO_PROPERTIES;
            }
            decodedWillTopic = MqttDecoder.decodeString(buffer, 0, Short.MAX_VALUE);
            numberOfBytesConsumed += ((Result)decodedWillTopic).numberOfBytesConsumed;
            decodedWillMessage = MqttDecoder.decodeByteArray(buffer);
            numberOfBytesConsumed += decodedWillMessage.length + 2;
        } else {
            willProperties = MqttProperties.NO_PROPERTIES;
        }
        Result<String> decodedUserName = null;
        byte[] decodedPassword = null;
        if (mqttConnectVariableHeader.hasUserName()) {
            decodedUserName = MqttDecoder.decodeString(buffer);
            numberOfBytesConsumed += ((Result)decodedUserName).numberOfBytesConsumed;
        }
        if (mqttConnectVariableHeader.hasPassword()) {
            decodedPassword = MqttDecoder.decodeByteArray(buffer);
            numberOfBytesConsumed += decodedPassword.length + 2;
        }
        MqttConnectPayload mqttConnectPayload = new MqttConnectPayload((String)((Result)decodedClientId).value, willProperties, decodedWillTopic != null ? (String)((Result)decodedWillTopic).value : null, decodedWillMessage, decodedUserName != null ? (String)((Result)decodedUserName).value : null, decodedPassword);
        return new Result<MqttConnectPayload>(mqttConnectPayload, numberOfBytesConsumed);
    }

    private static Result<MqttSubscribePayload> decodeSubscribePayload(ByteBuf buffer, int bytesRemainingInVariablePart) {
        int numberOfBytesConsumed;
        ArrayList<MqttTopicSubscription> subscribeTopics = new ArrayList<MqttTopicSubscription>();
        for (numberOfBytesConsumed = 0; numberOfBytesConsumed < bytesRemainingInVariablePart; ++numberOfBytesConsumed) {
            Result<String> decodedTopicName = MqttDecoder.decodeString(buffer);
            numberOfBytesConsumed += ((Result)decodedTopicName).numberOfBytesConsumed;
            short optionByte = buffer.readUnsignedByte();
            MqttQoS qos = MqttQoS.valueOf(optionByte & 3);
            boolean noLocal = (optionByte & 4) >> 2 == 1;
            boolean retainAsPublished = (optionByte & 8) >> 3 == 1;
            MqttSubscriptionOption.RetainedHandlingPolicy retainHandling = MqttSubscriptionOption.RetainedHandlingPolicy.valueOf((optionByte & 0x30) >> 4);
            MqttSubscriptionOption subscriptionOption = new MqttSubscriptionOption(qos, noLocal, retainAsPublished, retainHandling);
            subscribeTopics.add(new MqttTopicSubscription((String)((Result)decodedTopicName).value, subscriptionOption));
        }
        return new Result<MqttSubscribePayload>(new MqttSubscribePayload(subscribeTopics), numberOfBytesConsumed);
    }

    private static Result<MqttSubAckPayload> decodeSubackPayload(ByteBuf buffer, int bytesRemainingInVariablePart) {
        int numberOfBytesConsumed;
        ArrayList<Integer> grantedQos = new ArrayList<Integer>(bytesRemainingInVariablePart);
        for (numberOfBytesConsumed = 0; numberOfBytesConsumed < bytesRemainingInVariablePart; ++numberOfBytesConsumed) {
            short reasonCode = buffer.readUnsignedByte();
            grantedQos.add(Integer.valueOf(reasonCode));
        }
        return new Result<MqttSubAckPayload>(new MqttSubAckPayload(grantedQos), numberOfBytesConsumed);
    }

    private static Result<MqttUnsubAckPayload> decodeUnsubAckPayload(ChannelHandlerContext ctx, ByteBuf buffer, int bytesRemainingInVariablePart) {
        int numberOfBytesConsumed;
        ArrayList<Short> reasonCodes = new ArrayList<Short>(bytesRemainingInVariablePart);
        for (numberOfBytesConsumed = 0; numberOfBytesConsumed < bytesRemainingInVariablePart; ++numberOfBytesConsumed) {
            short reasonCode = buffer.readUnsignedByte();
            reasonCodes.add(reasonCode);
        }
        return new Result<MqttUnsubAckPayload>(new MqttUnsubAckPayload(reasonCodes), numberOfBytesConsumed);
    }

    private static Result<MqttUnsubscribePayload> decodeUnsubscribePayload(ByteBuf buffer, int bytesRemainingInVariablePart) {
        int numberOfBytesConsumed;
        Result<String> decodedTopicName;
        ArrayList<String> unsubscribeTopics = new ArrayList<String>();
        for (numberOfBytesConsumed = 0; numberOfBytesConsumed < bytesRemainingInVariablePart; numberOfBytesConsumed += ((Result)decodedTopicName).numberOfBytesConsumed) {
            decodedTopicName = MqttDecoder.decodeString(buffer);
            unsubscribeTopics.add((String)((Result)decodedTopicName).value);
        }
        return new Result<MqttUnsubscribePayload>(new MqttUnsubscribePayload(unsubscribeTopics), numberOfBytesConsumed);
    }

    private static Result<ByteBuf> decodePublishPayload(ByteBuf buffer, int bytesRemainingInVariablePart) {
        ByteBuf b = buffer.readRetainedSlice(bytesRemainingInVariablePart);
        return new Result<ByteBuf>(b, bytesRemainingInVariablePart);
    }

    private static Result<String> decodeString(ByteBuf buffer) {
        return MqttDecoder.decodeString(buffer, 0, Integer.MAX_VALUE);
    }

    private static Result<String> decodeString(ByteBuf buffer, int minBytes, int maxBytes) {
        int size = MqttDecoder.decodeMsbLsb(buffer);
        int numberOfBytesConsumed = 2;
        if (size < minBytes || size > maxBytes) {
            buffer.skipBytes(size);
            return new Result<Object>(null, numberOfBytesConsumed += size);
        }
        String s = buffer.toString(buffer.readerIndex(), size, CharsetUtil.UTF_8);
        buffer.skipBytes(size);
        return new Result<String>(s, numberOfBytesConsumed += size);
    }

    private static byte[] decodeByteArray(ByteBuf buffer) {
        int size = MqttDecoder.decodeMsbLsb(buffer);
        byte[] bytes = new byte[size];
        buffer.readBytes(bytes);
        return bytes;
    }

    private static long packInts(int a, int b) {
        return (long)a << 32 | (long)b & 0xFFFFFFFFL;
    }

    private static int unpackA(long ints) {
        return (int)(ints >> 32);
    }

    private static int unpackB(long ints) {
        return (int)ints;
    }

    private static int decodeMsbLsb(ByteBuf buffer) {
        short lsbSize;
        int min = 0;
        int max = 65535;
        short msbSize = buffer.readUnsignedByte();
        int result = msbSize << 8 | (lsbSize = buffer.readUnsignedByte());
        if (result < min || result > max) {
            result = -1;
        }
        return result;
    }

    private static long decodeVariableByteInteger(ByteBuf buffer) {
        short digit;
        int remainingLength = 0;
        int multiplier = 1;
        int loops = 0;
        do {
            digit = buffer.readUnsignedByte();
            remainingLength += (digit & 0x7F) * multiplier;
            multiplier *= 128;
        } while ((digit & 0x80) != 0 && ++loops < 4);
        if (loops == 4 && (digit & 0x80) != 0) {
            throw new DecoderException("MQTT protocol limits Remaining Length to 4 bytes");
        }
        return MqttDecoder.packInts(remainingLength, loops);
    }

    private static Result<MqttProperties> decodeProperties(ByteBuf buffer) {
        long propertiesLength = MqttDecoder.decodeVariableByteInteger(buffer);
        int totalPropertiesLength = MqttDecoder.unpackA(propertiesLength);
        int numberOfBytesConsumed = MqttDecoder.unpackB(propertiesLength);
        MqttProperties decodedProperties = new MqttProperties();
        block9: while (numberOfBytesConsumed < totalPropertiesLength) {
            long propertyId = MqttDecoder.decodeVariableByteInteger(buffer);
            int propertyIdValue = MqttDecoder.unpackA(propertyId);
            numberOfBytesConsumed += MqttDecoder.unpackB(propertyId);
            MqttProperties.MqttPropertyType propertyType = MqttProperties.MqttPropertyType.valueOf(propertyIdValue);
            switch (propertyType) {
                case PAYLOAD_FORMAT_INDICATOR: 
                case REQUEST_PROBLEM_INFORMATION: 
                case REQUEST_RESPONSE_INFORMATION: 
                case MAXIMUM_QOS: 
                case RETAIN_AVAILABLE: 
                case WILDCARD_SUBSCRIPTION_AVAILABLE: 
                case SUBSCRIPTION_IDENTIFIER_AVAILABLE: 
                case SHARED_SUBSCRIPTION_AVAILABLE: {
                    short b1 = buffer.readUnsignedByte();
                    ++numberOfBytesConsumed;
                    decodedProperties.add(new MqttProperties.IntegerProperty(propertyIdValue, Integer.valueOf(b1)));
                    continue block9;
                }
                case SERVER_KEEP_ALIVE: 
                case RECEIVE_MAXIMUM: 
                case TOPIC_ALIAS_MAXIMUM: 
                case TOPIC_ALIAS: {
                    int int2BytesResult = MqttDecoder.decodeMsbLsb(buffer);
                    numberOfBytesConsumed += 2;
                    decodedProperties.add(new MqttProperties.IntegerProperty(propertyIdValue, int2BytesResult));
                    continue block9;
                }
                case PUBLICATION_EXPIRY_INTERVAL: 
                case SESSION_EXPIRY_INTERVAL: 
                case WILL_DELAY_INTERVAL: 
                case MAXIMUM_PACKET_SIZE: {
                    int maxPacketSize = buffer.readInt();
                    numberOfBytesConsumed += 4;
                    decodedProperties.add(new MqttProperties.IntegerProperty(propertyIdValue, maxPacketSize));
                    continue block9;
                }
                case SUBSCRIPTION_IDENTIFIER: {
                    long vbIntegerResult = MqttDecoder.decodeVariableByteInteger(buffer);
                    numberOfBytesConsumed += MqttDecoder.unpackB(vbIntegerResult);
                    decodedProperties.add(new MqttProperties.IntegerProperty(propertyIdValue, MqttDecoder.unpackA(vbIntegerResult)));
                    continue block9;
                }
                case CONTENT_TYPE: 
                case RESPONSE_TOPIC: 
                case ASSIGNED_CLIENT_IDENTIFIER: 
                case AUTHENTICATION_METHOD: 
                case RESPONSE_INFORMATION: 
                case SERVER_REFERENCE: 
                case REASON_STRING: {
                    Result<String> stringResult = MqttDecoder.decodeString(buffer);
                    numberOfBytesConsumed += ((Result)stringResult).numberOfBytesConsumed;
                    decodedProperties.add(new MqttProperties.StringProperty(propertyIdValue, (String)((Result)stringResult).value));
                    continue block9;
                }
                case USER_PROPERTY: {
                    Result<String> keyResult = MqttDecoder.decodeString(buffer);
                    Result<String> valueResult = MqttDecoder.decodeString(buffer);
                    numberOfBytesConsumed += ((Result)keyResult).numberOfBytesConsumed;
                    numberOfBytesConsumed += ((Result)valueResult).numberOfBytesConsumed;
                    decodedProperties.add(new MqttProperties.UserProperty((String)((Result)keyResult).value, (String)((Result)valueResult).value));
                    continue block9;
                }
                case CORRELATION_DATA: 
                case AUTHENTICATION_DATA: {
                    byte[] binaryDataResult = MqttDecoder.decodeByteArray(buffer);
                    numberOfBytesConsumed += binaryDataResult.length + 2;
                    decodedProperties.add(new MqttProperties.BinaryProperty(propertyIdValue, binaryDataResult));
                    continue block9;
                }
            }
            throw new DecoderException("Unknown property type: " + (Object)((Object)propertyType));
        }
        return new Result<MqttProperties>(decodedProperties, numberOfBytesConsumed);
    }

    private static final class Result<T> {
        private final T value;
        private final int numberOfBytesConsumed;

        Result(T value, int numberOfBytesConsumed) {
            this.value = value;
            this.numberOfBytesConsumed = numberOfBytesConsumed;
        }
    }

    static enum DecoderState {
        READ_FIXED_HEADER,
        READ_VARIABLE_HEADER,
        READ_PAYLOAD,
        BAD_MESSAGE;

    }
}

