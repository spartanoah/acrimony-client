/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.archivers.zip;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;
import java.util.zip.ZipException;
import org.apache.commons.compress.archivers.zip.ExplodingInputStream;
import org.apache.commons.compress.archivers.zip.GeneralPurposeBit;
import org.apache.commons.compress.archivers.zip.InflaterInputStreamWithStatistics;
import org.apache.commons.compress.archivers.zip.UnshrinkingInputStream;
import org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException;
import org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraField;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryPredicate;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipEightByteInteger;
import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.apache.commons.compress.archivers.zip.ZipExtraField;
import org.apache.commons.compress.archivers.zip.ZipLong;
import org.apache.commons.compress.archivers.zip.ZipMethod;
import org.apache.commons.compress.archivers.zip.ZipShort;
import org.apache.commons.compress.archivers.zip.ZipSplitReadOnlySeekableByteChannel;
import org.apache.commons.compress.archivers.zip.ZipUtil;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.deflate64.Deflate64CompressorInputStream;
import org.apache.commons.compress.utils.BoundedArchiveInputStream;
import org.apache.commons.compress.utils.BoundedSeekableByteChannelInputStream;
import org.apache.commons.compress.utils.CountingInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.utils.InputStreamStatistics;

public class ZipFile
implements Closeable {
    private static final int HASH_SIZE = 509;
    static final int NIBLET_MASK = 15;
    static final int BYTE_SHIFT = 8;
    private static final int POS_0 = 0;
    private static final int POS_1 = 1;
    private static final int POS_2 = 2;
    private static final int POS_3 = 3;
    private static final byte[] ONE_ZERO_BYTE = new byte[1];
    private final List<ZipArchiveEntry> entries = new LinkedList<ZipArchiveEntry>();
    private final Map<String, LinkedList<ZipArchiveEntry>> nameMap = new HashMap<String, LinkedList<ZipArchiveEntry>>(509);
    private final String encoding;
    private final ZipEncoding zipEncoding;
    private final String archiveName;
    private final SeekableByteChannel archive;
    private final boolean useUnicodeExtraFields;
    private volatile boolean closed = true;
    private final boolean isSplitZipArchive;
    private final byte[] dwordBuf = new byte[8];
    private final byte[] wordBuf = new byte[4];
    private final byte[] cfhBuf = new byte[42];
    private final byte[] shortBuf = new byte[2];
    private final ByteBuffer dwordBbuf = ByteBuffer.wrap(this.dwordBuf);
    private final ByteBuffer wordBbuf = ByteBuffer.wrap(this.wordBuf);
    private final ByteBuffer cfhBbuf = ByteBuffer.wrap(this.cfhBuf);
    private final ByteBuffer shortBbuf = ByteBuffer.wrap(this.shortBuf);
    private long centralDirectoryStartDiskNumber;
    private long centralDirectoryStartRelativeOffset;
    private long centralDirectoryStartOffset;
    private static final int CFH_LEN = 42;
    private static final long CFH_SIG = ZipLong.getValue(ZipArchiveOutputStream.CFH_SIG);
    static final int MIN_EOCD_SIZE = 22;
    private static final int MAX_EOCD_SIZE = 65557;
    private static final int CFD_LOCATOR_OFFSET = 16;
    private static final int CFD_DISK_OFFSET = 6;
    private static final int CFD_LOCATOR_RELATIVE_OFFSET = 8;
    private static final int ZIP64_EOCDL_LENGTH = 20;
    private static final int ZIP64_EOCDL_LOCATOR_OFFSET = 8;
    private static final int ZIP64_EOCD_CFD_LOCATOR_OFFSET = 48;
    private static final int ZIP64_EOCD_CFD_DISK_OFFSET = 20;
    private static final int ZIP64_EOCD_CFD_LOCATOR_RELATIVE_OFFSET = 24;
    private static final long LFH_OFFSET_FOR_FILENAME_LENGTH = 26L;
    private final Comparator<ZipArchiveEntry> offsetComparator = Comparator.comparingLong(ZipArchiveEntry::getDiskNumberStart).thenComparingLong(ZipArchiveEntry::getLocalHeaderOffset);

    public ZipFile(File f) throws IOException {
        this(f, "UTF8");
    }

    public ZipFile(String name) throws IOException {
        this(new File(name), "UTF8");
    }

    public ZipFile(String name, String encoding) throws IOException {
        this(new File(name), encoding, true);
    }

    public ZipFile(File f, String encoding) throws IOException {
        this(f, encoding, true);
    }

    public ZipFile(File f, String encoding, boolean useUnicodeExtraFields) throws IOException {
        this(f, encoding, useUnicodeExtraFields, false);
    }

    public ZipFile(File f, String encoding, boolean useUnicodeExtraFields, boolean ignoreLocalFileHeader) throws IOException {
        this(Files.newByteChannel(f.toPath(), EnumSet.of(StandardOpenOption.READ), new FileAttribute[0]), f.getAbsolutePath(), encoding, useUnicodeExtraFields, true, ignoreLocalFileHeader);
    }

    public ZipFile(SeekableByteChannel channel) throws IOException {
        this(channel, "unknown archive", "UTF8", true);
    }

    public ZipFile(SeekableByteChannel channel, String encoding) throws IOException {
        this(channel, "unknown archive", encoding, true);
    }

    public ZipFile(SeekableByteChannel channel, String archiveName, String encoding, boolean useUnicodeExtraFields) throws IOException {
        this(channel, archiveName, encoding, useUnicodeExtraFields, false, false);
    }

    public ZipFile(SeekableByteChannel channel, String archiveName, String encoding, boolean useUnicodeExtraFields, boolean ignoreLocalFileHeader) throws IOException {
        this(channel, archiveName, encoding, useUnicodeExtraFields, false, ignoreLocalFileHeader);
    }

    private ZipFile(SeekableByteChannel channel, String archiveName, String encoding, boolean useUnicodeExtraFields, boolean closeOnError, boolean ignoreLocalFileHeader) throws IOException {
        this.isSplitZipArchive = channel instanceof ZipSplitReadOnlySeekableByteChannel;
        this.archiveName = archiveName;
        this.encoding = encoding;
        this.zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
        this.useUnicodeExtraFields = useUnicodeExtraFields;
        this.archive = channel;
        boolean success = false;
        try {
            Map<ZipArchiveEntry, NameAndComment> entriesWithoutUTF8Flag = this.populateFromCentralDirectory();
            if (!ignoreLocalFileHeader) {
                this.resolveLocalFileHeaderData(entriesWithoutUTF8Flag);
            }
            this.fillNameMap();
            success = true;
            boolean bl = this.closed = !success;
        } catch (IOException e) {
            try {
                throw new IOException("Error on ZipFile " + archiveName, e);
            } catch (Throwable throwable) {
                boolean bl = this.closed = !success;
                if (!success && closeOnError) {
                    IOUtils.closeQuietly(this.archive);
                }
                throw throwable;
            }
        }
        if (!success && closeOnError) {
            IOUtils.closeQuietly(this.archive);
        }
    }

    public String getEncoding() {
        return this.encoding;
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
        this.archive.close();
    }

    public static void closeQuietly(ZipFile zipfile) {
        IOUtils.closeQuietly(zipfile);
    }

    public Enumeration<ZipArchiveEntry> getEntries() {
        return Collections.enumeration(this.entries);
    }

    public Enumeration<ZipArchiveEntry> getEntriesInPhysicalOrder() {
        ZipArchiveEntry[] allEntries = this.entries.toArray(ZipArchiveEntry.EMPTY_ZIP_ARCHIVE_ENTRY_ARRAY);
        Arrays.sort(allEntries, this.offsetComparator);
        return Collections.enumeration(Arrays.asList(allEntries));
    }

    public ZipArchiveEntry getEntry(String name) {
        LinkedList<ZipArchiveEntry> entriesOfThatName = this.nameMap.get(name);
        return entriesOfThatName != null ? entriesOfThatName.getFirst() : null;
    }

    public Iterable<ZipArchiveEntry> getEntries(String name) {
        List<ZipArchiveEntry> entriesOfThatName = (List<ZipArchiveEntry>)this.nameMap.get(name);
        return entriesOfThatName != null ? entriesOfThatName : Collections.emptyList();
    }

    public Iterable<ZipArchiveEntry> getEntriesInPhysicalOrder(String name) {
        ZipArchiveEntry[] entriesOfThatName = ZipArchiveEntry.EMPTY_ZIP_ARCHIVE_ENTRY_ARRAY;
        if (this.nameMap.containsKey(name)) {
            entriesOfThatName = this.nameMap.get(name).toArray(entriesOfThatName);
            Arrays.sort(entriesOfThatName, this.offsetComparator);
        }
        return Arrays.asList(entriesOfThatName);
    }

    public boolean canReadEntryData(ZipArchiveEntry ze) {
        return ZipUtil.canHandleEntryData(ze);
    }

    public InputStream getRawInputStream(ZipArchiveEntry ze) {
        if (!(ze instanceof Entry)) {
            return null;
        }
        long start = ze.getDataOffset();
        if (start == -1L) {
            return null;
        }
        return this.createBoundedInputStream(start, ze.getCompressedSize());
    }

    public void copyRawEntries(ZipArchiveOutputStream target, ZipArchiveEntryPredicate predicate) throws IOException {
        Enumeration<ZipArchiveEntry> src = this.getEntriesInPhysicalOrder();
        while (src.hasMoreElements()) {
            ZipArchiveEntry entry = src.nextElement();
            if (!predicate.test(entry)) continue;
            target.addRawArchiveEntry(entry, this.getRawInputStream(entry));
        }
    }

    public InputStream getInputStream(ZipArchiveEntry ze) throws IOException {
        if (!(ze instanceof Entry)) {
            return null;
        }
        ZipUtil.checkRequestedFeatures(ze);
        long start = this.getDataOffset(ze);
        BufferedInputStream is = new BufferedInputStream(this.createBoundedInputStream(start, ze.getCompressedSize()));
        switch (ZipMethod.getMethodByCode(ze.getMethod())) {
            case STORED: {
                return new StoredStatisticsStream(is);
            }
            case UNSHRINKING: {
                return new UnshrinkingInputStream(is);
            }
            case IMPLODING: {
                try {
                    return new ExplodingInputStream(ze.getGeneralPurposeBit().getSlidingDictionarySize(), ze.getGeneralPurposeBit().getNumberOfShannonFanoTrees(), is);
                } catch (IllegalArgumentException ex) {
                    throw new IOException("bad IMPLODE data", ex);
                }
            }
            case DEFLATED: {
                final Inflater inflater = new Inflater(true);
                return new InflaterInputStreamWithStatistics(new SequenceInputStream(is, new ByteArrayInputStream(ONE_ZERO_BYTE)), inflater){

                    @Override
                    public void close() throws IOException {
                        try {
                            super.close();
                        } finally {
                            inflater.end();
                        }
                    }
                };
            }
            case BZIP2: {
                return new BZip2CompressorInputStream(is);
            }
            case ENHANCED_DEFLATED: {
                return new Deflate64CompressorInputStream(is);
            }
        }
        throw new UnsupportedZipFeatureException(ZipMethod.getMethodByCode(ze.getMethod()), ze);
    }

    public String getUnixSymlink(ZipArchiveEntry entry) throws IOException {
        if (entry != null && entry.isUnixSymlink()) {
            try (InputStream in = this.getInputStream(entry);){
                String string = this.zipEncoding.decode(IOUtils.toByteArray(in));
                return string;
            }
        }
        return null;
    }

    protected void finalize() throws Throwable {
        try {
            if (!this.closed) {
                System.err.println("Cleaning up unclosed ZipFile for archive " + this.archiveName);
                this.close();
            }
        } finally {
            super.finalize();
        }
    }

    private Map<ZipArchiveEntry, NameAndComment> populateFromCentralDirectory() throws IOException {
        HashMap<ZipArchiveEntry, NameAndComment> noUTF8Flag = new HashMap<ZipArchiveEntry, NameAndComment>();
        this.positionAtCentralDirectory();
        this.centralDirectoryStartOffset = this.archive.position();
        this.wordBbuf.rewind();
        IOUtils.readFully(this.archive, this.wordBbuf);
        long sig = ZipLong.getValue(this.wordBuf);
        if (sig != CFH_SIG && this.startsWithLocalFileHeader()) {
            throw new IOException("Central directory is empty, can't expand corrupt archive.");
        }
        while (sig == CFH_SIG) {
            this.readCentralDirectoryEntry(noUTF8Flag);
            this.wordBbuf.rewind();
            IOUtils.readFully(this.archive, this.wordBbuf);
            sig = ZipLong.getValue(this.wordBuf);
        }
        return noUTF8Flag;
    }

    private void readCentralDirectoryEntry(Map<ZipArchiveEntry, NameAndComment> noUTF8Flag) throws IOException {
        ZipEncoding entryEncoding;
        this.cfhBbuf.rewind();
        IOUtils.readFully(this.archive, this.cfhBbuf);
        int off = 0;
        Entry ze = new Entry();
        int versionMadeBy = ZipShort.getValue(this.cfhBuf, off);
        ze.setVersionMadeBy(versionMadeBy);
        ze.setPlatform(versionMadeBy >> 8 & 0xF);
        ze.setVersionRequired(ZipShort.getValue(this.cfhBuf, off += 2));
        GeneralPurposeBit gpFlag = GeneralPurposeBit.parse(this.cfhBuf, off += 2);
        boolean hasUTF8Flag = gpFlag.usesUTF8ForNames();
        ZipEncoding zipEncoding = entryEncoding = hasUTF8Flag ? ZipEncodingHelper.UTF8_ZIP_ENCODING : this.zipEncoding;
        if (hasUTF8Flag) {
            ze.setNameSource(ZipArchiveEntry.NameSource.NAME_WITH_EFS_FLAG);
        }
        ze.setGeneralPurposeBit(gpFlag);
        ze.setRawFlag(ZipShort.getValue(this.cfhBuf, off));
        ze.setMethod(ZipShort.getValue(this.cfhBuf, off += 2));
        long time = ZipUtil.dosToJavaTime(ZipLong.getValue(this.cfhBuf, off += 2));
        ze.setTime(time);
        ze.setCrc(ZipLong.getValue(this.cfhBuf, off += 4));
        long size = ZipLong.getValue(this.cfhBuf, off += 4);
        if (size < 0L) {
            throw new IOException("broken archive, entry with negative compressed size");
        }
        ze.setCompressedSize(size);
        size = ZipLong.getValue(this.cfhBuf, off += 4);
        if (size < 0L) {
            throw new IOException("broken archive, entry with negative size");
        }
        ze.setSize(size);
        int fileNameLen = ZipShort.getValue(this.cfhBuf, off += 4);
        off += 2;
        if (fileNameLen < 0) {
            throw new IOException("broken archive, entry with negative fileNameLen");
        }
        int extraLen = ZipShort.getValue(this.cfhBuf, off);
        off += 2;
        if (extraLen < 0) {
            throw new IOException("broken archive, entry with negative extraLen");
        }
        int commentLen = ZipShort.getValue(this.cfhBuf, off);
        off += 2;
        if (commentLen < 0) {
            throw new IOException("broken archive, entry with negative commentLen");
        }
        ze.setDiskNumberStart(ZipShort.getValue(this.cfhBuf, off));
        ze.setInternalAttributes(ZipShort.getValue(this.cfhBuf, off += 2));
        ze.setExternalAttributes(ZipLong.getValue(this.cfhBuf, off += 2));
        off += 4;
        byte[] fileName = IOUtils.readRange(this.archive, fileNameLen);
        if (fileName.length < fileNameLen) {
            throw new EOFException();
        }
        ze.setName(entryEncoding.decode(fileName), fileName);
        ze.setLocalHeaderOffset(ZipLong.getValue(this.cfhBuf, off));
        this.entries.add(ze);
        byte[] cdExtraData = IOUtils.readRange(this.archive, extraLen);
        if (cdExtraData.length < extraLen) {
            throw new EOFException();
        }
        try {
            ze.setCentralDirectoryExtra(cdExtraData);
        } catch (RuntimeException ex) {
            ZipException z = new ZipException("Invalid extra data in entry " + ze.getName());
            z.initCause(ex);
            throw z;
        }
        this.setSizesAndOffsetFromZip64Extra(ze);
        this.sanityCheckLFHOffset(ze);
        byte[] comment = IOUtils.readRange(this.archive, commentLen);
        if (comment.length < commentLen) {
            throw new EOFException();
        }
        ze.setComment(entryEncoding.decode(comment));
        if (!hasUTF8Flag && this.useUnicodeExtraFields) {
            noUTF8Flag.put(ze, new NameAndComment(fileName, comment));
        }
        ze.setStreamContiguous(true);
    }

    private void sanityCheckLFHOffset(ZipArchiveEntry ze) throws IOException {
        if (ze.getDiskNumberStart() < 0L) {
            throw new IOException("broken archive, entry with negative disk number");
        }
        if (ze.getLocalHeaderOffset() < 0L) {
            throw new IOException("broken archive, entry with negative local file header offset");
        }
        if (this.isSplitZipArchive) {
            if (ze.getDiskNumberStart() > this.centralDirectoryStartDiskNumber) {
                throw new IOException("local file header for " + ze.getName() + " starts on a later disk than central directory");
            }
            if (ze.getDiskNumberStart() == this.centralDirectoryStartDiskNumber && ze.getLocalHeaderOffset() > this.centralDirectoryStartRelativeOffset) {
                throw new IOException("local file header for " + ze.getName() + " starts after central directory");
            }
        } else if (ze.getLocalHeaderOffset() > this.centralDirectoryStartOffset) {
            throw new IOException("local file header for " + ze.getName() + " starts after central directory");
        }
    }

    private void setSizesAndOffsetFromZip64Extra(ZipArchiveEntry ze) throws IOException {
        ZipExtraField extra = ze.getExtraField(Zip64ExtendedInformationExtraField.HEADER_ID);
        if (extra != null && !(extra instanceof Zip64ExtendedInformationExtraField)) {
            throw new ZipException("archive contains unparseable zip64 extra field");
        }
        Zip64ExtendedInformationExtraField z64 = (Zip64ExtendedInformationExtraField)extra;
        if (z64 != null) {
            long size;
            boolean hasUncompressedSize = ze.getSize() == 0xFFFFFFFFL;
            boolean hasCompressedSize = ze.getCompressedSize() == 0xFFFFFFFFL;
            boolean hasRelativeHeaderOffset = ze.getLocalHeaderOffset() == 0xFFFFFFFFL;
            boolean hasDiskStart = ze.getDiskNumberStart() == 65535L;
            z64.reparseCentralDirectoryData(hasUncompressedSize, hasCompressedSize, hasRelativeHeaderOffset, hasDiskStart);
            if (hasUncompressedSize) {
                size = z64.getSize().getLongValue();
                if (size < 0L) {
                    throw new IOException("broken archive, entry with negative size");
                }
                ze.setSize(size);
            } else if (hasCompressedSize) {
                z64.setSize(new ZipEightByteInteger(ze.getSize()));
            }
            if (hasCompressedSize) {
                size = z64.getCompressedSize().getLongValue();
                if (size < 0L) {
                    throw new IOException("broken archive, entry with negative compressed size");
                }
                ze.setCompressedSize(size);
            } else if (hasUncompressedSize) {
                z64.setCompressedSize(new ZipEightByteInteger(ze.getCompressedSize()));
            }
            if (hasRelativeHeaderOffset) {
                ze.setLocalHeaderOffset(z64.getRelativeHeaderOffset().getLongValue());
            }
            if (hasDiskStart) {
                ze.setDiskNumberStart(z64.getDiskStartNumber().getValue());
            }
        }
    }

    private void positionAtCentralDirectory() throws IOException {
        boolean searchedForZip64EOCD;
        this.positionAtEndOfCentralDirectoryRecord();
        boolean found = false;
        boolean bl = searchedForZip64EOCD = this.archive.position() > 20L;
        if (searchedForZip64EOCD) {
            this.archive.position(this.archive.position() - 20L);
            this.wordBbuf.rewind();
            IOUtils.readFully(this.archive, this.wordBbuf);
            found = Arrays.equals(ZipArchiveOutputStream.ZIP64_EOCD_LOC_SIG, this.wordBuf);
        }
        if (!found) {
            if (searchedForZip64EOCD) {
                this.skipBytes(16);
            }
            this.positionAtCentralDirectory32();
        } else {
            this.positionAtCentralDirectory64();
        }
    }

    private void positionAtCentralDirectory64() throws IOException {
        if (this.isSplitZipArchive) {
            this.wordBbuf.rewind();
            IOUtils.readFully(this.archive, this.wordBbuf);
            long diskNumberOfEOCD = ZipLong.getValue(this.wordBuf);
            this.dwordBbuf.rewind();
            IOUtils.readFully(this.archive, this.dwordBbuf);
            long relativeOffsetOfEOCD = ZipEightByteInteger.getLongValue(this.dwordBuf);
            ((ZipSplitReadOnlySeekableByteChannel)this.archive).position(diskNumberOfEOCD, relativeOffsetOfEOCD);
        } else {
            this.skipBytes(4);
            this.dwordBbuf.rewind();
            IOUtils.readFully(this.archive, this.dwordBbuf);
            this.archive.position(ZipEightByteInteger.getLongValue(this.dwordBuf));
        }
        this.wordBbuf.rewind();
        IOUtils.readFully(this.archive, this.wordBbuf);
        if (!Arrays.equals(this.wordBuf, ZipArchiveOutputStream.ZIP64_EOCD_SIG)) {
            throw new ZipException("Archive's ZIP64 end of central directory locator is corrupt.");
        }
        if (this.isSplitZipArchive) {
            this.skipBytes(16);
            this.wordBbuf.rewind();
            IOUtils.readFully(this.archive, this.wordBbuf);
            this.centralDirectoryStartDiskNumber = ZipLong.getValue(this.wordBuf);
            this.skipBytes(24);
            this.dwordBbuf.rewind();
            IOUtils.readFully(this.archive, this.dwordBbuf);
            this.centralDirectoryStartRelativeOffset = ZipEightByteInteger.getLongValue(this.dwordBuf);
            ((ZipSplitReadOnlySeekableByteChannel)this.archive).position(this.centralDirectoryStartDiskNumber, this.centralDirectoryStartRelativeOffset);
        } else {
            this.skipBytes(44);
            this.dwordBbuf.rewind();
            IOUtils.readFully(this.archive, this.dwordBbuf);
            this.centralDirectoryStartDiskNumber = 0L;
            this.centralDirectoryStartRelativeOffset = ZipEightByteInteger.getLongValue(this.dwordBuf);
            this.archive.position(this.centralDirectoryStartRelativeOffset);
        }
    }

    private void positionAtCentralDirectory32() throws IOException {
        if (this.isSplitZipArchive) {
            this.skipBytes(6);
            this.shortBbuf.rewind();
            IOUtils.readFully(this.archive, this.shortBbuf);
            this.centralDirectoryStartDiskNumber = ZipShort.getValue(this.shortBuf);
            this.skipBytes(8);
            this.wordBbuf.rewind();
            IOUtils.readFully(this.archive, this.wordBbuf);
            this.centralDirectoryStartRelativeOffset = ZipLong.getValue(this.wordBuf);
            ((ZipSplitReadOnlySeekableByteChannel)this.archive).position(this.centralDirectoryStartDiskNumber, this.centralDirectoryStartRelativeOffset);
        } else {
            this.skipBytes(16);
            this.wordBbuf.rewind();
            IOUtils.readFully(this.archive, this.wordBbuf);
            this.centralDirectoryStartDiskNumber = 0L;
            this.centralDirectoryStartRelativeOffset = ZipLong.getValue(this.wordBuf);
            this.archive.position(this.centralDirectoryStartRelativeOffset);
        }
    }

    private void positionAtEndOfCentralDirectoryRecord() throws IOException {
        boolean found = this.tryToLocateSignature(22L, 65557L, ZipArchiveOutputStream.EOCD_SIG);
        if (!found) {
            throw new ZipException("Archive is not a ZIP archive");
        }
    }

    private boolean tryToLocateSignature(long minDistanceFromEnd, long maxDistanceFromEnd, byte[] sig) throws IOException {
        long off;
        boolean found = false;
        long stopSearching = Math.max(0L, this.archive.size() - maxDistanceFromEnd);
        if (off >= 0L) {
            for (off = this.archive.size() - minDistanceFromEnd; off >= stopSearching; --off) {
                this.archive.position(off);
                try {
                    this.wordBbuf.rewind();
                    IOUtils.readFully(this.archive, this.wordBbuf);
                    this.wordBbuf.flip();
                } catch (EOFException ex) {
                    break;
                }
                byte curr = this.wordBbuf.get();
                if (curr != sig[0] || (curr = this.wordBbuf.get()) != sig[1] || (curr = this.wordBbuf.get()) != sig[2] || (curr = this.wordBbuf.get()) != sig[3]) continue;
                found = true;
                break;
            }
        }
        if (found) {
            this.archive.position(off);
        }
        return found;
    }

    private void skipBytes(int count) throws IOException {
        long currentPosition = this.archive.position();
        long newPosition = currentPosition + (long)count;
        if (newPosition > this.archive.size()) {
            throw new EOFException();
        }
        this.archive.position(newPosition);
    }

    private void resolveLocalFileHeaderData(Map<ZipArchiveEntry, NameAndComment> entriesWithoutUTF8Flag) throws IOException {
        for (ZipArchiveEntry zipArchiveEntry : this.entries) {
            Entry ze = (Entry)zipArchiveEntry;
            int[] lens = this.setDataOffset(ze);
            int fileNameLen = lens[0];
            int extraFieldLen = lens[1];
            this.skipBytes(fileNameLen);
            byte[] localExtraData = IOUtils.readRange(this.archive, extraFieldLen);
            if (localExtraData.length < extraFieldLen) {
                throw new EOFException();
            }
            try {
                ze.setExtra(localExtraData);
            } catch (RuntimeException ex) {
                ZipException z = new ZipException("Invalid extra data in entry " + ze.getName());
                z.initCause(ex);
                throw z;
            }
            if (!entriesWithoutUTF8Flag.containsKey(ze)) continue;
            NameAndComment nc = entriesWithoutUTF8Flag.get(ze);
            ZipUtil.setNameAndCommentFromExtraFields(ze, nc.name, nc.comment);
        }
    }

    private void fillNameMap() {
        for (ZipArchiveEntry ze : this.entries) {
            String name = ze.getName();
            LinkedList entriesOfThatName = this.nameMap.computeIfAbsent(name, k -> new LinkedList());
            entriesOfThatName.addLast(ze);
        }
    }

    private int[] setDataOffset(ZipArchiveEntry ze) throws IOException {
        long offset = ze.getLocalHeaderOffset();
        if (this.isSplitZipArchive) {
            ((ZipSplitReadOnlySeekableByteChannel)this.archive).position(ze.getDiskNumberStart(), offset + 26L);
            offset = this.archive.position() - 26L;
        } else {
            this.archive.position(offset + 26L);
        }
        this.wordBbuf.rewind();
        IOUtils.readFully(this.archive, this.wordBbuf);
        this.wordBbuf.flip();
        this.wordBbuf.get(this.shortBuf);
        int fileNameLen = ZipShort.getValue(this.shortBuf);
        this.wordBbuf.get(this.shortBuf);
        int extraFieldLen = ZipShort.getValue(this.shortBuf);
        ze.setDataOffset(offset + 26L + 2L + 2L + (long)fileNameLen + (long)extraFieldLen);
        if (ze.getDataOffset() + ze.getCompressedSize() > this.centralDirectoryStartOffset) {
            throw new IOException("data for " + ze.getName() + " overlaps with central directory.");
        }
        return new int[]{fileNameLen, extraFieldLen};
    }

    private long getDataOffset(ZipArchiveEntry ze) throws IOException {
        long s = ze.getDataOffset();
        if (s == -1L) {
            this.setDataOffset(ze);
            return ze.getDataOffset();
        }
        return s;
    }

    private boolean startsWithLocalFileHeader() throws IOException {
        this.archive.position(0L);
        this.wordBbuf.rewind();
        IOUtils.readFully(this.archive, this.wordBbuf);
        return Arrays.equals(this.wordBuf, ZipArchiveOutputStream.LFH_SIG);
    }

    private BoundedArchiveInputStream createBoundedInputStream(long start, long remaining) {
        if (start < 0L || remaining < 0L || start + remaining < start) {
            throw new IllegalArgumentException("Corrupted archive, stream boundaries are out of range");
        }
        return this.archive instanceof FileChannel ? new BoundedFileChannelInputStream(start, remaining) : new BoundedSeekableByteChannelInputStream(start, remaining, this.archive);
    }

    private static class StoredStatisticsStream
    extends CountingInputStream
    implements InputStreamStatistics {
        StoredStatisticsStream(InputStream in) {
            super(in);
        }

        @Override
        public long getCompressedCount() {
            return super.getBytesRead();
        }

        @Override
        public long getUncompressedCount() {
            return this.getCompressedCount();
        }
    }

    private static class Entry
    extends ZipArchiveEntry {
        Entry() {
        }

        @Override
        public int hashCode() {
            return 3 * super.hashCode() + (int)this.getLocalHeaderOffset() + (int)(this.getLocalHeaderOffset() >> 32);
        }

        @Override
        public boolean equals(Object other) {
            if (super.equals(other)) {
                Entry otherEntry = (Entry)other;
                return this.getLocalHeaderOffset() == otherEntry.getLocalHeaderOffset() && super.getDataOffset() == otherEntry.getDataOffset() && super.getDiskNumberStart() == otherEntry.getDiskNumberStart();
            }
            return false;
        }
    }

    private static final class NameAndComment {
        private final byte[] name;
        private final byte[] comment;

        private NameAndComment(byte[] name, byte[] comment) {
            this.name = name;
            this.comment = comment;
        }
    }

    private class BoundedFileChannelInputStream
    extends BoundedArchiveInputStream {
        private final FileChannel archive;

        BoundedFileChannelInputStream(long start, long remaining) {
            super(start, remaining);
            this.archive = (FileChannel)ZipFile.this.archive;
        }

        @Override
        protected int read(long pos, ByteBuffer buf) throws IOException {
            int read = this.archive.read(buf, pos);
            buf.flip();
            return read;
        }
    }
}

