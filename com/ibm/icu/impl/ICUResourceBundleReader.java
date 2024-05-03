/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.impl;

import com.ibm.icu.impl.ICUBinary;
import com.ibm.icu.impl.ICUData;
import com.ibm.icu.impl.SoftCache;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.VersionInfo;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class ICUResourceBundleReader
implements ICUBinary.Authenticate {
    private static final byte[] DATA_FORMAT_ID = new byte[]{82, 101, 115, 66};
    private static final int URES_INDEX_LENGTH = 0;
    private static final int URES_INDEX_KEYS_TOP = 1;
    private static final int URES_INDEX_BUNDLE_TOP = 3;
    private static final int URES_INDEX_ATTRIBUTES = 5;
    private static final int URES_INDEX_16BIT_TOP = 6;
    private static final int URES_INDEX_POOL_CHECKSUM = 7;
    private static final int URES_ATT_NO_FALLBACK = 1;
    private static final int URES_ATT_IS_POOL_BUNDLE = 2;
    private static final int URES_ATT_USES_POOL_BUNDLE = 4;
    private static final boolean DEBUG = false;
    private byte[] dataVersion;
    private String s16BitUnits;
    private byte[] poolBundleKeys;
    private String poolBundleKeysAsString;
    private int rootRes;
    private int localKeyLimit;
    private boolean noFallback;
    private boolean isPoolBundle;
    private boolean usesPoolBundle;
    private int[] indexes;
    private byte[] keyStrings;
    private String keyStringsAsString;
    private byte[] resourceBytes;
    private int resourceBottom;
    private static ReaderCache CACHE = new ReaderCache();
    private static final ICUResourceBundleReader NULL_READER = new ICUResourceBundleReader();
    private static byte[] emptyBytes = new byte[0];
    private static ByteBuffer emptyByteBuffer = ByteBuffer.allocate(0).asReadOnlyBuffer();
    private static char[] emptyChars = new char[0];
    private static int[] emptyInts = new int[0];
    private static String emptyString = "";
    private static final String ICU_RESOURCE_SUFFIX = ".res";

    private ICUResourceBundleReader() {
    }

    private ICUResourceBundleReader(InputStream stream, String baseName, String localeID, ClassLoader loader) {
        BufferedInputStream bs = new BufferedInputStream(stream);
        try {
            this.dataVersion = ICUBinary.readHeader(bs, DATA_FORMAT_ID, this);
            this.readData(bs);
            stream.close();
        } catch (IOException ex) {
            String fullName = ICUResourceBundleReader.getFullName(baseName, localeID);
            throw new RuntimeException("Data file " + fullName + " is corrupt - " + ex.getMessage());
        }
        if (this.usesPoolBundle) {
            ICUResourceBundleReader poolBundleReader = ICUResourceBundleReader.getReader(baseName, "pool", loader);
            if (!poolBundleReader.isPoolBundle) {
                throw new IllegalStateException("pool.res is not a pool bundle");
            }
            if (poolBundleReader.indexes[7] != this.indexes[7]) {
                throw new IllegalStateException("pool.res has a different checksum than this bundle");
            }
            this.poolBundleKeys = poolBundleReader.keyStrings;
            this.poolBundleKeysAsString = poolBundleReader.keyStringsAsString;
        }
    }

    static ICUResourceBundleReader getReader(String baseName, String localeID, ClassLoader root) {
        ReaderInfo info = new ReaderInfo(baseName, localeID, root);
        ICUResourceBundleReader reader = (ICUResourceBundleReader)CACHE.getInstance(info, info);
        if (reader == NULL_READER) {
            return null;
        }
        return reader;
    }

    private void readData(InputStream stream) throws IOException {
        DataInputStream ds = new DataInputStream(stream);
        this.rootRes = ds.readInt();
        int indexes0 = ds.readInt();
        int indexLength = indexes0 & 0xFF;
        this.indexes = new int[indexLength];
        this.indexes[0] = indexes0;
        for (int i = 1; i < indexLength; ++i) {
            this.indexes[i] = ds.readInt();
        }
        this.resourceBottom = 1 + indexLength << 2;
        if (indexLength > 5) {
            int att = this.indexes[5];
            this.noFallback = (att & 1) != 0;
            this.isPoolBundle = (att & 2) != 0;
            this.usesPoolBundle = (att & 4) != 0;
        }
        int length = this.indexes[3] * 4;
        if (this.indexes[1] > 1 + indexLength) {
            int keysTop;
            int keysBottom = 1 + indexLength << 2;
            this.resourceBottom = keysTop = this.indexes[1] << 2;
            if (this.isPoolBundle) {
                keysTop -= keysBottom;
                keysBottom = 0;
            } else {
                this.localKeyLimit = keysTop;
            }
            this.keyStrings = new byte[keysTop];
            ds.readFully(this.keyStrings, keysBottom, keysTop - keysBottom);
            if (this.isPoolBundle) {
                while (keysBottom < keysTop && this.keyStrings[keysTop - 1] == -86) {
                    this.keyStrings[--keysTop] = 0;
                }
                this.keyStringsAsString = new String(this.keyStrings, "US-ASCII");
            }
        }
        if (indexLength > 6 && this.indexes[6] > this.indexes[1]) {
            int num16BitUnits = (this.indexes[6] - this.indexes[1]) * 2;
            char[] c16BitUnits = new char[num16BitUnits];
            byte[] c16BitUnitsBytes = new byte[num16BitUnits * 2];
            ds.readFully(c16BitUnitsBytes);
            for (int i = 0; i < num16BitUnits; ++i) {
                c16BitUnits[i] = (char)(c16BitUnitsBytes[i * 2] << 8 | c16BitUnitsBytes[i * 2 + 1] & 0xFF);
            }
            this.s16BitUnits = new String(c16BitUnits);
            this.resourceBottom = this.indexes[6] << 2;
        } else {
            this.s16BitUnits = "\u0000";
        }
        this.resourceBytes = new byte[length - this.resourceBottom];
        ds.readFully(this.resourceBytes);
    }

    VersionInfo getVersion() {
        return VersionInfo.getInstance(this.dataVersion[0], this.dataVersion[1], this.dataVersion[2], this.dataVersion[3]);
    }

    public boolean isDataVersionAcceptable(byte[] version) {
        return version[0] == 1 && version[1] >= 1 || version[0] == 2;
    }

    int getRootResource() {
        return this.rootRes;
    }

    boolean getNoFallback() {
        return this.noFallback;
    }

    boolean getUsesPoolBundle() {
        return this.usesPoolBundle;
    }

    static int RES_GET_TYPE(int res) {
        return res >>> 28;
    }

    private static int RES_GET_OFFSET(int res) {
        return res & 0xFFFFFFF;
    }

    private int getResourceByteOffset(int offset) {
        return (offset << 2) - this.resourceBottom;
    }

    static int RES_GET_INT(int res) {
        return res << 4 >> 4;
    }

    static int RES_GET_UINT(int res) {
        return res & 0xFFFFFFF;
    }

    static boolean URES_IS_TABLE(int type) {
        return type == 2 || type == 5 || type == 4;
    }

    private char getChar(int offset) {
        return (char)(this.resourceBytes[offset] << 8 | this.resourceBytes[offset + 1] & 0xFF);
    }

    private char[] getChars(int offset, int count) {
        char[] chars = new char[count];
        for (int i = 0; i < count; ++i) {
            chars[i] = (char)(this.resourceBytes[offset] << 8 | this.resourceBytes[offset + 1] & 0xFF);
            offset += 2;
        }
        return chars;
    }

    private int getInt(int offset) {
        return this.resourceBytes[offset] << 24 | (this.resourceBytes[offset + 1] & 0xFF) << 16 | (this.resourceBytes[offset + 2] & 0xFF) << 8 | this.resourceBytes[offset + 3] & 0xFF;
    }

    private int[] getInts(int offset, int count) {
        int[] ints = new int[count];
        for (int i = 0; i < count; ++i) {
            ints[i] = this.resourceBytes[offset] << 24 | (this.resourceBytes[offset + 1] & 0xFF) << 16 | (this.resourceBytes[offset + 2] & 0xFF) << 8 | this.resourceBytes[offset + 3] & 0xFF;
            offset += 4;
        }
        return ints;
    }

    private char[] getTable16KeyOffsets(int offset) {
        char length;
        if ((length = this.s16BitUnits.charAt(offset++)) > '\u0000') {
            return this.s16BitUnits.substring(offset, offset + length).toCharArray();
        }
        return emptyChars;
    }

    private char[] getTableKeyOffsets(int offset) {
        char length = this.getChar(offset);
        if (length > '\u0000') {
            return this.getChars(offset + 2, length);
        }
        return emptyChars;
    }

    private int[] getTable32KeyOffsets(int offset) {
        int length = this.getInt(offset);
        if (length > 0) {
            return this.getInts(offset + 4, length);
        }
        return emptyInts;
    }

    private String makeKeyStringFromBytes(int keyOffset) {
        byte b;
        StringBuilder sb = new StringBuilder();
        while ((b = this.keyStrings[keyOffset++]) != 0) {
            sb.append((char)b);
        }
        return sb.toString();
    }

    private String makeKeyStringFromString(int keyOffset) {
        int endOffset = keyOffset;
        while (this.poolBundleKeysAsString.charAt(endOffset) != '\u0000') {
            ++endOffset;
        }
        return this.poolBundleKeysAsString.substring(keyOffset, endOffset);
    }

    private ByteSequence RES_GET_KEY16(char keyOffset) {
        if (keyOffset < this.localKeyLimit) {
            return new ByteSequence(this.keyStrings, keyOffset);
        }
        return new ByteSequence(this.poolBundleKeys, keyOffset - this.localKeyLimit);
    }

    private String getKey16String(int keyOffset) {
        if (keyOffset < this.localKeyLimit) {
            return this.makeKeyStringFromBytes(keyOffset);
        }
        return this.makeKeyStringFromString(keyOffset - this.localKeyLimit);
    }

    private ByteSequence RES_GET_KEY32(int keyOffset) {
        if (keyOffset >= 0) {
            return new ByteSequence(this.keyStrings, keyOffset);
        }
        return new ByteSequence(this.poolBundleKeys, keyOffset & Integer.MAX_VALUE);
    }

    private String getKey32String(int keyOffset) {
        if (keyOffset >= 0) {
            return this.makeKeyStringFromBytes(keyOffset);
        }
        return this.makeKeyStringFromString(keyOffset & Integer.MAX_VALUE);
    }

    private static int compareKeys(CharSequence key, ByteSequence tableKey) {
        int i;
        for (i = 0; i < key.length(); ++i) {
            byte c2 = tableKey.charAt(i);
            if (c2 == 0) {
                return 1;
            }
            int diff = key.charAt(i) - c2;
            if (diff == 0) continue;
            return diff;
        }
        return -tableKey.charAt(i);
    }

    private int compareKeys(CharSequence key, char keyOffset) {
        return ICUResourceBundleReader.compareKeys(key, this.RES_GET_KEY16(keyOffset));
    }

    private int compareKeys32(CharSequence key, int keyOffset) {
        return ICUResourceBundleReader.compareKeys(key, this.RES_GET_KEY32(keyOffset));
    }

    String getString(int res) {
        int offset = ICUResourceBundleReader.RES_GET_OFFSET(res);
        if (ICUResourceBundleReader.RES_GET_TYPE(res) == 6) {
            int length;
            char first = this.s16BitUnits.charAt(offset);
            if ((first & 0xFFFFFC00) != 56320) {
                if (first == '\u0000') {
                    return emptyString;
                }
                int endOffset = offset + 1;
                while (this.s16BitUnits.charAt(endOffset) != '\u0000') {
                    ++endOffset;
                }
                return this.s16BitUnits.substring(offset, endOffset);
            }
            if (first < '\udfef') {
                length = first & 0x3FF;
                ++offset;
            } else if (first < '\udfff') {
                length = first - 57327 << 16 | this.s16BitUnits.charAt(offset + 1);
                offset += 2;
            } else {
                length = this.s16BitUnits.charAt(offset + 1) << 16 | this.s16BitUnits.charAt(offset + 2);
                offset += 3;
            }
            return this.s16BitUnits.substring(offset, offset + length);
        }
        if (res == offset) {
            if (res == 0) {
                return emptyString;
            }
            offset = this.getResourceByteOffset(offset);
            int length = this.getInt(offset);
            return new String(this.getChars(offset + 4, length));
        }
        return null;
    }

    String getAlias(int res) {
        int offset = ICUResourceBundleReader.RES_GET_OFFSET(res);
        if (ICUResourceBundleReader.RES_GET_TYPE(res) == 3) {
            if (offset == 0) {
                return emptyString;
            }
            offset = this.getResourceByteOffset(offset);
            int length = this.getInt(offset);
            return new String(this.getChars(offset + 4, length));
        }
        return null;
    }

    byte[] getBinary(int res, byte[] ba) {
        int offset = ICUResourceBundleReader.RES_GET_OFFSET(res);
        if (ICUResourceBundleReader.RES_GET_TYPE(res) == 1) {
            if (offset == 0) {
                return emptyBytes;
            }
            offset = this.getResourceByteOffset(offset);
            int length = this.getInt(offset);
            if (ba == null || ba.length != length) {
                ba = new byte[length];
            }
            System.arraycopy(this.resourceBytes, offset + 4, ba, 0, length);
            return ba;
        }
        return null;
    }

    ByteBuffer getBinary(int res) {
        int offset = ICUResourceBundleReader.RES_GET_OFFSET(res);
        if (ICUResourceBundleReader.RES_GET_TYPE(res) == 1) {
            if (offset == 0) {
                return emptyByteBuffer.duplicate();
            }
            offset = this.getResourceByteOffset(offset);
            int length = this.getInt(offset);
            return ByteBuffer.wrap(this.resourceBytes, offset + 4, length).slice().asReadOnlyBuffer();
        }
        return null;
    }

    int[] getIntVector(int res) {
        int offset = ICUResourceBundleReader.RES_GET_OFFSET(res);
        if (ICUResourceBundleReader.RES_GET_TYPE(res) == 14) {
            if (offset == 0) {
                return emptyInts;
            }
            offset = this.getResourceByteOffset(offset);
            int length = this.getInt(offset);
            return this.getInts(offset + 4, length);
        }
        return null;
    }

    Container getArray(int res) {
        int type = ICUResourceBundleReader.RES_GET_TYPE(res);
        int offset = ICUResourceBundleReader.RES_GET_OFFSET(res);
        switch (type) {
            case 8: 
            case 9: {
                if (offset != 0) break;
                return new Container(this);
            }
            default: {
                return null;
            }
        }
        switch (type) {
            case 8: {
                return new Array(this, offset);
            }
            case 9: {
                return new Array16(this, offset);
            }
        }
        return null;
    }

    Table getTable(int res) {
        int type = ICUResourceBundleReader.RES_GET_TYPE(res);
        int offset = ICUResourceBundleReader.RES_GET_OFFSET(res);
        switch (type) {
            case 2: 
            case 4: 
            case 5: {
                if (offset != 0) break;
                return new Table(this);
            }
            default: {
                return null;
            }
        }
        switch (type) {
            case 2: {
                return new Table1632(this, offset);
            }
            case 5: {
                return new Table16(this, offset);
            }
            case 4: {
                return new Table32(this, offset);
            }
        }
        return null;
    }

    public static String getFullName(String baseName, String localeName) {
        if (baseName == null || baseName.length() == 0) {
            if (localeName.length() == 0) {
                localeName = ULocale.getDefault().toString();
                return localeName;
            }
            return localeName + ICU_RESOURCE_SUFFIX;
        }
        if (baseName.indexOf(46) == -1) {
            if (baseName.charAt(baseName.length() - 1) != '/') {
                return baseName + "/" + localeName + ICU_RESOURCE_SUFFIX;
            }
            return baseName + localeName + ICU_RESOURCE_SUFFIX;
        }
        baseName = baseName.replace('.', '/');
        if (localeName.length() == 0) {
            return baseName + ICU_RESOURCE_SUFFIX;
        }
        return baseName + "_" + localeName + ICU_RESOURCE_SUFFIX;
    }

    private static final class Table32
    extends Table {
        int getContainerResource(int index) {
            return this.getContainer32Resource(index);
        }

        Table32(ICUResourceBundleReader reader, int offset) {
            super(reader);
            offset = reader.getResourceByteOffset(offset);
            this.key32Offsets = reader.getTable32KeyOffsets(offset);
            this.size = this.key32Offsets.length;
            this.itemsOffset = offset + 4 * (1 + this.size);
        }
    }

    private static final class Table16
    extends Table {
        int getContainerResource(int index) {
            return this.getContainer16Resource(index);
        }

        Table16(ICUResourceBundleReader reader, int offset) {
            super(reader);
            this.keyOffsets = reader.getTable16KeyOffsets(offset);
            this.size = this.keyOffsets.length;
            this.itemsOffset = offset + 1 + this.size;
        }
    }

    private static final class Table1632
    extends Table {
        int getContainerResource(int index) {
            return this.getContainer32Resource(index);
        }

        Table1632(ICUResourceBundleReader reader, int offset) {
            super(reader);
            offset = reader.getResourceByteOffset(offset);
            this.keyOffsets = reader.getTableKeyOffsets(offset);
            this.size = this.keyOffsets.length;
            this.itemsOffset = offset + 2 * (this.size + 2 & 0xFFFFFFFE);
        }
    }

    static class Table
    extends Container {
        protected char[] keyOffsets;
        protected int[] key32Offsets;
        private static final int URESDATA_ITEM_NOT_FOUND = -1;

        String getKey(int index) {
            if (index < 0 || this.size <= index) {
                return null;
            }
            return this.keyOffsets != null ? this.reader.getKey16String(this.keyOffsets[index]) : this.reader.getKey32String(this.key32Offsets[index]);
        }

        int findTableItem(CharSequence key) {
            int start = 0;
            int limit = this.size;
            while (start < limit) {
                int mid = start + limit >>> 1;
                int result = this.keyOffsets != null ? this.reader.compareKeys(key, this.keyOffsets[mid]) : this.reader.compareKeys32(key, this.key32Offsets[mid]);
                if (result < 0) {
                    limit = mid;
                    continue;
                }
                if (result > 0) {
                    start = mid + 1;
                    continue;
                }
                return mid;
            }
            return -1;
        }

        int getTableResource(String resKey) {
            return this.getContainerResource(this.findTableItem(resKey));
        }

        Table(ICUResourceBundleReader reader) {
            super(reader);
        }
    }

    private static final class Array16
    extends Container {
        int getContainerResource(int index) {
            return this.getContainer16Resource(index);
        }

        Array16(ICUResourceBundleReader reader, int offset) {
            super(reader);
            this.size = reader.s16BitUnits.charAt(offset);
            this.itemsOffset = offset + 1;
        }
    }

    private static final class Array
    extends Container {
        int getContainerResource(int index) {
            return this.getContainer32Resource(index);
        }

        Array(ICUResourceBundleReader reader, int offset) {
            super(reader);
            offset = reader.getResourceByteOffset(offset);
            this.size = reader.getInt(offset);
            this.itemsOffset = offset + 4;
        }
    }

    static class Container {
        protected ICUResourceBundleReader reader;
        protected int size;
        protected int itemsOffset;

        int getSize() {
            return this.size;
        }

        int getContainerResource(int index) {
            return -1;
        }

        protected int getContainer16Resource(int index) {
            if (index < 0 || this.size <= index) {
                return -1;
            }
            return 0x60000000 | this.reader.s16BitUnits.charAt(this.itemsOffset + index);
        }

        protected int getContainer32Resource(int index) {
            if (index < 0 || this.size <= index) {
                return -1;
            }
            return this.reader.getInt(this.itemsOffset + 4 * index);
        }

        Container(ICUResourceBundleReader reader) {
            this.reader = reader;
        }
    }

    private static final class ByteSequence {
        private byte[] bytes;
        private int offset;

        public ByteSequence(byte[] bytes, int offset) {
            this.bytes = bytes;
            this.offset = offset;
        }

        public byte charAt(int index) {
            return this.bytes[this.offset + index];
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    private static class ReaderCache
    extends SoftCache<ReaderInfo, ICUResourceBundleReader, ReaderInfo> {
        private ReaderCache() {
        }

        @Override
        protected ICUResourceBundleReader createInstance(ReaderInfo key, ReaderInfo data) {
            String fullName = ICUResourceBundleReader.getFullName(data.baseName, data.localeID);
            InputStream stream = ICUData.getStream(data.loader, fullName);
            if (stream == null) {
                return NULL_READER;
            }
            return new ICUResourceBundleReader(stream, data.baseName, data.localeID, data.loader);
        }
    }

    private static class ReaderInfo {
        final String baseName;
        final String localeID;
        final ClassLoader loader;

        ReaderInfo(String baseName, String localeID, ClassLoader loader) {
            this.baseName = baseName == null ? "" : baseName;
            this.localeID = localeID == null ? "" : localeID;
            this.loader = loader;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ReaderInfo)) {
                return false;
            }
            ReaderInfo info = (ReaderInfo)obj;
            return this.baseName.equals(info.baseName) && this.localeID.equals(info.localeID) && this.loader.equals(info.loader);
        }

        public int hashCode() {
            return this.baseName.hashCode() ^ this.localeID.hashCode() ^ this.loader.hashCode();
        }
    }
}

