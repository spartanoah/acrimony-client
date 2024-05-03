/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package joptsimple;

import java.util.Collection;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.ArgumentList;
import joptsimple.OptionMissingRequiredArgumentException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class RequiredArgumentOptionSpec<V>
extends ArgumentAcceptingOptionSpec<V> {
    RequiredArgumentOptionSpec(String option) {
        super(option, true);
    }

    RequiredArgumentOptionSpec(Collection<String> options, String description) {
        super(options, true, description);
    }

    @Override
    protected void detectOptionArgument(OptionParser parser, ArgumentList arguments, OptionSet detectedOptions) {
        if (!arguments.hasMore()) {
            throw new OptionMissingRequiredArgumentException(this.options());
        }
        this.addArguments(detectedOptions, arguments.next());
    }
}

