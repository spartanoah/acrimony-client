/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.impl;

public class IllegalIcuArgumentException
extends IllegalArgumentException {
    private static final long serialVersionUID = 3789261542830211225L;

    public IllegalIcuArgumentException(String errorMessage) {
        super(errorMessage);
    }

    public IllegalIcuArgumentException(Throwable cause) {
        super(cause);
    }

    public IllegalIcuArgumentException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }

    public synchronized IllegalIcuArgumentException initCause(Throwable cause) {
        return (IllegalIcuArgumentException)super.initCause(cause);
    }
}

