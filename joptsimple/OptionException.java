/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package joptsimple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import joptsimple.UnrecognizedOptionException;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public abstract class OptionException
extends RuntimeException {
    private static final long serialVersionUID = -1L;
    private final List<String> options = new ArrayList<String>();

    protected OptionException(Collection<String> options) {
        this.options.addAll(options);
    }

    protected OptionException(Collection<String> options, Throwable cause) {
        super(cause);
        this.options.addAll(options);
    }

    public Collection<String> options() {
        return Collections.unmodifiableCollection(this.options);
    }

    protected final String singleOptionMessage() {
        return this.singleOptionMessage(this.options.get(0));
    }

    protected final String singleOptionMessage(String option) {
        return "'" + option + "'";
    }

    protected final String multipleOptionMessage() {
        StringBuilder buffer = new StringBuilder("[");
        Iterator<String> iter = this.options.iterator();
        while (iter.hasNext()) {
            buffer.append(this.singleOptionMessage(iter.next()));
            if (!iter.hasNext()) continue;
            buffer.append(", ");
        }
        buffer.append(']');
        return buffer.toString();
    }

    static OptionException unrecognizedOption(String option) {
        return new UnrecognizedOptionException(option);
    }
}

