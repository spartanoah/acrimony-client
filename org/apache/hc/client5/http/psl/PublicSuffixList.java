/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.hc.client5.http.psl;

import java.util.Collections;
import java.util.List;
import org.apache.hc.client5.http.psl.DomainType;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;

@Contract(threading=ThreadingBehavior.IMMUTABLE)
public final class PublicSuffixList {
    private final DomainType type;
    private final List<String> rules;
    private final List<String> exceptions;

    public PublicSuffixList(DomainType type, List<String> rules, List<String> exceptions) {
        this.type = Args.notNull(type, "Domain type");
        this.rules = Collections.unmodifiableList(Args.notNull(rules, "Domain suffix rules"));
        this.exceptions = Collections.unmodifiableList(exceptions != null ? exceptions : Collections.emptyList());
    }

    public PublicSuffixList(List<String> rules, List<String> exceptions) {
        this(DomainType.UNKNOWN, rules, exceptions);
    }

    public DomainType getType() {
        return this.type;
    }

    public List<String> getRules() {
        return this.rules;
    }

    public List<String> getExceptions() {
        return this.exceptions;
    }
}

