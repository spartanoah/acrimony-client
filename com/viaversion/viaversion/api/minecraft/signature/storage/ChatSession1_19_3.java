/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.viaversion.viaversion.api.minecraft.signature.storage;

import com.google.common.primitives.Ints;
import com.viaversion.viaversion.api.minecraft.PlayerMessageSignature;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.minecraft.signature.model.MessageMetadata;
import com.viaversion.viaversion.api.minecraft.signature.model.chain.v1_19_3.MessageBody;
import com.viaversion.viaversion.api.minecraft.signature.model.chain.v1_19_3.MessageLink;
import com.viaversion.viaversion.api.minecraft.signature.storage.ChatSession;
import com.viaversion.viaversion.api.minecraft.signature.util.DataConsumer;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.UUID;

public class ChatSession1_19_3
extends ChatSession {
    private final UUID sessionId = UUID.randomUUID();
    private MessageLink link;

    public ChatSession1_19_3(UUID uuid, PrivateKey privateKey, ProfileKey profileKey) {
        super(uuid, privateKey, profileKey);
        this.link = new MessageLink(uuid, this.sessionId);
    }

    public byte[] signChatMessage(MessageMetadata metadata, String content, PlayerMessageSignature[] lastSeenMessages) throws SignatureException {
        return this.sign(signer -> {
            MessageLink messageLink = this.nextLink();
            MessageBody messageBody = new MessageBody(content, metadata.timestamp(), metadata.salt(), lastSeenMessages);
            signer.accept(Ints.toByteArray(1));
            messageLink.update((DataConsumer)signer);
            messageBody.update((DataConsumer)signer);
        });
    }

    private MessageLink nextLink() {
        MessageLink messageLink = this.link;
        if (messageLink != null) {
            this.link = messageLink.next();
        }
        return messageLink;
    }

    public UUID getSessionId() {
        return this.sessionId;
    }
}

