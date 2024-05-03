/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package joptsimple;

import java.util.Collection;
import joptsimple.OptionException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class OptionArgumentConversionException
extends OptionException {
    private static final long serialVersionUID = -1L;
    private final String argument;

    OptionArgumentConversionException(Collection<String> options, String argument, Throwable cause) {
        super(options, cause);
        this.argument = argument;
    }

    @Override
    public String getMessage() {
        return "Cannot parse argument '" + this.argument + "' of option " + this.multipleOptionMessage();
    }
}

