/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity.mime;

import org.apache.hc.client5.http.entity.mime.ContentBody;
import org.apache.hc.client5.http.entity.mime.Header;
import org.apache.hc.client5.http.entity.mime.MultipartPart;
import org.apache.hc.core5.util.Args;

public class FormBodyPart
extends MultipartPart {
    private final String name;

    FormBodyPart(String name, ContentBody body, Header header) {
        super(body, header);
        Args.notNull(name, "Name");
        Args.notNull(body, "Body");
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void addField(String name, String value) {
        Args.notNull(name, "Field name");
        super.addField(name, value);
    }
}

