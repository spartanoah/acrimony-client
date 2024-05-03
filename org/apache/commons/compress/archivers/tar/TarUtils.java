/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.archivers.tar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.compress.archivers.tar.TarArchiveStructSparse;
import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.apache.commons.compress.utils.IOUtils;

public class TarUtils {
    private static final int BYTE_MASK = 255;
    static final ZipEncoding DEFAULT_ENCODING = ZipEncodingHelper.getZipEncoding(null);
    static final ZipEncoding FALLBACK_ENCODING = new ZipEncoding(){

        @Override
        public boolean canEncode(String name) {
            return true;
        }

        @Override
        public ByteBuffer encode(String name) {
            int length = name.length();
            byte[] buf = new byte[length];
            for (int i = 0; i < length; ++i) {
                buf[i] = (byte)name.charAt(i);
            }
            return ByteBuffer.wrap(buf);
        }

        @Override
        public String decode(byte[] buffer) {
            int length = buffer.length;
            StringBuilder result = new StringBuilder(length);
            for (byte b : buffer) {
                if (b == 0) break;
                result.append((char)(b & 0xFF));
            }
            return result.toString();
        }
    };

    private TarUtils() {
    }

    public static long parseOctal(byte[] buffer, int offset, int length) {
        int start;
        long result = 0L;
        int end = offset + length;
        if (length < 2) {
            throw new IllegalArgumentException("Length " + length + " must be at least 2");
        }
        if (buffer[start] == 0) {
            return 0L;
        }
        for (start = offset; start < end && buffer[start] == 32; ++start) {
        }
        byte trailer = buffer[end - 1];
        while (start < end && (trailer == 0 || trailer == 32)) {
            trailer = buffer[--end - 1];
        }
        while (start < end) {
            byte currentByte = buffer[start];
            if (currentByte < 48 || currentByte > 55) {
                throw new IllegalArgumentException(TarUtils.exceptionMessage(buffer, offset, length, start, currentByte));
            }
            result = (result << 3) + (long)(currentByte - 48);
            ++start;
        }
        return result;
    }

    public static long parseOctalOrBinary(byte[] buffer, int offset, int length) {
        boolean negative;
        if ((buffer[offset] & 0x80) == 0) {
            return TarUtils.parseOctal(buffer, offset, length);
        }
        boolean bl = negative = buffer[offset] == -1;
        if (length < 9) {
            return TarUtils.parseBinaryLong(buffer, offset, length, negative);
        }
        return TarUtils.parseBinaryBigInteger(buffer, offset, length, negative);
    }

    private static long parseBinaryLong(byte[] buffer, int offset, int length, boolean negative) {
        if (length >= 9) {
            throw new IllegalArgumentException("At offset " + offset + ", " + length + " byte binary number exceeds maximum signed long value");
        }
        long val2 = 0L;
        for (int i = 1; i < length; ++i) {
            val2 = (val2 << 8) + (long)(buffer[offset + i] & 0xFF);
        }
        if (negative) {
            --val2;
            val2 ^= (long)Math.pow(2.0, (double)(length - 1) * 8.0) - 1L;
        }
        return negative ? -val2 : val2;
    }

    private static long parseBinaryBigInteger(byte[] buffer, int offset, int length, boolean negative) {
        byte[] remainder = new byte[length - 1];
        System.arraycopy(buffer, offset + 1, remainder, 0, length - 1);
        BigInteger val2 = new BigInteger(remainder);
        if (negative) {
            val2 = val2.add(BigInteger.valueOf(-1L)).not();
        }
        if (val2.bitLength() > 63) {
            throw new IllegalArgumentException("At offset " + offset + ", " + length + " byte binary number exceeds maximum signed long value");
        }
        return negative ? -val2.longValue() : val2.longValue();
    }

    public static boolean parseBoolean(byte[] buffer, int offset) {
        return buffer[offset] == 1;
    }

    private static String exceptionMessage(byte[] buffer, int offset, int length, int current, byte currentByte) {
        String string = new String(buffer, offset, length);
        string = string.replace("\u0000", "{NUL}");
        return "Invalid byte " + currentByte + " at offset " + (current - offset) + " in '" + string + "' len=" + length;
    }

    public static String parseName(byte[] buffer, int offset, int length) {
        try {
            return TarUtils.parseName(buffer, offset, length, DEFAULT_ENCODING);
        } catch (IOException ex) {
            try {
                return TarUtils.parseName(buffer, offset, length, FALLBACK_ENCODING);
            } catch (IOException ex2) {
                throw new RuntimeException(ex2);
            }
        }
    }

    public static String parseName(byte[] buffer, int offset, int length, ZipEncoding encoding) throws IOException {
        int len = 0;
        int i = offset;
        while (len < length && buffer[i] != 0) {
            ++len;
            ++i;
        }
        if (len > 0) {
            byte[] b = new byte[len];
            System.arraycopy(buffer, offset, b, 0, len);
            return encoding.decode(b);
        }
        return "";
    }

    public static TarArchiveStructSparse parseSparse(byte[] buffer, int offset) {
        long sparseOffset = TarUtils.parseOctalOrBinary(buffer, offset, 12);
        long sparseNumbytes = TarUtils.parseOctalOrBinary(buffer, offset + 12, 12);
        return new TarArchiveStructSparse(sparseOffset, sparseNumbytes);
    }

    static List<TarArchiveStructSparse> readSparseStructs(byte[] buffer, int offset, int entries) throws IOException {
        ArrayList<TarArchiveStructSparse> sparseHeaders = new ArrayList<TarArchiveStructSparse>();
        for (int i = 0; i < entries; ++i) {
            try {
                TarArchiveStructSparse sparseHeader = TarUtils.parseSparse(buffer, offset + i * 24);
                if (sparseHeader.getOffset() < 0L) {
                    throw new IOException("Corrupted TAR archive, sparse entry with negative offset");
                }
                if (sparseHeader.getNumbytes() < 0L) {
                    throw new IOException("Corrupted TAR archive, sparse entry with negative numbytes");
                }
                sparseHeaders.add(sparseHeader);
                continue;
            } catch (IllegalArgumentException ex) {
                throw new IOException("Corrupted TAR archive, sparse entry is invalid", ex);
            }
        }
        return Collections.unmodifiableList(sparseHeaders);
    }

    public static int formatNameBytes(String name, byte[] buf, int offset, int length) {
        try {
            return TarUtils.formatNameBytes(name, buf, offset, length, DEFAULT_ENCODING);
        } catch (IOException ex) {
            try {
                return TarUtils.formatNameBytes(name, buf, offset, length, FALLBACK_ENCODING);
            } catch (IOException ex2) {
                throw new RuntimeException(ex2);
            }
        }
    }

    public static int formatNameBytes(String name, byte[] buf, int offset, int length, ZipEncoding encoding) throws IOException {
        int len = name.length();
        ByteBuffer b = encoding.encode(name);
        while (b.limit() > length && len > 0) {
            b = encoding.encode(name.substring(0, --len));
        }
        int limit = b.limit() - b.position();
        System.arraycopy(b.array(), b.arrayOffset(), buf, offset, limit);
        for (int i = limit; i < length; ++i) {
            buf[offset + i] = 0;
        }
        return offset + length;
    }

    public static void formatUnsignedOctalString(long value, byte[] buffer, int offset, int length) {
        int remaining = length;
        --remaining;
        if (value == 0L) {
            buffer[offset + remaining--] = 48;
        } else {
            long val2;
            for (val2 = value; remaining >= 0 && val2 != 0L; val2 >>>= 3, --remaining) {
                buffer[offset + remaining] = (byte)(48 + (byte)(val2 & 7L));
            }
            if (val2 != 0L) {
                throw new IllegalArgumentException(value + "=" + Long.toOctalString(value) + " will not fit in octal number buffer of length " + length);
            }
        }
        while (remaining >= 0) {
            buffer[offset + remaining] = 48;
            --remaining;
        }
    }

    public static int formatOctalBytes(long value, byte[] buf, int offset, int length) {
        int idx = length - 2;
        TarUtils.formatUnsignedOctalString(value, buf, offset, idx);
        buf[offset + idx++] = 32;
        buf[offset + idx] = 0;
        return offset + length;
    }

    public static int formatLongOctalBytes(long value, byte[] buf, int offset, int length) {
        int idx = length - 1;
        TarUtils.formatUnsignedOctalString(value, buf, offset, idx);
        buf[offset + idx] = 32;
        return offset + length;
    }

    public static int formatLongOctalOrBinaryBytes(long value, byte[] buf, int offset, int length) {
        boolean negative;
        long maxAsOctalChar = length == 8 ? 0x1FFFFFL : 0x1FFFFFFFFL;
        boolean bl = negative = value < 0L;
        if (!negative && value <= maxAsOctalChar) {
            return TarUtils.formatLongOctalBytes(value, buf, offset, length);
        }
        if (length < 9) {
            TarUtils.formatLongBinary(value, buf, offset, length, negative);
        } else {
            TarUtils.formatBigIntegerBinary(value, buf, offset, length, negative);
        }
        buf[offset] = (byte)(negative ? 255 : 128);
        return offset + length;
    }

    private static void formatLongBinary(long value, byte[] buf, int offset, int length, boolean negative) {
        int bits = (length - 1) * 8;
        long max = 1L << bits;
        long val2 = Math.abs(value);
        if (val2 < 0L || val2 >= max) {
            throw new IllegalArgumentException("Value " + value + " is too large for " + length + " byte field.");
        }
        if (negative) {
            val2 ^= max - 1L;
            ++val2;
            val2 |= 255L << bits;
        }
        for (int i = offset + length - 1; i >= offset; --i) {
            buf[i] = (byte)val2;
            val2 >>= 8;
        }
    }

    private static void formatBigIntegerBinary(long value, byte[] buf, int offset, int length, boolean negative) {
        BigInteger val2 = BigInteger.valueOf(value);
        byte[] b = val2.toByteArray();
        int len = b.length;
        if (len > length - 1) {
            throw new IllegalArgumentException("Value " + value + " is too large for " + length + " byte field.");
        }
        int off = offset + length - len;
        System.arraycopy(b, 0, buf, off, len);
        byte fill = (byte)(negative ? 255 : 0);
        for (int i = offset + 1; i < off; ++i) {
            buf[i] = fill;
        }
    }

    public static int formatCheckSumOctalBytes(long value, byte[] buf, int offset, int length) {
        int idx = length - 2;
        TarUtils.formatUnsignedOctalString(value, buf, offset, idx);
        buf[offset + idx++] = 0;
        buf[offset + idx] = 32;
        return offset + length;
    }

    public static long computeCheckSum(byte[] buf) {
        long sum = 0L;
        for (byte element : buf) {
            sum += (long)(0xFF & element);
        }
        return sum;
    }

    public static boolean verifyCheckSum(byte[] header) {
        long storedSum = TarUtils.parseOctal(header, 148, 8);
        long unsignedSum = 0L;
        long signedSum = 0L;
        for (int i = 0; i < header.length; ++i) {
            int b = header[i];
            if (148 <= i && i < 156) {
                b = 32;
            }
            unsignedSum += (long)(0xFF & b);
            signedSum += (long)b;
        }
        return storedSum == unsignedSum || storedSum == signedSum;
    }

    @Deprecated
    protected static Map<String, String> parsePaxHeaders(InputStream inputStream, List<TarArchiveStructSparse> sparseHeaders, Map<String, String> globalPaxHeaders) throws IOException {
        return TarUtils.parsePaxHeaders(inputStream, sparseHeaders, globalPaxHeaders, -1L);
    }

    protected static Map<String, String> parsePaxHeaders(InputStream inputStream, List<TarArchiveStructSparse> sparseHeaders, Map<String, String> globalPaxHeaders, long headerSize) throws IOException {
        int ch;
        HashMap<String, String> headers = new HashMap<String, String>(globalPaxHeaders);
        Long offset = null;
        int totalRead = 0;
        block4: do {
            int len = 0;
            int read = 0;
            while ((ch = inputStream.read()) != -1) {
                ++read;
                ++totalRead;
                if (ch == 10) continue block4;
                if (ch == 32) {
                    ByteArrayOutputStream coll = new ByteArrayOutputStream();
                    while ((ch = inputStream.read()) != -1) {
                        ++read;
                        if (++totalRead < 0 || headerSize >= 0L && (long)totalRead >= headerSize) continue block4;
                        if (ch == 61) {
                            long numbytes;
                            String keyword = coll.toString("UTF-8");
                            int restLen = len - read;
                            if (restLen <= 1) {
                                headers.remove(keyword);
                                continue block4;
                            }
                            if (headerSize >= 0L && (long)restLen > headerSize - (long)totalRead) {
                                throw new IOException("Paxheader value size " + restLen + " exceeds size of header record");
                            }
                            byte[] rest = IOUtils.readRange(inputStream, restLen);
                            int got = rest.length;
                            if (got != restLen) {
                                throw new IOException("Failed to read Paxheader. Expected " + restLen + " bytes, read " + got);
                            }
                            totalRead += restLen;
                            if (rest[restLen - 1] != 10) {
                                throw new IOException("Failed to read Paxheader.Value should end with a newline");
                            }
                            String value = new String(rest, 0, restLen - 1, StandardCharsets.UTF_8);
                            headers.put(keyword, value);
                            if (keyword.equals("GNU.sparse.offset")) {
                                if (offset != null) {
                                    sparseHeaders.add(new TarArchiveStructSparse(offset, 0L));
                                }
                                try {
                                    offset = Long.valueOf(value);
                                } catch (NumberFormatException ex) {
                                    throw new IOException("Failed to read Paxheader.GNU.sparse.offset contains a non-numeric value");
                                }
                                if (offset < 0L) {
                                    throw new IOException("Failed to read Paxheader.GNU.sparse.offset contains negative value");
                                }
                            }
                            if (!keyword.equals("GNU.sparse.numbytes")) continue block4;
                            if (offset == null) {
                                throw new IOException("Failed to read Paxheader.GNU.sparse.offset is expected before GNU.sparse.numbytes shows up.");
                            }
                            try {
                                numbytes = Long.parseLong(value);
                            } catch (NumberFormatException ex) {
                                throw new IOException("Failed to read Paxheader.GNU.sparse.numbytes contains a non-numeric value.");
                            }
                            if (numbytes < 0L) {
                                throw new IOException("Failed to read Paxheader.GNU.sparse.numbytes contains negative value");
                            }
                            sparseHeaders.add(new TarArchiveStructSparse(offset, numbytes));
                            offset = null;
                            continue block4;
                        }
                        coll.write((byte)ch);
                    }
                    continue block4;
                }
                if (ch < 48 || ch > 57) {
                    throw new IOException("Failed to read Paxheader. Encountered a non-number while reading length");
                }
                len *= 10;
                len += ch - 48;
            }
        } while (ch != -1);
        if (offset != null) {
            sparseHeaders.add(new TarArchiveStructSparse(offset, 0L));
        }
        return headers;
    }

    protected static List<TarArchiveStructSparse> parsePAX01SparseHeaders(String sparseMap) {
        try {
            return TarUtils.parseFromPAX01SparseHeaders(sparseMap);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    protected static List<TarArchiveStructSparse> parseFromPAX01SparseHeaders(String sparseMap) throws IOException {
        ArrayList<TarArchiveStructSparse> sparseHeaders = new ArrayList<TarArchiveStructSparse>();
        String[] sparseHeaderStrings = sparseMap.split(",");
        if (sparseHeaderStrings.length % 2 == 1) {
            throw new IOException("Corrupted TAR archive. Bad format in GNU.sparse.map PAX Header");
        }
        for (int i = 0; i < sparseHeaderStrings.length; i += 2) {
            long sparseNumbytes;
            long sparseOffset;
            try {
                sparseOffset = Long.parseLong(sparseHeaderStrings[i]);
            } catch (NumberFormatException ex) {
                throw new IOException("Corrupted TAR archive. Sparse struct offset contains a non-numeric value");
            }
            if (sparseOffset < 0L) {
                throw new IOException("Corrupted TAR archive. Sparse struct offset contains negative value");
            }
            try {
                sparseNumbytes = Long.parseLong(sparseHeaderStrings[i + 1]);
            } catch (NumberFormatException ex) {
                throw new IOException("Corrupted TAR archive. Sparse struct numbytes contains a non-numeric value");
            }
            if (sparseNumbytes < 0L) {
                throw new IOException("Corrupted TAR archive. Sparse struct numbytes contains negative value");
            }
            sparseHeaders.add(new TarArchiveStructSparse(sparseOffset, sparseNumbytes));
        }
        return Collections.unmodifiableList(sparseHeaders);
    }

    protected static List<TarArchiveStructSparse> parsePAX1XSparseHeaders(InputStream inputStream, int recordSize) throws IOException {
        ArrayList<TarArchiveStructSparse> sparseHeaders = new ArrayList<TarArchiveStructSparse>();
        long bytesRead = 0L;
        long[] readResult = TarUtils.readLineOfNumberForPax1X(inputStream);
        long sparseHeadersCount = readResult[0];
        if (sparseHeadersCount < 0L) {
            throw new IOException("Corrupted TAR archive. Negative value in sparse headers block");
        }
        bytesRead += readResult[1];
        while (sparseHeadersCount-- > 0L) {
            readResult = TarUtils.readLineOfNumberForPax1X(inputStream);
            long sparseOffset = readResult[0];
            if (sparseOffset < 0L) {
                throw new IOException("Corrupted TAR archive. Sparse header block offset contains negative value");
            }
            bytesRead += readResult[1];
            readResult = TarUtils.readLineOfNumberForPax1X(inputStream);
            long sparseNumbytes = readResult[0];
            if (sparseNumbytes < 0L) {
                throw new IOException("Corrupted TAR archive. Sparse header block numbytes contains negative value");
            }
            bytesRead += readResult[1];
            sparseHeaders.add(new TarArchiveStructSparse(sparseOffset, sparseNumbytes));
        }
        long bytesToSkip = (long)recordSize - bytesRead % (long)recordSize;
        IOUtils.skip(inputStream, bytesToSkip);
        return sparseHeaders;
    }

    private static long[] readLineOfNumberForPax1X(InputStream inputStream) throws IOException {
        int number;
        long result = 0L;
        long bytesRead = 0L;
        while ((number = inputStream.read()) != 10) {
            ++bytesRead;
            if (number == -1) {
                throw new IOException("Unexpected EOF when reading parse information of 1.X PAX format");
            }
            if (number < 48 || number > 57) {
                throw new IOException("Corrupted TAR archive. Non-numeric value in sparse headers block");
            }
            result = result * 10L + (long)(number - 48);
        }
        return new long[]{result, ++bytesRead};
    }
}

