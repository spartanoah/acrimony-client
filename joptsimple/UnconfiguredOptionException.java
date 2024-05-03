/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package joptsimple;

import java.util.Collection;
import java.util.Collections;
import joptsimple.OptionException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class UnconfiguredOptionException
extends OptionException {
    private static final long serialVersionUID = -1L;

    UnconfiguredOptionException(String option) {
        this(Collections.singletonList(option));
    }

    UnconfiguredOptionException(Collection<String> options) {
        super(options);
    }

    @Override
    public String getMessage() {
        return "Option " + this.multipleOptionMessage() + " has not been configured on this parser";
    }
}

