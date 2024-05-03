/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.ibm.icu.impl;

import com.ibm.icu.util.VersionInfo;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class ICUBinary {
    private static final byte MAGIC1 = -38;
    private static final byte MAGIC2 = 39;
    private static final byte BIG_ENDIAN_ = 1;
    private static final byte CHAR_SET_ = 0;
    private static final byte CHAR_SIZE_ = 2;
    private static final String MAGIC_NUMBER_AUTHENTICATION_FAILED_ = "ICU data file error: Not an ICU data file";
    private static final String HEADER_AUTHENTICATION_FAILED_ = "ICU data file error: Header authentication failed, please check if you have a valid ICU data file";

    public static final byte[] readHeader(InputStream inputStream, byte[] dataFormatIDExpected, Authenticate authenticate) throws IOException {
        DataInputStream input = new DataInputStream(inputStream);
        char headersize = input.readChar();
        char readcount = '\u0002';
        byte magic1 = input.readByte();
        ++readcount;
        byte magic2 = input.readByte();
        ++readcount;
        if (magic1 != -38 || magic2 != 39) {
            throw new IOException(MAGIC_NUMBER_AUTHENTICATION_FAILED_);
        }
        input.readChar();
        readcount += 2;
        input.readChar();
        readcount += 2;
        byte bigendian = input.readByte();
        ++readcount;
        byte charset = input.readByte();
        ++readcount;
        byte charsize = input.readByte();
        ++readcount;
        input.readByte();
        ++readcount;
        byte[] dataFormatID = new byte[4];
        input.readFully(dataFormatID);
        readcount += 4;
        byte[] dataVersion = new byte[4];
        input.readFully(dataVersion);
        readcount += 4;
        byte[] unicodeVersion = new byte[4];
        input.readFully(unicodeVersion);
        if (headersize < (readcount += 4)) {
            throw new IOException("Internal Error: Header size error");
        }
        input.skipBytes(headersize - readcount);
        if (bigendian != 1 || charset != 0 || charsize != 2 || !Arrays.equals(dataFormatIDExpected, dataFormatID) || authenticate != null && !authenticate.isDataVersionAcceptable(dataVersion)) {
            throw new IOException(HEADER_AUTHENTICATION_FAILED_);
        }
        return unicodeVersion;
    }

    public static final VersionInfo readHeaderAndDataVersion(InputStream inputStream, byte[] dataFormatIDExpected, Authenticate authenticate) throws IOException {
        byte[] dataVersion = ICUBinary.readHeader(inputStream, dataFormatIDExpected, authenticate);
        return VersionInfo.getInstance(dataVersion[0], dataVersion[1], dataVersion[2], dataVersion[3]);
    }

    public static interface Authenticate {
        public boolean isDataVersionAcceptable(byte[] var1);
    }
}

