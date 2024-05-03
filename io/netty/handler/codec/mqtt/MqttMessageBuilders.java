/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectPayload;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttConnectVariableHeader;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdAndPropertiesVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttPubReplyMessageVariableHeader;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttReasonCodeAndPropertiesVariableHeader;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttSubscribePayload;
import io.netty.handler.codec.mqtt.MqttSubscriptionOption;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubAckPayload;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribePayload;
import io.netty.handler.codec.mqtt.MqttVersion;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MqttMessageBuilders {
    public static ConnectBuilder connect() {
        return new ConnectBuilder();
    }

    public static ConnAckBuilder connAck() {
        return new ConnAckBuilder();
    }

    public static PublishBuilder publish() {
        return new PublishBuilder();
    }

    public static SubscribeBuilder subscribe() {
        return new SubscribeBuilder();
    }

    public static UnsubscribeBuilder unsubscribe() {
        return new UnsubscribeBuilder();
    }

    public static PubAckBuilder pubAck() {
        return new PubAckBuilder();
    }

    public static SubAckBuilder subAck() {
        return new SubAckBuilder();
    }

    public static UnsubAckBuilder unsubAck() {
        return new UnsubAckBuilder();
    }

    public static DisconnectBuilder disconnect() {
        return new DisconnectBuilder();
    }

    public static AuthBuilder auth() {
        return new AuthBuilder();
    }

    private MqttMessageBuilders() {
    }

    public static final class AuthBuilder {
        private MqttProperties properties;
        private byte reasonCode;

        AuthBuilder() {
        }

        public AuthBuilder properties(MqttProperties properties) {
            this.properties = properties;
            return this;
        }

        public AuthBuilder reasonCode(byte reasonCode) {
            this.reasonCode = reasonCode;
            return this;
        }

        public MqttMessage build() {
            MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.AUTH, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttReasonCodeAndPropertiesVariableHeader mqttAuthVariableHeader = new MqttReasonCodeAndPropertiesVariableHeader(this.reasonCode, this.properties);
            return new MqttMessage(mqttFixedHeader, mqttAuthVariableHeader);
        }
    }

    public static final class DisconnectBuilder {
        private MqttProperties properties;
        private byte reasonCode;

        DisconnectBuilder() {
        }

        public DisconnectBuilder properties(MqttProperties properties) {
            this.properties = properties;
            return this;
        }

        public DisconnectBuilder reasonCode(byte reasonCode) {
            this.reasonCode = reasonCode;
            return this;
        }

        public MqttMessage build() {
            MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.DISCONNECT, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttReasonCodeAndPropertiesVariableHeader mqttDisconnectVariableHeader = new MqttReasonCodeAndPropertiesVariableHeader(this.reasonCode, this.properties);
            return new MqttMessage(mqttFixedHeader, mqttDisconnectVariableHeader);
        }
    }

    public static final class UnsubAckBuilder {
        private int packetId;
        private MqttProperties properties;
        private final List<Short> reasonCodes = new ArrayList<Short>();

        UnsubAckBuilder() {
        }

        public UnsubAckBuilder packetId(int packetId) {
            this.packetId = packetId;
            return this;
        }

        @Deprecated
        public UnsubAckBuilder packetId(short packetId) {
            return this.packetId(packetId & 0xFFFF);
        }

        public UnsubAckBuilder properties(MqttProperties properties) {
            this.properties = properties;
            return this;
        }

        public UnsubAckBuilder addReasonCode(short reasonCode) {
            this.reasonCodes.add(reasonCode);
            return this;
        }

        public UnsubAckBuilder addReasonCodes(Short ... reasonCodes) {
            this.reasonCodes.addAll(Arrays.asList(reasonCodes));
            return this;
        }

        public MqttUnsubAckMessage build() {
            MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttMessageIdAndPropertiesVariableHeader mqttSubAckVariableHeader = new MqttMessageIdAndPropertiesVariableHeader(this.packetId, this.properties);
            MqttUnsubAckPayload subAckPayload = new MqttUnsubAckPayload(this.reasonCodes);
            return new MqttUnsubAckMessage(mqttFixedHeader, mqttSubAckVariableHeader, subAckPayload);
        }
    }

    public static final class SubAckBuilder {
        private int packetId;
        private MqttProperties properties;
        private final List<MqttQoS> grantedQoses = new ArrayList<MqttQoS>();

        SubAckBuilder() {
        }

        public SubAckBuilder packetId(int packetId) {
            this.packetId = packetId;
            return this;
        }

        @Deprecated
        public SubAckBuilder packetId(short packetId) {
            return this.packetId(packetId & 0xFFFF);
        }

        public SubAckBuilder properties(MqttProperties properties) {
            this.properties = properties;
            return this;
        }

        public SubAckBuilder addGrantedQos(MqttQoS qos) {
            this.grantedQoses.add(qos);
            return this;
        }

        public SubAckBuilder addGrantedQoses(MqttQoS ... qoses) {
            this.grantedQoses.addAll(Arrays.asList(qoses));
            return this;
        }

        public MqttSubAckMessage build() {
            MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttMessageIdAndPropertiesVariableHeader mqttSubAckVariableHeader = new MqttMessageIdAndPropertiesVariableHeader(this.packetId, this.properties);
            int[] grantedQoses = new int[this.grantedQoses.size()];
            int i = 0;
            for (MqttQoS grantedQos : this.grantedQoses) {
                grantedQoses[i++] = grantedQos.value();
            }
            MqttSubAckPayload subAckPayload = new MqttSubAckPayload(grantedQoses);
            return new MqttSubAckMessage(mqttFixedHeader, mqttSubAckVariableHeader, subAckPayload);
        }
    }

    public static final class PubAckBuilder {
        private int packetId;
        private byte reasonCode;
        private MqttProperties properties;

        PubAckBuilder() {
        }

        public PubAckBuilder reasonCode(byte reasonCode) {
            this.reasonCode = reasonCode;
            return this;
        }

        public PubAckBuilder packetId(int packetId) {
            this.packetId = packetId;
            return this;
        }

        @Deprecated
        public PubAckBuilder packetId(short packetId) {
            return this.packetId(packetId & 0xFFFF);
        }

        public PubAckBuilder properties(MqttProperties properties) {
            this.properties = properties;
            return this;
        }

        public MqttMessage build() {
            MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttPubReplyMessageVariableHeader mqttPubAckVariableHeader = new MqttPubReplyMessageVariableHeader(this.packetId, this.reasonCode, this.properties);
            return new MqttMessage(mqttFixedHeader, mqttPubAckVariableHeader);
        }
    }

    public static final class ConnAckPropertiesBuilder {
        private String clientId;
        private Long sessionExpiryInterval;
        private int receiveMaximum;
        private Byte maximumQos;
        private boolean retain;
        private Long maximumPacketSize;
        private int topicAliasMaximum;
        private String reasonString;
        private final MqttProperties.UserProperties userProperties = new MqttProperties.UserProperties();
        private Boolean wildcardSubscriptionAvailable;
        private Boolean subscriptionIdentifiersAvailable;
        private Boolean sharedSubscriptionAvailable;
        private Integer serverKeepAlive;
        private String responseInformation;
        private String serverReference;
        private String authenticationMethod;
        private byte[] authenticationData;

        public MqttProperties build() {
            MqttProperties props = new MqttProperties();
            if (this.clientId != null) {
                props.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.ASSIGNED_CLIENT_IDENTIFIER.value(), this.clientId));
            }
            if (this.sessionExpiryInterval != null) {
                props.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SESSION_EXPIRY_INTERVAL.value(), this.sessionExpiryInterval.intValue()));
            }
            if (this.receiveMaximum > 0) {
                props.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.RECEIVE_MAXIMUM.value(), this.receiveMaximum));
            }
            if (this.maximumQos != null) {
                props.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.MAXIMUM_QOS.value(), this.receiveMaximum));
            }
            props.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.RETAIN_AVAILABLE.value(), this.retain ? 1 : 0));
            if (this.maximumPacketSize != null) {
                props.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.MAXIMUM_PACKET_SIZE.value(), this.maximumPacketSize.intValue()));
            }
            props.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.TOPIC_ALIAS_MAXIMUM.value(), this.topicAliasMaximum));
            if (this.reasonString != null) {
                props.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.REASON_STRING.value(), this.reasonString));
            }
            props.add(this.userProperties);
            if (this.wildcardSubscriptionAvailable != null) {
                props.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.WILDCARD_SUBSCRIPTION_AVAILABLE.value(), this.wildcardSubscriptionAvailable != false ? 1 : 0));
            }
            if (this.subscriptionIdentifiersAvailable != null) {
                props.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SUBSCRIPTION_IDENTIFIER_AVAILABLE.value(), this.subscriptionIdentifiersAvailable != false ? 1 : 0));
            }
            if (this.sharedSubscriptionAvailable != null) {
                props.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SHARED_SUBSCRIPTION_AVAILABLE.value(), this.sharedSubscriptionAvailable != false ? 1 : 0));
            }
            if (this.serverKeepAlive != null) {
                props.add(new MqttProperties.IntegerProperty(MqttProperties.MqttPropertyType.SERVER_KEEP_ALIVE.value(), this.serverKeepAlive));
            }
            if (this.responseInformation != null) {
                props.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.RESPONSE_INFORMATION.value(), this.responseInformation));
            }
            if (this.serverReference != null) {
                props.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.SERVER_REFERENCE.value(), this.serverReference));
            }
            if (this.authenticationMethod != null) {
                props.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.AUTHENTICATION_METHOD.value(), this.authenticationMethod));
            }
            if (this.authenticationData != null) {
                props.add(new MqttProperties.BinaryProperty(MqttProperties.MqttPropertyType.AUTHENTICATION_DATA.value(), this.authenticationData));
            }
            return props;
        }

        public ConnAckPropertiesBuilder sessionExpiryInterval(long seconds) {
            this.sessionExpiryInterval = seconds;
            return this;
        }

        public ConnAckPropertiesBuilder receiveMaximum(int value) {
            this.receiveMaximum = ObjectUtil.checkPositive(value, "value");
            return this;
        }

        public ConnAckPropertiesBuilder maximumQos(byte value) {
            if (value != 0 && value != 1) {
                throw new IllegalArgumentException("maximum QoS property could be 0 or 1");
            }
            this.maximumQos = value;
            return this;
        }

        public ConnAckPropertiesBuilder retainAvailable(boolean retain) {
            this.retain = retain;
            return this;
        }

        public ConnAckPropertiesBuilder maximumPacketSize(long size) {
            this.maximumPacketSize = ObjectUtil.checkPositive(size, "size");
            return this;
        }

        public ConnAckPropertiesBuilder assignedClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public ConnAckPropertiesBuilder topicAliasMaximum(int value) {
            this.topicAliasMaximum = value;
            return this;
        }

        public ConnAckPropertiesBuilder reasonString(String reason) {
            this.reasonString = reason;
            return this;
        }

        public ConnAckPropertiesBuilder userProperty(String name, String value) {
            this.userProperties.add(name, value);
            return this;
        }

        public ConnAckPropertiesBuilder wildcardSubscriptionAvailable(boolean value) {
            this.wildcardSubscriptionAvailable = value;
            return this;
        }

        public ConnAckPropertiesBuilder subscriptionIdentifiersAvailable(boolean value) {
            this.subscriptionIdentifiersAvailable = value;
            return this;
        }

        public ConnAckPropertiesBuilder sharedSubscriptionAvailable(boolean value) {
            this.sharedSubscriptionAvailable = value;
            return this;
        }

        public ConnAckPropertiesBuilder serverKeepAlive(int seconds) {
            this.serverKeepAlive = seconds;
            return this;
        }

        public ConnAckPropertiesBuilder responseInformation(String value) {
            this.responseInformation = value;
            return this;
        }

        public ConnAckPropertiesBuilder serverReference(String host) {
            this.serverReference = host;
            return this;
        }

        public ConnAckPropertiesBuilder authenticationMethod(String methodName) {
            this.authenticationMethod = methodName;
            return this;
        }

        public ConnAckPropertiesBuilder authenticationData(byte[] rawData) {
            this.authenticationData = (byte[])rawData.clone();
            return this;
        }
    }

    public static final class ConnAckBuilder {
        private MqttConnectReturnCode returnCode;
        private boolean sessionPresent;
        private MqttProperties properties = MqttProperties.NO_PROPERTIES;
        private ConnAckPropertiesBuilder propsBuilder;

        private ConnAckBuilder() {
        }

        public ConnAckBuilder returnCode(MqttConnectReturnCode returnCode) {
            this.returnCode = returnCode;
            return this;
        }

        public ConnAckBuilder sessionPresent(boolean sessionPresent) {
            this.sessionPresent = sessionPresent;
            return this;
        }

        public ConnAckBuilder properties(MqttProperties properties) {
            this.properties = properties;
            return this;
        }

        public ConnAckBuilder properties(PropertiesInitializer<ConnAckPropertiesBuilder> consumer) {
            if (this.propsBuilder == null) {
                this.propsBuilder = new ConnAckPropertiesBuilder();
            }
            consumer.apply(this.propsBuilder);
            return this;
        }

        public MqttConnAckMessage build() {
            if (this.propsBuilder != null) {
                this.properties = this.propsBuilder.build();
            }
            MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttConnAckVariableHeader mqttConnAckVariableHeader = new MqttConnAckVariableHeader(this.returnCode, this.sessionPresent, this.properties);
            return new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
        }
    }

    public static interface PropertiesInitializer<T> {
        public void apply(T var1);
    }

    public static final class UnsubscribeBuilder {
        private List<String> topicFilters;
        private int messageId;
        private MqttProperties properties;

        UnsubscribeBuilder() {
        }

        public UnsubscribeBuilder addTopicFilter(String topic) {
            if (this.topicFilters == null) {
                this.topicFilters = new ArrayList<String>(5);
            }
            this.topicFilters.add(topic);
            return this;
        }

        public UnsubscribeBuilder messageId(int messageId) {
            this.messageId = messageId;
            return this;
        }

        public UnsubscribeBuilder properties(MqttProperties properties) {
            this.properties = properties;
            return this;
        }

        public MqttUnsubscribeMessage build() {
            MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, 0);
            MqttMessageIdAndPropertiesVariableHeader mqttVariableHeader = new MqttMessageIdAndPropertiesVariableHeader(this.messageId, this.properties);
            MqttUnsubscribePayload mqttSubscribePayload = new MqttUnsubscribePayload(this.topicFilters);
            return new MqttUnsubscribeMessage(mqttFixedHeader, mqttVariableHeader, mqttSubscribePayload);
        }
    }

    public static final class SubscribeBuilder {
        private List<MqttTopicSubscription> subscriptions;
        private int messageId;
        private MqttProperties properties;

        SubscribeBuilder() {
        }

        public SubscribeBuilder addSubscription(MqttQoS qos, String topic) {
            this.ensureSubscriptionsExist();
            this.subscriptions.add(new MqttTopicSubscription(topic, qos));
            return this;
        }

        public SubscribeBuilder addSubscription(String topic, MqttSubscriptionOption option) {
            this.ensureSubscriptionsExist();
            this.subscriptions.add(new MqttTopicSubscription(topic, option));
            return this;
        }

        public SubscribeBuilder messageId(int messageId) {
            this.messageId = messageId;
            return this;
        }

        public SubscribeBuilder properties(MqttProperties properties) {
            this.properties = properties;
            return this;
        }

        public MqttSubscribeMessage build() {
            MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, 0);
            MqttMessageIdAndPropertiesVariableHeader mqttVariableHeader = new MqttMessageIdAndPropertiesVariableHeader(this.messageId, this.properties);
            MqttSubscribePayload mqttSubscribePayload = new MqttSubscribePayload(this.subscriptions);
            return new MqttSubscribeMessage(mqttFixedHeader, mqttVariableHeader, mqttSubscribePayload);
        }

        private void ensureSubscriptionsExist() {
            if (this.subscriptions == null) {
                this.subscriptions = new ArrayList<MqttTopicSubscription>(5);
            }
        }
    }

    public static final class ConnectBuilder {
        private MqttVersion version = MqttVersion.MQTT_3_1_1;
        private String clientId;
        private boolean cleanSession;
        private boolean hasUser;
        private boolean hasPassword;
        private int keepAliveSecs;
        private MqttProperties willProperties = MqttProperties.NO_PROPERTIES;
        private boolean willFlag;
        private boolean willRetain;
        private MqttQoS willQos = MqttQoS.AT_MOST_ONCE;
        private String willTopic;
        private byte[] willMessage;
        private String username;
        private byte[] password;
        private MqttProperties properties = MqttProperties.NO_PROPERTIES;

        ConnectBuilder() {
        }

        public ConnectBuilder protocolVersion(MqttVersion version) {
            this.version = version;
            return this;
        }

        public ConnectBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public ConnectBuilder cleanSession(boolean cleanSession) {
            this.cleanSession = cleanSession;
            return this;
        }

        public ConnectBuilder keepAlive(int keepAliveSecs) {
            this.keepAliveSecs = keepAliveSecs;
            return this;
        }

        public ConnectBuilder willFlag(boolean willFlag) {
            this.willFlag = willFlag;
            return this;
        }

        public ConnectBuilder willQoS(MqttQoS willQos) {
            this.willQos = willQos;
            return this;
        }

        public ConnectBuilder willTopic(String willTopic) {
            this.willTopic = willTopic;
            return this;
        }

        @Deprecated
        public ConnectBuilder willMessage(String willMessage) {
            this.willMessage(willMessage == null ? null : willMessage.getBytes(CharsetUtil.UTF_8));
            return this;
        }

        public ConnectBuilder willMessage(byte[] willMessage) {
            this.willMessage = willMessage;
            return this;
        }

        public ConnectBuilder willRetain(boolean willRetain) {
            this.willRetain = willRetain;
            return this;
        }

        public ConnectBuilder willProperties(MqttProperties willProperties) {
            this.willProperties = willProperties;
            return this;
        }

        public ConnectBuilder hasUser(boolean value) {
            this.hasUser = value;
            return this;
        }

        public ConnectBuilder hasPassword(boolean value) {
            this.hasPassword = value;
            return this;
        }

        public ConnectBuilder username(String username) {
            this.hasUser = username != null;
            this.username = username;
            return this;
        }

        @Deprecated
        public ConnectBuilder password(String password) {
            this.password(password == null ? null : password.getBytes(CharsetUtil.UTF_8));
            return this;
        }

        public ConnectBuilder password(byte[] password) {
            this.hasPassword = password != null;
            this.password = password;
            return this;
        }

        public ConnectBuilder properties(MqttProperties properties) {
            this.properties = properties;
            return this;
        }

        public MqttConnectMessage build() {
            MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttConnectVariableHeader mqttConnectVariableHeader = new MqttConnectVariableHeader(this.version.protocolName(), this.version.protocolLevel(), this.hasUser, this.hasPassword, this.willRetain, this.willQos.value(), this.willFlag, this.cleanSession, this.keepAliveSecs, this.properties);
            MqttConnectPayload mqttConnectPayload = new MqttConnectPayload(this.clientId, this.willProperties, this.willTopic, this.willMessage, this.username, this.password);
            return new MqttConnectMessage(mqttFixedHeader, mqttConnectVariableHeader, mqttConnectPayload);
        }
    }

    public static final class PublishBuilder {
        private String topic;
        private boolean retained;
        private MqttQoS qos;
        private ByteBuf payload;
        private int messageId;
        private MqttProperties mqttProperties;

        PublishBuilder() {
        }

        public PublishBuilder topicName(String topic) {
            this.topic = topic;
            return this;
        }

        public PublishBuilder retained(boolean retained) {
            this.retained = retained;
            return this;
        }

        public PublishBuilder qos(MqttQoS qos) {
            this.qos = qos;
            return this;
        }

        public PublishBuilder payload(ByteBuf payload) {
            this.payload = payload;
            return this;
        }

        public PublishBuilder messageId(int messageId) {
            this.messageId = messageId;
            return this;
        }

        public PublishBuilder properties(MqttProperties properties) {
            this.mqttProperties = properties;
            return this;
        }

        public MqttPublishMessage build() {
            MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, this.qos, this.retained, 0);
            MqttPublishVariableHeader mqttVariableHeader = new MqttPublishVariableHeader(this.topic, this.messageId, this.mqttProperties);
            return new MqttPublishMessage(mqttFixedHeader, mqttVariableHeader, Unpooled.buffer().writeBytes(this.payload));
        }
    }
}

