/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package io.netty.handler.codec.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MqttSubAckPayload {
    private final List<Integer> reasonCodes;

    public MqttSubAckPayload(int ... reasonCodes) {
        ObjectUtil.checkNotNull(reasonCodes, "reasonCodes");
        ArrayList<Integer> list = new ArrayList<Integer>(reasonCodes.length);
        for (int v : reasonCodes) {
            list.add(v);
        }
        this.reasonCodes = Collections.unmodifiableList(list);
    }

    public MqttSubAckPayload(Iterable<Integer> reasonCodes) {
        ObjectUtil.checkNotNull(reasonCodes, "reasonCodes");
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (Integer v : reasonCodes) {
            if (v == null) break;
            list.add(v);
        }
        this.reasonCodes = Collections.unmodifiableList(list);
    }

    public List<Integer> grantedQoSLevels() {
        ArrayList<Integer> qosLevels = new ArrayList<Integer>(this.reasonCodes.size());
        for (int code : this.reasonCodes) {
            if (code > MqttQoS.EXACTLY_ONCE.value()) {
                qosLevels.add(MqttQoS.FAILURE.value());
                continue;
            }
            qosLevels.add(code);
        }
        return qosLevels;
    }

    public List<Integer> reasonCodes() {
        return this.reasonCodes;
    }

    public String toString() {
        return StringUtil.simpleClassName(this) + '[' + "reasonCodes=" + this.reasonCodes + ']';
    }
}

