/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity;

import org.apache.hc.client5.http.entity.DecompressingEntity;
import org.apache.hc.client5.http.entity.GZIPInputStreamFactory;
import org.apache.hc.core5.http.HttpEntity;

public class GzipDecompressingEntity
extends DecompressingEntity {
    public GzipDecompressingEntity(HttpEntity entity) {
        super(entity, GZIPInputStreamFactory.getInstance());
    }
}

