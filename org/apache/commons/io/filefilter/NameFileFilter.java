/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.AbstractFileFilter;

public class NameFileFilter
extends AbstractFileFilter
implements Serializable {
    private final String[] names;
    private final IOCase caseSensitivity;

    public NameFileFilter(String name) {
        this(name, null);
    }

    public NameFileFilter(String name, IOCase caseSensitivity) {
        if (name == null) {
            throw new IllegalArgumentException("The wildcard must not be null");
        }
        this.names = new String[]{name};
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    public NameFileFilter(String[] names) {
        this(names, null);
    }

    public NameFileFilter(String[] names, IOCase caseSensitivity) {
        if (names == null) {
            throw new IllegalArgumentException("The array of names must not be null");
        }
        this.names = new String[names.length];
        System.arraycopy(names, 0, this.names, 0, names.length);
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    public NameFileFilter(List<String> names) {
        this(names, null);
    }

    public NameFileFilter(List<String> names, IOCase caseSensitivity) {
        if (names == null) {
            throw new IllegalArgumentException("The list of names must not be null");
        }
        this.names = names.toArray(new String[names.size()]);
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }

    @Override
    public boolean accept(File file) {
        String name = file.getName();
        for (String name2 : this.names) {
            if (!this.caseSensitivity.checkEquals(name, name2)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean accept(File dir, String name) {
        for (String name2 : this.names) {
            if (!this.caseSensitivity.checkEquals(name, name2)) continue;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(super.toString());
        buffer.append("(");
        if (this.names != null) {
            for (int i = 0; i < this.names.length; ++i) {
                if (i > 0) {
                    buffer.append(",");
                }
                buffer.append(this.names[i]);
            }
        }
        buffer.append(")");
        return buffer.toString();
    }
}

