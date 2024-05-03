/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package joptsimple;

import java.util.Collection;
import java.util.List;
import joptsimple.OptionSet;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface OptionSpec<V> {
    public List<V> values(OptionSet var1);

    public V value(OptionSet var1);

    public Collection<String> options();

    public boolean isForHelp();
}

