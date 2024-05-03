/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package joptsimple;

import java.util.Collection;
import joptsimple.OptionException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class MissingRequiredOptionException
extends OptionException {
    private static final long serialVersionUID = -1L;

    protected MissingRequiredOptionException(Collection<String> options) {
        super(options);
    }

    @Override
    public String getMessage() {
        return "Missing required option(s) " + this.multipleOptionMessage();
    }
}

