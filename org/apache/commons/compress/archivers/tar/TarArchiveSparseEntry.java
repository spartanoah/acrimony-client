/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.archivers.tar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.archivers.tar.TarArchiveStructSparse;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.commons.compress.archivers.tar.TarUtils;

public class TarArchiveSparseEntry
implements TarConstants {
    private final boolean isExtended;
    private final List<TarArchiveStructSparse> sparseHeaders;

    public TarArchiveSparseEntry(byte[] headerBuf) throws IOException {
        int offset = 0;
        this.sparseHeaders = new ArrayList<TarArchiveStructSparse>(TarUtils.readSparseStructs(headerBuf, 0, 21));
        this.isExtended = TarUtils.parseBoolean(headerBuf, offset += 504);
    }

    public boolean isExtended() {
        return this.isExtended;
    }

    public List<TarArchiveStructSparse> getSparseHeaders() {
        return this.sparseHeaders;
    }
}

