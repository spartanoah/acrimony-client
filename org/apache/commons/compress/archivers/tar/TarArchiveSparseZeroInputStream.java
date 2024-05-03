/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.archivers.tar;

import java.io.IOException;
import java.io.InputStream;

class TarArchiveSparseZeroInputStream
extends InputStream {
    TarArchiveSparseZeroInputStream() {
    }

    @Override
    public int read() throws IOException {
        return 0;
    }

    @Override
    public long skip(long n) {
        return n;
    }
}

