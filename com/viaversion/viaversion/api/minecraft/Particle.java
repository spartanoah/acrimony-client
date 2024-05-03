/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;

public final class Particle {
    private final List<ParticleData<?>> arguments = new ArrayList(4);
    private int id;

    public Particle(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public <T> ParticleData<T> getArgument(int index) {
        return this.arguments.get(index);
    }

    public <T> ParticleData<T> removeArgument(int index) {
        return this.arguments.remove(index);
    }

    public List<ParticleData<?>> getArguments() {
        return this.arguments;
    }

    public <T> void add(Type<T> type, T value) {
        this.arguments.add(new ParticleData<T>(type, value));
    }

    public <T> void add(int index, Type<T> type, T value) {
        this.arguments.add(index, new ParticleData<T>(type, value));
    }

    public static final class ParticleData<T> {
        private final Type<T> type;
        private T value;

        public ParticleData(Type<T> type, T value) {
            this.type = type;
            this.value = value;
        }

        public Type<T> getType() {
            return this.type;
        }

        public T getValue() {
            return this.value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public void write(ByteBuf buf) throws Exception {
            this.type.write(buf, this.value);
        }

        public void write(PacketWrapper wrapper) {
            wrapper.write(this.type, this.value);
        }

        public String toString() {
            return "ParticleData{type=" + this.type + ", value=" + this.value + '}';
        }
    }
}

