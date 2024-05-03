/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.text;

import com.ibm.icu.impl.CharTrie;
import com.ibm.icu.impl.ICUData;
import com.ibm.icu.impl.StringPrepDataReader;
import com.ibm.icu.impl.UBiDiProps;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.Normalizer;
import com.ibm.icu.text.StringPrepParseException;
import com.ibm.icu.text.UCharacterIterator;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.util.VersionInfo;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public final class StringPrep {
    public static final int DEFAULT = 0;
    public static final int ALLOW_UNASSIGNED = 1;
    public static final int RFC3491_NAMEPREP = 0;
    public static final int RFC3530_NFS4_CS_PREP = 1;
    public static final int RFC3530_NFS4_CS_PREP_CI = 2;
    public static final int RFC3530_NFS4_CIS_PREP = 3;
    public static final int RFC3530_NFS4_MIXED_PREP_PREFIX = 4;
    public static final int RFC3530_NFS4_MIXED_PREP_SUFFIX = 5;
    public static final int RFC3722_ISCSI = 6;
    public static final int RFC3920_NODEPREP = 7;
    public static final int RFC3920_RESOURCEPREP = 8;
    public static final int RFC4011_MIB = 9;
    public static final int RFC4013_SASLPREP = 10;
    public static final int RFC4505_TRACE = 11;
    public static final int RFC4518_LDAP = 12;
    public static final int RFC4518_LDAP_CI = 13;
    private static final int MAX_PROFILE = 13;
    private static final String[] PROFILE_NAMES = new String[]{"rfc3491", "rfc3530cs", "rfc3530csci", "rfc3491", "rfc3530mixp", "rfc3491", "rfc3722", "rfc3920node", "rfc3920res", "rfc4011", "rfc4013", "rfc4505", "rfc4518", "rfc4518ci"};
    private static final WeakReference<StringPrep>[] CACHE = new WeakReference[14];
    private static final int UNASSIGNED = 0;
    private static final int MAP = 1;
    private static final int PROHIBITED = 2;
    private static final int DELETE = 3;
    private static final int TYPE_LIMIT = 4;
    private static final int NORMALIZATION_ON = 1;
    private static final int CHECK_BIDI_ON = 2;
    private static final int TYPE_THRESHOLD = 65520;
    private static final int MAX_INDEX_VALUE = 16319;
    private static final int INDEX_TRIE_SIZE = 0;
    private static final int INDEX_MAPPING_DATA_SIZE = 1;
    private static final int NORM_CORRECTNS_LAST_UNI_VERSION = 2;
    private static final int ONE_UCHAR_MAPPING_INDEX_START = 3;
    private static final int TWO_UCHARS_MAPPING_INDEX_START = 4;
    private static final int THREE_UCHARS_MAPPING_INDEX_START = 5;
    private static final int FOUR_UCHARS_MAPPING_INDEX_START = 6;
    private static final int OPTIONS = 7;
    private static final int INDEX_TOP = 16;
    private static final int DATA_BUFFER_SIZE = 25000;
    private CharTrie sprepTrie;
    private int[] indexes;
    private char[] mappingData;
    private VersionInfo sprepUniVer;
    private VersionInfo normCorrVer;
    private boolean doNFKC;
    private boolean checkBiDi;
    private UBiDiProps bdp;

    private char getCodePointValue(int ch) {
        return this.sprepTrie.getCodePointValue(ch);
    }

    private static VersionInfo getVersionInfo(int comp) {
        int micro = comp & 0xFF;
        int milli = comp >> 8 & 0xFF;
        int minor = comp >> 16 & 0xFF;
        int major = comp >> 24 & 0xFF;
        return VersionInfo.getInstance(major, minor, milli, micro);
    }

    private static VersionInfo getVersionInfo(byte[] version) {
        if (version.length != 4) {
            return null;
        }
        return VersionInfo.getInstance(version[0], version[1], version[2], version[3]);
    }

    public StringPrep(InputStream inputStream) throws IOException {
        BufferedInputStream b = new BufferedInputStream(inputStream, 25000);
        StringPrepDataReader reader = new StringPrepDataReader(b);
        this.indexes = reader.readIndexes(16);
        byte[] sprepBytes = new byte[this.indexes[0]];
        this.mappingData = new char[this.indexes[1] / 2];
        reader.read(sprepBytes, this.mappingData);
        this.sprepTrie = new CharTrie(new ByteArrayInputStream(sprepBytes), null);
        reader.getDataFormatVersion();
        this.doNFKC = (this.indexes[7] & 1) > 0;
        this.checkBiDi = (this.indexes[7] & 2) > 0;
        this.sprepUniVer = StringPrep.getVersionInfo(reader.getUnicodeVersion());
        this.normCorrVer = StringPrep.getVersionInfo(this.indexes[2]);
        VersionInfo normUniVer = UCharacter.getUnicodeVersion();
        if (normUniVer.compareTo(this.sprepUniVer) < 0 && normUniVer.compareTo(this.normCorrVer) < 0 && (this.indexes[7] & 1) > 0) {
            throw new IOException("Normalization Correction version not supported");
        }
        b.close();
        if (this.checkBiDi) {
            this.bdp = UBiDiProps.INSTANCE;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static StringPrep getInstance(int profile) {
        if (profile < 0 || profile > 13) {
            throw new IllegalArgumentException("Bad profile type");
        }
        StringPrep instance = null;
        WeakReference<StringPrep>[] weakReferenceArray = CACHE;
        synchronized (CACHE) {
            WeakReference<StringPrep> ref = CACHE[profile];
            if (ref != null) {
                instance = (StringPrep)ref.get();
            }
            if (instance == null) {
                InputStream stream = ICUData.getRequiredStream("data/icudt51b/" + PROFILE_NAMES[profile] + ".spp");
                if (stream != null) {
                    try {
                        try {
                            instance = new StringPrep(stream);
                        } finally {
                            stream.close();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e.toString());
                    }
                }
                if (instance != null) {
                    StringPrep.CACHE[profile] = new WeakReference<StringPrep>(instance);
                }
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return instance;
        }
    }

    private static final void getValues(char trieWord, Values values) {
        values.reset();
        if (trieWord == '\u0000') {
            values.type = 4;
        } else if (trieWord >= '\ufff0') {
            values.type = trieWord - 65520;
        } else {
            values.type = 1;
            if ((trieWord & 2) > 0) {
                values.isIndex = true;
                values.value = trieWord >> 2;
            } else {
                values.isIndex = false;
                values.value = trieWord << 16 >> 16;
                values.value >>= 2;
            }
            if (trieWord >> 2 == 16319) {
                values.type = 3;
                values.isIndex = false;
                values.value = 0;
            }
        }
    }

    private StringBuffer map(UCharacterIterator iter, int options) throws StringPrepParseException {
        boolean allowUnassigned;
        Values val2 = new Values();
        char result = '\u0000';
        int ch = -1;
        StringBuffer dest = new StringBuffer();
        boolean bl = allowUnassigned = (options & 1) > 0;
        while ((ch = iter.nextCodePoint()) != -1) {
            result = this.getCodePointValue(ch);
            StringPrep.getValues(result, val2);
            if (val2.type == 0 && !allowUnassigned) {
                throw new StringPrepParseException("An unassigned code point was found in the input", 3, iter.getText(), iter.getIndex());
            }
            if (val2.type == 1) {
                if (val2.isIndex) {
                    int index = val2.value;
                    int length = index >= this.indexes[3] && index < this.indexes[4] ? 1 : (index >= this.indexes[4] && index < this.indexes[5] ? 2 : (index >= this.indexes[5] && index < this.indexes[6] ? 3 : this.mappingData[index++]));
                    dest.append(this.mappingData, index, length);
                    continue;
                }
                ch -= val2.value;
            } else if (val2.type == 3) continue;
            UTF16.append(dest, ch);
        }
        return dest;
    }

    private StringBuffer normalize(StringBuffer src) {
        return new StringBuffer(Normalizer.normalize(src.toString(), Normalizer.NFKC, 32));
    }

    public StringBuffer prepare(UCharacterIterator src, int options) throws StringPrepParseException {
        int ch;
        StringBuffer mapOut;
        StringBuffer normOut = mapOut = this.map(src, options);
        if (this.doNFKC) {
            normOut = this.normalize(mapOut);
        }
        UCharacterIterator iter = UCharacterIterator.getInstance(normOut);
        Values val2 = new Values();
        int direction = 19;
        int firstCharDir = 19;
        int rtlPos = -1;
        int ltrPos = -1;
        boolean rightToLeft = false;
        boolean leftToRight = false;
        while ((ch = iter.nextCodePoint()) != -1) {
            char result = this.getCodePointValue(ch);
            StringPrep.getValues(result, val2);
            if (val2.type == 2) {
                throw new StringPrepParseException("A prohibited code point was found in the input", 2, iter.getText(), val2.value);
            }
            if (!this.checkBiDi) continue;
            direction = this.bdp.getClass(ch);
            if (firstCharDir == 19) {
                firstCharDir = direction;
            }
            if (direction == 0) {
                leftToRight = true;
                ltrPos = iter.getIndex() - 1;
            }
            if (direction != 1 && direction != 13) continue;
            rightToLeft = true;
            rtlPos = iter.getIndex() - 1;
        }
        if (this.checkBiDi) {
            if (leftToRight && rightToLeft) {
                throw new StringPrepParseException("The input does not conform to the rules for BiDi code points.", 4, iter.getText(), rtlPos > ltrPos ? rtlPos : ltrPos);
            }
            if (rightToLeft && (firstCharDir != 1 && firstCharDir != 13 || direction != 1 && direction != 13)) {
                throw new StringPrepParseException("The input does not conform to the rules for BiDi code points.", 4, iter.getText(), rtlPos > ltrPos ? rtlPos : ltrPos);
            }
        }
        return normOut;
    }

    public String prepare(String src, int options) throws StringPrepParseException {
        StringBuffer result = this.prepare(UCharacterIterator.getInstance(src), options);
        return result.toString();
    }

    private static final class Values {
        boolean isIndex;
        int value;
        int type;

        private Values() {
        }

        public void reset() {
            this.isIndex = false;
            this.value = 0;
            this.type = -1;
        }
    }
}

