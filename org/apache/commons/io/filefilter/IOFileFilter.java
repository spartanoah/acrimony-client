/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

public interface IOFileFilter
extends FileFilter,
FilenameFilter {
    @Override
    public boolean accept(File var1);

    @Override
    public boolean accept(File var1, String var2);
}

