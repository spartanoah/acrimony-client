/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.minecraft.signature.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.minecraft.signature.util.DataConsumer;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatSession
implements StorableObject {
    private final UUID uuid;
    private final PrivateKey privateKey;
    private final ProfileKey profileKey;
    private final Signature signer;

    public ChatSession(UUID uuid, PrivateKey privateKey, ProfileKey profileKey) {
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(privateKey, "privateKey");
        Objects.requireNonNull(profileKey, "profileKey");
        this.uuid = uuid;
        this.privateKey = privateKey;
        this.profileKey = profileKey;
        try {
            this.signer = Signature.getInstance("SHA256withRSA");
            this.signer.initSign(this.privateKey);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to initialize signature", e);
        }
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public ProfileKey getProfileKey() {
        return this.profileKey;
    }

    public byte[] sign(Consumer<DataConsumer> dataConsumer) throws SignatureException {
        dataConsumer.accept(bytes -> {
            try {
                this.signer.update((byte[])bytes);
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }
        });
        return this.signer.sign();
    }
}

