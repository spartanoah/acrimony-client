/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.annotation;

public enum PropertyAccessor {
    GETTER,
    SETTER,
    CREATOR,
    FIELD,
    IS_GETTER,
    NONE,
    ALL;


    public boolean creatorEnabled() {
        return this == CREATOR || this == ALL;
    }

    public boolean getterEnabled() {
        return this == GETTER || this == ALL;
    }

    public boolean isGetterEnabled() {
        return this == IS_GETTER || this == ALL;
    }

    public boolean setterEnabled() {
        return this == SETTER || this == ALL;
    }

    public boolean fieldEnabled() {
        return this == FIELD || this == ALL;
    }
}

