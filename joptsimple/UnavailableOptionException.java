/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package joptsimple;

import java.util.List;
import joptsimple.OptionException;
import joptsimple.OptionSpec;

class UnavailableOptionException
extends OptionException {
    private static final long serialVersionUID = -1L;

    UnavailableOptionException(List<? extends OptionSpec<?>> forbiddenOptions) {
        super(forbiddenOptions);
    }

    Object[] messageArguments() {
        return new Object[]{this.multipleOptionString()};
    }
}

