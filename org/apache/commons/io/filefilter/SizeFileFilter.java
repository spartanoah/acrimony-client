/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import org.apache.commons.io.filefilter.AbstractFileFilter;

public class SizeFileFilter
extends AbstractFileFilter
implements Serializable {
    private final long size;
    private final boolean acceptLarger;

    public SizeFileFilter(long size) {
        this(size, true);
    }

    public SizeFileFilter(long size, boolean acceptLarger) {
        if (size < 0L) {
            throw new IllegalArgumentException("The size must be non-negative");
        }
        this.size = size;
        this.acceptLarger = acceptLarger;
    }

    @Override
    public boolean accept(File file) {
        boolean smaller;
        boolean bl = smaller = file.length() < this.size;
        return this.acceptLarger ? !smaller : smaller;
    }

    @Override
    public String toString() {
        String condition = this.acceptLarger ? ">=" : "<";
        return super.toString() + "(" + condition + this.size + ")";
    }
}

