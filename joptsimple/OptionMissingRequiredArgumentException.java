/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package joptsimple;

import java.util.Collection;
import joptsimple.OptionException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class OptionMissingRequiredArgumentException
extends OptionException {
    private static final long serialVersionUID = -1L;

    OptionMissingRequiredArgumentException(Collection<String> options) {
        super(options);
    }

    @Override
    public String getMessage() {
        return "Option " + this.multipleOptionMessage() + " requires an argument";
    }
}

