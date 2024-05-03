/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import org.apache.commons.io.comparator.AbstractFileComparator;

public class CompositeFileComparator
extends AbstractFileComparator
implements Serializable {
    private static final Comparator<?>[] NO_COMPARATORS = new Comparator[0];
    private final Comparator<File>[] delegates;

    public CompositeFileComparator(Comparator<File> ... delegates) {
        if (delegates == null) {
            this.delegates = NO_COMPARATORS;
        } else {
            this.delegates = new Comparator[delegates.length];
            System.arraycopy(delegates, 0, this.delegates, 0, delegates.length);
        }
    }

    public CompositeFileComparator(Iterable<Comparator<File>> delegates) {
        if (delegates == null) {
            this.delegates = NO_COMPARATORS;
        } else {
            ArrayList<Comparator<File>> list = new ArrayList<Comparator<File>>();
            for (Comparator<File> comparator : delegates) {
                list.add(comparator);
            }
            this.delegates = list.toArray(new Comparator[list.size()]);
        }
    }

    @Override
    public int compare(File file1, File file2) {
        Comparator<File> delegate;
        int result = 0;
        Comparator<File>[] arr$ = this.delegates;
        int len$ = arr$.length;
        for (int i$ = 0; i$ < len$ && (result = (delegate = arr$[i$]).compare(file1, file2)) == 0; ++i$) {
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append('{');
        for (int i = 0; i < this.delegates.length; ++i) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(this.delegates[i]);
        }
        builder.append('}');
        return builder.toString();
    }
}

