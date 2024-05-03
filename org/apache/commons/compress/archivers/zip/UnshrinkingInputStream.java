/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.archivers.zip;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import org.apache.commons.compress.compressors.lzw.LZWInputStream;

class UnshrinkingInputStream
extends LZWInputStream {
    private static final int MAX_CODE_SIZE = 13;
    private static final int MAX_TABLE_SIZE = 8192;
    private final boolean[] isUsed;

    public UnshrinkingInputStream(InputStream inputStream) throws IOException {
        super(inputStream, ByteOrder.LITTLE_ENDIAN);
        this.setClearCode(9);
        this.initializeTables(13);
        this.isUsed = new boolean[this.getPrefixesLength()];
        for (int i = 0; i < 256; ++i) {
            this.isUsed[i] = true;
        }
        this.setTableSize(this.getClearCode() + 1);
    }

    @Override
    protected int addEntry(int previousCode, byte character) throws IOException {
        int tableSize;
        for (tableSize = this.getTableSize(); tableSize < 8192 && this.isUsed[tableSize]; ++tableSize) {
        }
        this.setTableSize(tableSize);
        int idx = this.addEntry(previousCode, character, 8192);
        if (idx >= 0) {
            this.isUsed[idx] = true;
        }
        return idx;
    }

    private void partialClear() {
        int i;
        boolean[] isParent = new boolean[8192];
        for (i = 0; i < this.isUsed.length; ++i) {
            if (!this.isUsed[i] || this.getPrefix(i) == -1) continue;
            isParent[this.getPrefix((int)i)] = true;
        }
        for (i = this.getClearCode() + 1; i < isParent.length; ++i) {
            if (isParent[i]) continue;
            this.isUsed[i] = false;
            this.setPrefix(i, -1);
        }
    }

    @Override
    protected int decompressNextSymbol() throws IOException {
        int code = this.readNextCode();
        if (code < 0) {
            return -1;
        }
        if (code != this.getClearCode()) {
            boolean addedUnfinishedEntry = false;
            int effectiveCode = code;
            if (!this.isUsed[code]) {
                effectiveCode = this.addRepeatOfPreviousCode();
                addedUnfinishedEntry = true;
            }
            return this.expandCodeToOutputStack(effectiveCode, addedUnfinishedEntry);
        }
        int subCode = this.readNextCode();
        if (subCode < 0) {
            throw new IOException("Unexpected EOF;");
        }
        if (subCode == 1) {
            if (this.getCodeSize() >= 13) {
                throw new IOException("Attempt to increase code size beyond maximum");
            }
            this.incrementCodeSize();
        } else if (subCode == 2) {
            this.partialClear();
            this.setTableSize(this.getClearCode() + 1);
        } else {
            throw new IOException("Invalid clear code subcode " + subCode);
        }
        return 0;
    }
}

