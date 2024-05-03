/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.util.collection.IntObjectHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class MqttProperties {
    public static final MqttProperties NO_PROPERTIES = new MqttProperties(false);
    private IntObjectHashMap<MqttProperty> props;
    private List<UserProperty> userProperties;
    private List<IntegerProperty> subscriptionIds;
    private final boolean canModify;

    static MqttProperties withEmptyDefaults(MqttProperties properties) {
        if (properties == null) {
            return NO_PROPERTIES;
        }
        return properties;
    }

    public MqttProperties() {
        this(true);
    }

    private MqttProperties(boolean canModify) {
        this.canModify = canModify;
    }

    /*
     * WARNING - void declaration
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void add(MqttProperty property) {
        if (!this.canModify) {
            throw new UnsupportedOperationException("adding property isn't allowed");
        }
        IntObjectHashMap<MqttProperty> intObjectHashMap = this.props;
        if (property.propertyId == MqttPropertyType.USER_PROPERTY.value) {
            List<UserProperty> userProperties = this.userProperties;
            if (userProperties == null) {
                this.userProperties = userProperties = new ArrayList<UserProperty>(1);
            }
            if (property instanceof UserProperty) {
                userProperties.add((UserProperty)property);
                return;
            } else {
                if (!(property instanceof UserProperties)) throw new IllegalArgumentException("User property must be of UserProperty or UserProperties type");
                for (StringPair pair : (List)((UserProperties)property).value) {
                    userProperties.add(new UserProperty(pair.key, pair.value));
                }
            }
            return;
        } else if (property.propertyId == MqttPropertyType.SUBSCRIPTION_IDENTIFIER.value) {
            List<IntegerProperty> subscriptionIds = this.subscriptionIds;
            if (subscriptionIds == null) {
                this.subscriptionIds = subscriptionIds = new ArrayList<IntegerProperty>(1);
            }
            if (!(property instanceof IntegerProperty)) throw new IllegalArgumentException("Subscription ID must be an integer property");
            subscriptionIds.add((IntegerProperty)property);
            return;
        } else {
            void var2_4;
            if (intObjectHashMap == null) {
                IntObjectHashMap intObjectHashMap2 = new IntObjectHashMap();
                this.props = intObjectHashMap2;
            }
            var2_4.put(property.propertyId, property);
        }
    }

    public Collection<? extends MqttProperty> listAll() {
        IntObjectHashMap<MqttProperty> props = this.props;
        if (props == null && this.subscriptionIds == null && this.userProperties == null) {
            return Collections.emptyList();
        }
        if (this.subscriptionIds == null && this.userProperties == null) {
            return props.values();
        }
        if (props == null && this.userProperties == null) {
            return this.subscriptionIds;
        }
        ArrayList<MqttProperty> propValues = new ArrayList<MqttProperty>(props != null ? props.size() : 1);
        if (props != null) {
            propValues.addAll(props.values());
        }
        if (this.subscriptionIds != null) {
            propValues.addAll(this.subscriptionIds);
        }
        if (this.userProperties != null) {
            propValues.add(UserProperties.fromUserPropertyCollection(this.userProperties));
        }
        return propValues;
    }

    public boolean isEmpty() {
        IntObjectHashMap<MqttProperty> props = this.props;
        return props == null || props.isEmpty();
    }

    public MqttProperty getProperty(int propertyId) {
        if (propertyId == MqttPropertyType.USER_PROPERTY.value) {
            List<UserProperty> userProperties = this.userProperties;
            if (userProperties == null) {
                return null;
            }
            return UserProperties.fromUserPropertyCollection(userProperties);
        }
        if (propertyId == MqttPropertyType.SUBSCRIPTION_IDENTIFIER.value) {
            List<IntegerProperty> subscriptionIds = this.subscriptionIds;
            if (subscriptionIds == null || subscriptionIds.isEmpty()) {
                return null;
            }
            return subscriptionIds.get(0);
        }
        IntObjectHashMap<MqttProperty> props = this.props;
        return props == null ? null : props.get(propertyId);
    }

    public List<? extends MqttProperty> getProperties(int propertyId) {
        if (propertyId == MqttPropertyType.USER_PROPERTY.value) {
            return this.userProperties == null ? Collections.emptyList() : this.userProperties;
        }
        if (propertyId == MqttPropertyType.SUBSCRIPTION_IDENTIFIER.value) {
            return this.subscriptionIds == null ? Collections.emptyList() : this.subscriptionIds;
        }
        IntObjectHashMap<MqttProperty> props = this.props;
        return props == null || !props.containsKey(propertyId) ? Collections.emptyList() : Collections.singletonList(props.get(propertyId));
    }

    public static final class BinaryProperty
    extends MqttProperty<byte[]> {
        public BinaryProperty(int propertyId, byte[] value) {
            super(propertyId, value);
        }

        public String toString() {
            return "BinaryProperty(" + this.propertyId + ", " + ((byte[])this.value).length + " bytes)";
        }
    }

    public static final class UserProperty
    extends MqttProperty<StringPair> {
        public UserProperty(String key, String value) {
            super(MqttPropertyType.USER_PROPERTY.value, new StringPair(key, value));
        }

        public String toString() {
            return "UserProperty(" + ((StringPair)this.value).key + ", " + ((StringPair)this.value).value + ")";
        }
    }

    public static final class UserProperties
    extends MqttProperty<List<StringPair>> {
        public UserProperties() {
            super(MqttPropertyType.USER_PROPERTY.value, new ArrayList());
        }

        public UserProperties(Collection<StringPair> values) {
            this();
            ((List)this.value).addAll(values);
        }

        private static UserProperties fromUserPropertyCollection(Collection<UserProperty> properties) {
            UserProperties userProperties = new UserProperties();
            for (UserProperty property : properties) {
                userProperties.add(new StringPair(((StringPair)property.value).key, ((StringPair)property.value).value));
            }
            return userProperties;
        }

        public void add(StringPair pair) {
            ((List)this.value).add(pair);
        }

        public void add(String key, String value) {
            ((List)this.value).add(new StringPair(key, value));
        }

        public String toString() {
            StringBuilder builder = new StringBuilder("UserProperties(");
            boolean first = true;
            for (StringPair pair : (List)this.value) {
                if (!first) {
                    builder.append(", ");
                }
                builder.append(pair.key + "->" + pair.value);
                first = false;
            }
            builder.append(")");
            return builder.toString();
        }
    }

    public static final class StringPair {
        public final String key;
        public final String value;

        public StringPair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public int hashCode() {
            return this.key.hashCode() + 31 * this.value.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            StringPair that = (StringPair)obj;
            return that.key.equals(this.key) && that.value.equals(this.value);
        }
    }

    public static final class StringProperty
    extends MqttProperty<String> {
        public StringProperty(int propertyId, String value) {
            super(propertyId, value);
        }

        public String toString() {
            return "StringProperty(" + this.propertyId + ", " + (String)this.value + ")";
        }
    }

    public static final class IntegerProperty
    extends MqttProperty<Integer> {
        public IntegerProperty(int propertyId, Integer value) {
            super(propertyId, value);
        }

        public String toString() {
            return "IntegerProperty(" + this.propertyId + ", " + this.value + ")";
        }
    }

    public static abstract class MqttProperty<T> {
        final T value;
        final int propertyId;

        protected MqttProperty(int propertyId, T value) {
            this.propertyId = propertyId;
            this.value = value;
        }

        public T value() {
            return this.value;
        }

        public int propertyId() {
            return this.propertyId;
        }

        public int hashCode() {
            return this.propertyId + 31 * this.value.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            MqttProperty that = (MqttProperty)obj;
            return this.propertyId == that.propertyId && this.value.equals(that.value);
        }
    }

    public static enum MqttPropertyType {
        PAYLOAD_FORMAT_INDICATOR(1),
        REQUEST_PROBLEM_INFORMATION(23),
        REQUEST_RESPONSE_INFORMATION(25),
        MAXIMUM_QOS(36),
        RETAIN_AVAILABLE(37),
        WILDCARD_SUBSCRIPTION_AVAILABLE(40),
        SUBSCRIPTION_IDENTIFIER_AVAILABLE(41),
        SHARED_SUBSCRIPTION_AVAILABLE(42),
        SERVER_KEEP_ALIVE(19),
        RECEIVE_MAXIMUM(33),
        TOPIC_ALIAS_MAXIMUM(34),
        TOPIC_ALIAS(35),
        PUBLICATION_EXPIRY_INTERVAL(2),
        SESSION_EXPIRY_INTERVAL(17),
        WILL_DELAY_INTERVAL(24),
        MAXIMUM_PACKET_SIZE(39),
        SUBSCRIPTION_IDENTIFIER(11),
        CONTENT_TYPE(3),
        RESPONSE_TOPIC(8),
        ASSIGNED_CLIENT_IDENTIFIER(18),
        AUTHENTICATION_METHOD(21),
        RESPONSE_INFORMATION(26),
        SERVER_REFERENCE(28),
        REASON_STRING(31),
        USER_PROPERTY(38),
        CORRELATION_DATA(9),
        AUTHENTICATION_DATA(22);

        private static final MqttPropertyType[] VALUES;
        private final int value;

        private MqttPropertyType(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }

        public static MqttPropertyType valueOf(int type) {
            MqttPropertyType t = null;
            try {
                t = VALUES[type];
            } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                // empty catch block
            }
            if (t == null) {
                throw new IllegalArgumentException("unknown property type: " + type);
            }
            return t;
        }

        static {
            VALUES = new MqttPropertyType[43];
            MqttPropertyType[] mqttPropertyTypeArray = MqttPropertyType.values();
            int n = mqttPropertyTypeArray.length;
            for (int i = 0; i < n; ++i) {
                MqttPropertyType v;
                MqttPropertyType.VALUES[v.value] = v = mqttPropertyTypeArray[i];
            }
        }
    }
}

