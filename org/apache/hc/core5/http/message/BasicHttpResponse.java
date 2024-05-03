/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.core5.http.message;

import java.util.Locale;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.ReasonPhraseCatalog;
import org.apache.hc.core5.http.impl.EnglishReasonPhraseCatalog;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.HeaderGroup;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TextUtils;

public class BasicHttpResponse
extends HeaderGroup
implements HttpResponse {
    private static final long serialVersionUID = 1L;
    private final ReasonPhraseCatalog reasonCatalog;
    private ProtocolVersion version;
    private Locale locale;
    private int code;
    private String reasonPhrase;

    public BasicHttpResponse(int code, ReasonPhraseCatalog catalog, Locale locale) {
        this.code = Args.positive(code, "Status code");
        this.reasonCatalog = catalog != null ? catalog : EnglishReasonPhraseCatalog.INSTANCE;
        this.locale = locale;
    }

    public BasicHttpResponse(int code, String reasonPhrase) {
        this.code = Args.positive(code, "Status code");
        this.reasonPhrase = reasonPhrase;
        this.reasonCatalog = EnglishReasonPhraseCatalog.INSTANCE;
    }

    public BasicHttpResponse(int code) {
        this.code = Args.positive(code, "Status code");
        this.reasonPhrase = null;
        this.reasonCatalog = EnglishReasonPhraseCatalog.INSTANCE;
    }

    @Override
    public void addHeader(String name, Object value) {
        Args.notNull(name, "Header name");
        this.addHeader(new BasicHeader(name, value));
    }

    @Override
    public void setHeader(String name, Object value) {
        Args.notNull(name, "Header name");
        this.setHeader(new BasicHeader(name, value));
    }

    @Override
    public void setVersion(ProtocolVersion version) {
        this.version = version;
    }

    @Override
    public ProtocolVersion getVersion() {
        return this.version;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public void setCode(int code) {
        Args.positive(code, "Status code");
        this.code = code;
        this.reasonPhrase = null;
    }

    @Override
    public String getReasonPhrase() {
        return this.reasonPhrase != null ? this.reasonPhrase : this.getReason(this.code);
    }

    @Override
    public void setReasonPhrase(String reason) {
        this.reasonPhrase = TextUtils.isBlank(reason) ? null : reason;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = Args.notNull(locale, "Locale");
    }

    protected String getReason(int code) {
        return this.reasonCatalog != null ? this.reasonCatalog.getReason(code, this.locale != null ? this.locale : Locale.getDefault()) : null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.code).append(' ').append(this.reasonPhrase).append(' ').append(this.version);
        return sb.toString();
    }
}

