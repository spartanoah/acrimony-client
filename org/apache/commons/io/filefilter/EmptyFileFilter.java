/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;

public class EmptyFileFilter
extends AbstractFileFilter
implements Serializable {
    public static final IOFileFilter EMPTY = new EmptyFileFilter();
    public static final IOFileFilter NOT_EMPTY = new NotFileFilter(EMPTY);

    protected EmptyFileFilter() {
    }

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            return files == null || files.length == 0;
        }
        return file.length() == 0L;
    }
}

