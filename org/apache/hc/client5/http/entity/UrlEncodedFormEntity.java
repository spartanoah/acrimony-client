/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.entity;

import java.nio.charset.Charset;
import java.util.List;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URLEncodedUtils;

public class UrlEncodedFormEntity
extends StringEntity {
    public UrlEncodedFormEntity(Iterable<? extends NameValuePair> parameters, Charset charset) {
        super(URLEncodedUtils.format(parameters, charset != null ? charset : ContentType.APPLICATION_FORM_URLENCODED.getCharset()), charset != null ? ContentType.APPLICATION_FORM_URLENCODED.withCharset(charset) : ContentType.APPLICATION_FORM_URLENCODED);
    }

    public UrlEncodedFormEntity(List<? extends NameValuePair> parameters) {
        this(parameters, null);
    }

    public UrlEncodedFormEntity(Iterable<? extends NameValuePair> parameters) {
        this(parameters, null);
    }
}

