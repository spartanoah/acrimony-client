/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package joptsimple;

import java.util.Collection;
import java.util.List;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface OptionDescriptor {
    public Collection<String> options();

    public String description();

    public List<?> defaultValues();

    public boolean isRequired();

    public boolean acceptsArguments();

    public boolean requiresArgument();

    public String argumentDescription();

    public String argumentTypeIndicator();

    public boolean representsNonOptions();
}

