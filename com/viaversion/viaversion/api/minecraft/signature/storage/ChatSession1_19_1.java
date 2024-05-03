/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.minecraft.signature.storage;

import com.viaversion.viaversion.api.minecraft.PlayerMessageSignature;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.minecraft.signature.model.DecoratableMessage;
import com.viaversion.viaversion.api.minecraft.signature.model.MessageMetadata;
import com.viaversion.viaversion.api.minecraft.signature.model.chain.v1_19_1.MessageBody;
import com.viaversion.viaversion.api.minecraft.signature.model.chain.v1_19_1.MessageHeader;
import com.viaversion.viaversion.api.minecraft.signature.storage.ChatSession;
import com.viaversion.viaversion.api.minecraft.signature.util.DataConsumer;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.UUID;

public class ChatSession1_19_1
extends ChatSession {
    private byte[] precedingSignature;

    public ChatSession1_19_1(UUID uuid, PrivateKey privateKey, ProfileKey profileKey) {
        super(uuid, privateKey, profileKey);
    }

    public byte[] signChatMessage(MessageMetadata metadata, DecoratableMessage content, PlayerMessageSignature[] lastSeenMessages) throws SignatureException {
        byte[] signature = this.sign(signer -> {
            MessageHeader messageHeader = new MessageHeader(this.precedingSignature, metadata.sender());
            MessageBody messageBody = new MessageBody(content, metadata.timestamp(), metadata.salt(), lastSeenMessages);
            messageHeader.update((DataConsumer)signer);
            messageBody.update((DataConsumer)signer);
        });
        this.precedingSignature = signature;
        return signature;
    }
}

