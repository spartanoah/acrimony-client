/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.archivers.sevenz;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import org.apache.commons.compress.MemoryLimitException;
import org.apache.commons.compress.archivers.sevenz.Archive;
import org.apache.commons.compress.archivers.sevenz.BindPair;
import org.apache.commons.compress.archivers.sevenz.BoundedSeekableByteChannelInputStream;
import org.apache.commons.compress.archivers.sevenz.Coder;
import org.apache.commons.compress.archivers.sevenz.Coders;
import org.apache.commons.compress.archivers.sevenz.Folder;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFileOptions;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;
import org.apache.commons.compress.archivers.sevenz.SevenZMethodConfiguration;
import org.apache.commons.compress.archivers.sevenz.StartHeader;
import org.apache.commons.compress.archivers.sevenz.StreamMap;
import org.apache.commons.compress.archivers.sevenz.SubStreamsInfo;
import org.apache.commons.compress.utils.BoundedInputStream;
import org.apache.commons.compress.utils.ByteUtils;
import org.apache.commons.compress.utils.CRC32VerifyingInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.utils.InputStreamStatistics;

public class SevenZFile
implements Closeable {
    static final int SIGNATURE_HEADER_SIZE = 32;
    private static final String DEFAULT_FILE_NAME = "unknown archive";
    private final String fileName;
    private SeekableByteChannel channel;
    private final Archive archive;
    private int currentEntryIndex = -1;
    private int currentFolderIndex = -1;
    private InputStream currentFolderInputStream;
    private byte[] password;
    private final SevenZFileOptions options;
    private long compressedBytesReadFromCurrentEntry;
    private long uncompressedBytesReadFromCurrentEntry;
    private final ArrayList<InputStream> deferredBlockStreams = new ArrayList();
    static final byte[] sevenZSignature = new byte[]{55, 122, -68, -81, 39, 28};
    private static final CharsetEncoder PASSWORD_ENCODER = StandardCharsets.UTF_16LE.newEncoder();

    public SevenZFile(File fileName, char[] password) throws IOException {
        this(fileName, password, SevenZFileOptions.DEFAULT);
    }

    public SevenZFile(File fileName, char[] password, SevenZFileOptions options) throws IOException {
        this(Files.newByteChannel(fileName.toPath(), EnumSet.of(StandardOpenOption.READ), new FileAttribute[0]), fileName.getAbsolutePath(), SevenZFile.utf16Decode(password), true, options);
    }

    @Deprecated
    public SevenZFile(File fileName, byte[] password) throws IOException {
        this(Files.newByteChannel(fileName.toPath(), EnumSet.of(StandardOpenOption.READ), new FileAttribute[0]), fileName.getAbsolutePath(), password, true, SevenZFileOptions.DEFAULT);
    }

    public SevenZFile(SeekableByteChannel channel) throws IOException {
        this(channel, SevenZFileOptions.DEFAULT);
    }

    public SevenZFile(SeekableByteChannel channel, SevenZFileOptions options) throws IOException {
        this(channel, DEFAULT_FILE_NAME, null, options);
    }

    public SevenZFile(SeekableByteChannel channel, char[] password) throws IOException {
        this(channel, password, SevenZFileOptions.DEFAULT);
    }

    public SevenZFile(SeekableByteChannel channel, char[] password, SevenZFileOptions options) throws IOException {
        this(channel, DEFAULT_FILE_NAME, password, options);
    }

    public SevenZFile(SeekableByteChannel channel, String fileName, char[] password) throws IOException {
        this(channel, fileName, password, SevenZFileOptions.DEFAULT);
    }

    public SevenZFile(SeekableByteChannel channel, String fileName, char[] password, SevenZFileOptions options) throws IOException {
        this(channel, fileName, SevenZFile.utf16Decode(password), false, options);
    }

    public SevenZFile(SeekableByteChannel channel, String fileName) throws IOException {
        this(channel, fileName, SevenZFileOptions.DEFAULT);
    }

    public SevenZFile(SeekableByteChannel channel, String fileName, SevenZFileOptions options) throws IOException {
        this(channel, fileName, null, false, options);
    }

    @Deprecated
    public SevenZFile(SeekableByteChannel channel, byte[] password) throws IOException {
        this(channel, DEFAULT_FILE_NAME, password);
    }

    @Deprecated
    public SevenZFile(SeekableByteChannel channel, String fileName, byte[] password) throws IOException {
        this(channel, fileName, password, false, SevenZFileOptions.DEFAULT);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private SevenZFile(SeekableByteChannel channel, String filename, byte[] password, boolean closeOnError, SevenZFileOptions options) throws IOException {
        boolean succeeded = false;
        this.channel = channel;
        this.fileName = filename;
        this.options = options;
        try {
            this.archive = this.readHeaders(password);
            this.password = (byte[])(password != null ? Arrays.copyOf(password, password.length) : null);
            succeeded = true;
        } finally {
            if (!succeeded && closeOnError) {
                this.channel.close();
            }
        }
    }

    public SevenZFile(File fileName) throws IOException {
        this(fileName, SevenZFileOptions.DEFAULT);
    }

    public SevenZFile(File fileName, SevenZFileOptions options) throws IOException {
        this(fileName, null, options);
    }

    @Override
    public void close() throws IOException {
        if (this.channel != null) {
            try {
                this.channel.close();
            } finally {
                this.channel = null;
                if (this.password != null) {
                    Arrays.fill(this.password, (byte)0);
                }
                this.password = null;
            }
        }
    }

    public SevenZArchiveEntry getNextEntry() throws IOException {
        if (this.currentEntryIndex >= this.archive.files.length - 1) {
            return null;
        }
        ++this.currentEntryIndex;
        SevenZArchiveEntry entry = this.archive.files[this.currentEntryIndex];
        if (entry.getName() == null && this.options.getUseDefaultNameForUnnamedEntries()) {
            entry.setName(this.getDefaultName());
        }
        this.buildDecodingStream(this.currentEntryIndex, false);
        this.compressedBytesReadFromCurrentEntry = 0L;
        this.uncompressedBytesReadFromCurrentEntry = 0L;
        return entry;
    }

    public Iterable<SevenZArchiveEntry> getEntries() {
        return new ArrayList<SevenZArchiveEntry>(Arrays.asList(this.archive.files));
    }

    private Archive readHeaders(byte[] password) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
        this.readFully(buf);
        byte[] signature = new byte[6];
        buf.get(signature);
        if (!Arrays.equals(signature, sevenZSignature)) {
            throw new IOException("Bad 7z signature");
        }
        byte archiveVersionMajor = buf.get();
        byte archiveVersionMinor = buf.get();
        if (archiveVersionMajor != 0) {
            throw new IOException(String.format("Unsupported 7z version (%d,%d)", archiveVersionMajor, archiveVersionMinor));
        }
        boolean headerLooksValid = false;
        long startHeaderCrc = 0xFFFFFFFFL & (long)buf.getInt();
        if (startHeaderCrc == 0L) {
            long currentPosition = this.channel.position();
            ByteBuffer peekBuf = ByteBuffer.allocate(20);
            this.readFully(peekBuf);
            this.channel.position(currentPosition);
            while (peekBuf.hasRemaining()) {
                if (peekBuf.get() == 0) continue;
                headerLooksValid = true;
                break;
            }
        } else {
            headerLooksValid = true;
        }
        if (headerLooksValid) {
            StartHeader startHeader = this.readStartHeader(startHeaderCrc);
            return this.initializeArchive(startHeader, password, true);
        }
        if (this.options.getTryToRecoverBrokenArchives()) {
            return this.tryToLocateEndHeader(password);
        }
        throw new IOException("archive seems to be invalid.\nYou may want to retry and enable the tryToRecoverBrokenArchives if the archive could be a multi volume archive that has been closed prematurely.");
    }

    private Archive tryToLocateEndHeader(byte[] password) throws IOException {
        ByteBuffer nidBuf = ByteBuffer.allocate(1);
        long searchLimit = 0x100000L;
        long previousDataSize = this.channel.position() + 20L;
        long minPos = this.channel.position() + 0x100000L > this.channel.size() ? this.channel.position() : this.channel.size() - 0x100000L;
        long pos = this.channel.size() - 1L;
        while (pos > minPos) {
            this.channel.position(--pos);
            nidBuf.rewind();
            if (this.channel.read(nidBuf) < 1) {
                throw new EOFException();
            }
            byte nid = nidBuf.array()[0];
            if (nid != 23 && nid != 1) continue;
            try {
                StartHeader startHeader = new StartHeader();
                startHeader.nextHeaderOffset = pos - previousDataSize;
                startHeader.nextHeaderSize = this.channel.size() - pos;
                Archive result = this.initializeArchive(startHeader, password, false);
                if (result.packSizes.length <= 0 || result.files.length <= 0) continue;
                return result;
            } catch (Exception exception) {
            }
        }
        throw new IOException("Start header corrupt and unable to guess end header");
    }

    private Archive initializeArchive(StartHeader startHeader, byte[] password, boolean verifyCrc) throws IOException {
        SevenZFile.assertFitsIntoNonNegativeInt("nextHeaderSize", startHeader.nextHeaderSize);
        int nextHeaderSizeInt = (int)startHeader.nextHeaderSize;
        this.channel.position(32L + startHeader.nextHeaderOffset);
        ByteBuffer buf = ByteBuffer.allocate(nextHeaderSizeInt).order(ByteOrder.LITTLE_ENDIAN);
        this.readFully(buf);
        if (verifyCrc) {
            CRC32 crc = new CRC32();
            crc.update(buf.array());
            if (startHeader.nextHeaderCrc != crc.getValue()) {
                throw new IOException("NextHeader CRC mismatch");
            }
        }
        Archive archive = new Archive();
        int nid = SevenZFile.getUnsignedByte(buf);
        if (nid == 23) {
            buf = this.readEncodedHeader(buf, archive, password);
            archive = new Archive();
            nid = SevenZFile.getUnsignedByte(buf);
        }
        if (nid != 1) {
            throw new IOException("Broken or unsupported archive: no Header");
        }
        this.readHeader(buf, archive);
        archive.subStreamsInfo = null;
        return archive;
    }

    private StartHeader readStartHeader(long startHeaderCrc) throws IOException {
        StartHeader startHeader = new StartHeader();
        try (DataInputStream dataInputStream = new DataInputStream(new CRC32VerifyingInputStream((InputStream)new BoundedSeekableByteChannelInputStream(this.channel, 20L), 20L, startHeaderCrc));){
            startHeader.nextHeaderOffset = Long.reverseBytes(dataInputStream.readLong());
            if (startHeader.nextHeaderOffset < 0L || startHeader.nextHeaderOffset + 32L > this.channel.size()) {
                throw new IOException("nextHeaderOffset is out of bounds");
            }
            startHeader.nextHeaderSize = Long.reverseBytes(dataInputStream.readLong());
            long nextHeaderEnd = startHeader.nextHeaderOffset + startHeader.nextHeaderSize;
            if (nextHeaderEnd < startHeader.nextHeaderOffset || nextHeaderEnd + 32L > this.channel.size()) {
                throw new IOException("nextHeaderSize is out of bounds");
            }
            startHeader.nextHeaderCrc = 0xFFFFFFFFL & (long)Integer.reverseBytes(dataInputStream.readInt());
            StartHeader startHeader2 = startHeader;
            return startHeader2;
        }
    }

    private void readHeader(ByteBuffer header, Archive archive) throws IOException {
        int pos = header.position();
        ArchiveStatistics stats = this.sanityCheckAndCollectStatistics(header);
        stats.assertValidity(this.options.getMaxMemoryLimitInKb());
        header.position(pos);
        int nid = SevenZFile.getUnsignedByte(header);
        if (nid == 2) {
            this.readArchiveProperties(header);
            nid = SevenZFile.getUnsignedByte(header);
        }
        if (nid == 3) {
            throw new IOException("Additional streams unsupported");
        }
        if (nid == 4) {
            this.readStreamsInfo(header, archive);
            nid = SevenZFile.getUnsignedByte(header);
        }
        if (nid == 5) {
            this.readFilesInfo(header, archive);
            nid = SevenZFile.getUnsignedByte(header);
        }
    }

    private ArchiveStatistics sanityCheckAndCollectStatistics(ByteBuffer header) throws IOException {
        ArchiveStatistics stats = new ArchiveStatistics();
        int nid = SevenZFile.getUnsignedByte(header);
        if (nid == 2) {
            this.sanityCheckArchiveProperties(header);
            nid = SevenZFile.getUnsignedByte(header);
        }
        if (nid == 3) {
            throw new IOException("Additional streams unsupported");
        }
        if (nid == 4) {
            this.sanityCheckStreamsInfo(header, stats);
            nid = SevenZFile.getUnsignedByte(header);
        }
        if (nid == 5) {
            this.sanityCheckFilesInfo(header, stats);
            nid = SevenZFile.getUnsignedByte(header);
        }
        if (nid != 0) {
            throw new IOException("Badly terminated header, found " + nid);
        }
        return stats;
    }

    private void readArchiveProperties(ByteBuffer input) throws IOException {
        int nid = SevenZFile.getUnsignedByte(input);
        while (nid != 0) {
            long propertySize = SevenZFile.readUint64(input);
            byte[] property = new byte[(int)propertySize];
            SevenZFile.get(input, property);
            nid = SevenZFile.getUnsignedByte(input);
        }
    }

    private void sanityCheckArchiveProperties(ByteBuffer header) throws IOException {
        int nid = SevenZFile.getUnsignedByte(header);
        while (nid != 0) {
            int propertySize = SevenZFile.assertFitsIntoNonNegativeInt("propertySize", SevenZFile.readUint64(header));
            if (SevenZFile.skipBytesFully(header, propertySize) < (long)propertySize) {
                throw new IOException("invalid property size");
            }
            nid = SevenZFile.getUnsignedByte(header);
        }
    }

    private ByteBuffer readEncodedHeader(ByteBuffer header, Archive archive, byte[] password) throws IOException {
        int unpackSize;
        byte[] nextHeader;
        int pos = header.position();
        ArchiveStatistics stats = new ArchiveStatistics();
        this.sanityCheckStreamsInfo(header, stats);
        stats.assertValidity(this.options.getMaxMemoryLimitInKb());
        header.position(pos);
        this.readStreamsInfo(header, archive);
        if (archive.folders == null || archive.folders.length == 0) {
            throw new IOException("no folders, can't read encoded header");
        }
        if (archive.packSizes == null || archive.packSizes.length == 0) {
            throw new IOException("no packed streams, can't read encoded header");
        }
        Folder folder = archive.folders[0];
        boolean firstPackStreamIndex = false;
        long folderOffset = 32L + archive.packPos + 0L;
        this.channel.position(folderOffset);
        InputStream inputStreamStack = new BoundedSeekableByteChannelInputStream(this.channel, archive.packSizes[0]);
        for (Coder coder : folder.getOrderedCoders()) {
            if (coder.numInStreams != 1L || coder.numOutStreams != 1L) {
                throw new IOException("Multi input/output stream coders are not yet supported");
            }
            inputStreamStack = Coders.addDecoder(this.fileName, inputStreamStack, folder.getUnpackSizeForCoder(coder), coder, password, this.options.getMaxMemoryLimitInKb());
        }
        if (folder.hasCrc) {
            inputStreamStack = new CRC32VerifyingInputStream(inputStreamStack, folder.getUnpackSize(), folder.crc);
        }
        if ((nextHeader = IOUtils.readRange(inputStreamStack, unpackSize = SevenZFile.assertFitsIntoNonNegativeInt("unpackSize", folder.getUnpackSize()))).length < unpackSize) {
            throw new IOException("premature end of stream");
        }
        inputStreamStack.close();
        return ByteBuffer.wrap(nextHeader).order(ByteOrder.LITTLE_ENDIAN);
    }

    private void sanityCheckStreamsInfo(ByteBuffer header, ArchiveStatistics stats) throws IOException {
        int nid = SevenZFile.getUnsignedByte(header);
        if (nid == 6) {
            this.sanityCheckPackInfo(header, stats);
            nid = SevenZFile.getUnsignedByte(header);
        }
        if (nid == 7) {
            this.sanityCheckUnpackInfo(header, stats);
            nid = SevenZFile.getUnsignedByte(header);
        }
        if (nid == 8) {
            this.sanityCheckSubStreamsInfo(header, stats);
            nid = SevenZFile.getUnsignedByte(header);
        }
        if (nid != 0) {
            throw new IOException("Badly terminated StreamsInfo");
        }
    }

    private void readStreamsInfo(ByteBuffer header, Archive archive) throws IOException {
        int nid = SevenZFile.getUnsignedByte(header);
        if (nid == 6) {
            this.readPackInfo(header, archive);
            nid = SevenZFile.getUnsignedByte(header);
        }
        if (nid == 7) {
            this.readUnpackInfo(header, archive);
            nid = SevenZFile.getUnsignedByte(header);
        } else {
            archive.folders = Folder.EMPTY_FOLDER_ARRAY;
        }
        if (nid == 8) {
            this.readSubStreamsInfo(header, archive);
            nid = SevenZFile.getUnsignedByte(header);
        }
    }

    private void sanityCheckPackInfo(ByteBuffer header, ArchiveStatistics stats) throws IOException {
        long packPos = SevenZFile.readUint64(header);
        if (packPos < 0L || 32L + packPos > this.channel.size() || 32L + packPos < 0L) {
            throw new IOException("packPos (" + packPos + ") is out of range");
        }
        long numPackStreams = SevenZFile.readUint64(header);
        stats.numberOfPackedStreams = SevenZFile.assertFitsIntoNonNegativeInt("numPackStreams", numPackStreams);
        int nid = SevenZFile.getUnsignedByte(header);
        if (nid == 9) {
            long totalPackSizes = 0L;
            for (int i = 0; i < stats.numberOfPackedStreams; ++i) {
                long packSize = SevenZFile.readUint64(header);
                long endOfPackStreams = 32L + packPos + (totalPackSizes += packSize);
                if (packSize >= 0L && endOfPackStreams <= this.channel.size() && endOfPackStreams >= packPos) continue;
                throw new IOException("packSize (" + packSize + ") is out of range");
            }
            nid = SevenZFile.getUnsignedByte(header);
        }
        if (nid == 10) {
            int crcsDefined = this.readAllOrBits(header, stats.numberOfPackedStreams).cardinality();
            if (SevenZFile.skipBytesFully(header, 4 * crcsDefined) < (long)(4 * crcsDefined)) {
                throw new IOException("invalid number of CRCs in PackInfo");
            }
            nid = SevenZFile.getUnsignedByte(header);
        }
        if (nid != 0) {
            throw new IOException("Badly terminated PackInfo (" + nid + ")");
        }
    }

    private void readPackInfo(ByteBuffer header, Archive archive) throws IOException {
        int i;
        archive.packPos = SevenZFile.readUint64(header);
        int numPackStreamsInt = (int)SevenZFile.readUint64(header);
        int nid = SevenZFile.getUnsignedByte(header);
        if (nid == 9) {
            archive.packSizes = new long[numPackStreamsInt];
            for (i = 0; i < archive.packSizes.length; ++i) {
                archive.packSizes[i] = SevenZFile.readUint64(header);
            }
            nid = SevenZFile.getUnsignedByte(header);
        }
        if (nid == 10) {
            archive.packCrcsDefined = this.readAllOrBits(header, numPackStreamsInt);
            archive.packCrcs = new long[numPackStreamsInt];
            for (i = 0; i < numPackStreamsInt; ++i) {
                if (!archive.packCrcsDefined.get(i)) continue;
                archive.packCrcs[i] = 0xFFFFFFFFL & (long)SevenZFile.getInt(header);
            }
            nid = SevenZFile.getUnsignedByte(header);
        }
    }

    private void sanityCheckUnpackInfo(ByteBuffer header, ArchiveStatistics stats) throws IOException {
        int nid = SevenZFile.getUnsignedByte(header);
        if (nid != 11) {
            throw new IOException("Expected kFolder, got " + nid);
        }
        long numFolders = SevenZFile.readUint64(header);
        stats.numberOfFolders = SevenZFile.assertFitsIntoNonNegativeInt("numFolders", numFolders);
        int external = SevenZFile.getUnsignedByte(header);
        if (external != 0) {
            throw new IOException("External unsupported");
        }
        LinkedList<Integer> numberOfOutputStreamsPerFolder = new LinkedList<Integer>();
        for (int i = 0; i < stats.numberOfFolders; ++i) {
            numberOfOutputStreamsPerFolder.add(this.sanityCheckFolder(header, stats));
        }
        long totalNumberOfBindPairs = stats.numberOfOutStreams - (long)stats.numberOfFolders;
        long packedStreamsRequiredByFolders = stats.numberOfInStreams - totalNumberOfBindPairs;
        if (packedStreamsRequiredByFolders < (long)stats.numberOfPackedStreams) {
            throw new IOException("archive doesn't contain enough packed streams");
        }
        nid = SevenZFile.getUnsignedByte(header);
        if (nid != 12) {
            throw new IOException("Expected kCodersUnpackSize, got " + nid);
        }
        Iterator iterator = numberOfOutputStreamsPerFolder.iterator();
        while (iterator.hasNext()) {
            int numberOfOutputStreams = (Integer)iterator.next();
            for (int i = 0; i < numberOfOutputStreams; ++i) {
                long unpackSize = SevenZFile.readUint64(header);
                if (unpackSize >= 0L) continue;
                throw new IllegalArgumentException("negative unpackSize");
            }
        }
        nid = SevenZFile.getUnsignedByte(header);
        if (nid == 10) {
            stats.folderHasCrc = this.readAllOrBits(header, stats.numberOfFolders);
            int crcsDefined = stats.folderHasCrc.cardinality();
            if (SevenZFile.skipBytesFully(header, 4 * crcsDefined) < (long)(4 * crcsDefined)) {
                throw new IOException("invalid number of CRCs in UnpackInfo");
            }
            nid = SevenZFile.getUnsignedByte(header);
        }
        if (nid != 0) {
            throw new IOException("Badly terminated UnpackInfo");
        }
    }

    private void readUnpackInfo(ByteBuffer header, Archive archive) throws IOException {
        int nid = SevenZFile.getUnsignedByte(header);
        int numFoldersInt = (int)SevenZFile.readUint64(header);
        Folder[] folders = new Folder[numFoldersInt];
        archive.folders = folders;
        SevenZFile.getUnsignedByte(header);
        for (int i = 0; i < numFoldersInt; ++i) {
            folders[i] = this.readFolder(header);
        }
        nid = SevenZFile.getUnsignedByte(header);
        for (Folder folder : folders) {
            SevenZFile.assertFitsIntoNonNegativeInt("totalOutputStreams", folder.totalOutputStreams);
            folder.unpackSizes = new long[(int)folder.totalOutputStreams];
            int i = 0;
            while ((long)i < folder.totalOutputStreams) {
                folder.unpackSizes[i] = SevenZFile.readUint64(header);
                ++i;
            }
        }
        nid = SevenZFile.getUnsignedByte(header);
        if (nid == 10) {
            BitSet crcsDefined = this.readAllOrBits(header, numFoldersInt);
            for (int i = 0; i < numFoldersInt; ++i) {
                if (crcsDefined.get(i)) {
                    folders[i].hasCrc = true;
                    folders[i].crc = 0xFFFFFFFFL & (long)SevenZFile.getInt(header);
                    continue;
                }
                folders[i].hasCrc = false;
            }
            nid = SevenZFile.getUnsignedByte(header);
        }
    }

    private void sanityCheckSubStreamsInfo(ByteBuffer header, ArchiveStatistics stats) throws IOException {
        int nid = SevenZFile.getUnsignedByte(header);
        LinkedList<Integer> numUnpackSubStreamsPerFolder = new LinkedList<Integer>();
        if (nid == 13) {
            for (int i = 0; i < stats.numberOfFolders; ++i) {
                numUnpackSubStreamsPerFolder.add(SevenZFile.assertFitsIntoNonNegativeInt("numStreams", SevenZFile.readUint64(header)));
            }
            stats.numberOfUnpackSubStreams = numUnpackSubStreamsPerFolder.stream().collect(Collectors.summingLong(Integer::longValue));
            nid = SevenZFile.getUnsignedByte(header);
        } else {
            stats.numberOfUnpackSubStreams = stats.numberOfFolders;
        }
        SevenZFile.assertFitsIntoNonNegativeInt("totalUnpackStreams", stats.numberOfUnpackSubStreams);
        if (nid == 9) {
            Iterator i = numUnpackSubStreamsPerFolder.iterator();
            while (i.hasNext()) {
                int numUnpackSubStreams = (Integer)i.next();
                if (numUnpackSubStreams == 0) continue;
                for (int i2 = 0; i2 < numUnpackSubStreams - 1; ++i2) {
                    long size = SevenZFile.readUint64(header);
                    if (size >= 0L) continue;
                    throw new IOException("negative unpackSize");
                }
            }
            nid = SevenZFile.getUnsignedByte(header);
        }
        int numDigests = 0;
        if (numUnpackSubStreamsPerFolder.isEmpty()) {
            numDigests = stats.folderHasCrc == null ? stats.numberOfFolders : stats.numberOfFolders - stats.folderHasCrc.cardinality();
        } else {
            int folderIdx = 0;
            Iterator iterator = numUnpackSubStreamsPerFolder.iterator();
            while (iterator.hasNext()) {
                int numUnpackSubStreams = (Integer)iterator.next();
                if (numUnpackSubStreams == 1 && stats.folderHasCrc != null && stats.folderHasCrc.get(folderIdx++)) continue;
                numDigests += numUnpackSubStreams;
            }
        }
        if (nid == 10) {
            SevenZFile.assertFitsIntoNonNegativeInt("numDigests", numDigests);
            int missingCrcs = this.readAllOrBits(header, numDigests).cardinality();
            if (SevenZFile.skipBytesFully(header, 4 * missingCrcs) < (long)(4 * missingCrcs)) {
                throw new IOException("invalid number of missing CRCs in SubStreamInfo");
            }
            nid = SevenZFile.getUnsignedByte(header);
        }
        if (nid != 0) {
            throw new IOException("Badly terminated SubStreamsInfo");
        }
    }

    private void readSubStreamsInfo(ByteBuffer header, Archive archive) throws IOException {
        for (Folder folder : archive.folders) {
            folder.numUnpackSubStreams = 1;
        }
        long unpackStreamsCount = archive.folders.length;
        int nid = SevenZFile.getUnsignedByte(header);
        if (nid == 13) {
            unpackStreamsCount = 0L;
            for (Folder folder : archive.folders) {
                long numStreams = SevenZFile.readUint64(header);
                folder.numUnpackSubStreams = (int)numStreams;
                unpackStreamsCount += numStreams;
            }
            nid = SevenZFile.getUnsignedByte(header);
        }
        int n = (int)unpackStreamsCount;
        SubStreamsInfo subStreamsInfo = new SubStreamsInfo();
        subStreamsInfo.unpackSizes = new long[n];
        subStreamsInfo.hasCrc = new BitSet(n);
        subStreamsInfo.crcs = new long[n];
        int nextUnpackStream = 0;
        for (Folder folder : archive.folders) {
            if (folder.numUnpackSubStreams == 0) continue;
            long sum = 0L;
            if (nid == 9) {
                for (int i = 0; i < folder.numUnpackSubStreams - 1; ++i) {
                    long size = SevenZFile.readUint64(header);
                    subStreamsInfo.unpackSizes[nextUnpackStream++] = size;
                    sum += size;
                }
            }
            if (sum > folder.getUnpackSize()) {
                throw new IOException("sum of unpack sizes of folder exceeds total unpack size");
            }
            subStreamsInfo.unpackSizes[nextUnpackStream++] = folder.getUnpackSize() - sum;
        }
        if (nid == 9) {
            nid = SevenZFile.getUnsignedByte(header);
        }
        boolean bl = false;
        for (Folder folder : archive.folders) {
            if (folder.numUnpackSubStreams == 1 && folder.hasCrc) continue;
            n2 += folder.numUnpackSubStreams;
        }
        if (nid == 10) {
            int n2;
            BitSet hasMissingCrc = this.readAllOrBits(header, n2);
            long[] missingCrcs = new long[n2];
            for (int i = 0; i < n2; ++i) {
                if (!hasMissingCrc.get(i)) continue;
                missingCrcs[i] = 0xFFFFFFFFL & (long)SevenZFile.getInt(header);
            }
            int nextCrc = 0;
            int nextMissingCrc = 0;
            for (Folder folder : archive.folders) {
                if (folder.numUnpackSubStreams == 1 && folder.hasCrc) {
                    subStreamsInfo.hasCrc.set(nextCrc, true);
                    subStreamsInfo.crcs[nextCrc] = folder.crc;
                    ++nextCrc;
                    continue;
                }
                for (int i = 0; i < folder.numUnpackSubStreams; ++i) {
                    subStreamsInfo.hasCrc.set(nextCrc, hasMissingCrc.get(nextMissingCrc));
                    subStreamsInfo.crcs[nextCrc] = missingCrcs[nextMissingCrc];
                    ++nextCrc;
                    ++nextMissingCrc;
                }
            }
            nid = SevenZFile.getUnsignedByte(header);
        }
        archive.subStreamsInfo = subStreamsInfo;
    }

    private int sanityCheckFolder(ByteBuffer header, ArchiveStatistics stats) throws IOException {
        int numCoders = SevenZFile.assertFitsIntoNonNegativeInt("numCoders", SevenZFile.readUint64(header));
        if (numCoders == 0) {
            throw new IOException("Folder without coders");
        }
        stats.numberOfCoders = stats.numberOfCoders + (long)numCoders;
        long totalOutStreams = 0L;
        long totalInStreams = 0L;
        for (int i = 0; i < numCoders; ++i) {
            int propertiesSize;
            boolean moreAlternativeMethods;
            int bits = SevenZFile.getUnsignedByte(header);
            int idSize = bits & 0xF;
            SevenZFile.get(header, new byte[idSize]);
            boolean isSimple = (bits & 0x10) == 0;
            boolean hasAttributes = (bits & 0x20) != 0;
            boolean bl = moreAlternativeMethods = (bits & 0x80) != 0;
            if (moreAlternativeMethods) {
                throw new IOException("Alternative methods are unsupported, please report. The reference implementation doesn't support them either.");
            }
            if (isSimple) {
                ++totalInStreams;
                ++totalOutStreams;
            } else {
                totalInStreams += (long)SevenZFile.assertFitsIntoNonNegativeInt("numInStreams", SevenZFile.readUint64(header));
                totalOutStreams += (long)SevenZFile.assertFitsIntoNonNegativeInt("numOutStreams", SevenZFile.readUint64(header));
            }
            if (!hasAttributes || SevenZFile.skipBytesFully(header, propertiesSize = SevenZFile.assertFitsIntoNonNegativeInt("propertiesSize", SevenZFile.readUint64(header))) >= (long)propertiesSize) continue;
            throw new IOException("invalid propertiesSize in folder");
        }
        SevenZFile.assertFitsIntoNonNegativeInt("totalInStreams", totalInStreams);
        SevenZFile.assertFitsIntoNonNegativeInt("totalOutStreams", totalOutStreams);
        stats.numberOfOutStreams = stats.numberOfOutStreams + totalOutStreams;
        stats.numberOfInStreams = stats.numberOfInStreams + totalInStreams;
        if (totalOutStreams == 0L) {
            throw new IOException("Total output streams can't be 0");
        }
        int numBindPairs = SevenZFile.assertFitsIntoNonNegativeInt("numBindPairs", totalOutStreams - 1L);
        if (totalInStreams < (long)numBindPairs) {
            throw new IOException("Total input streams can't be less than the number of bind pairs");
        }
        BitSet inStreamsBound = new BitSet((int)totalInStreams);
        for (int i = 0; i < numBindPairs; ++i) {
            int inIndex = SevenZFile.assertFitsIntoNonNegativeInt("inIndex", SevenZFile.readUint64(header));
            if (totalInStreams <= (long)inIndex) {
                throw new IOException("inIndex is bigger than number of inStreams");
            }
            inStreamsBound.set(inIndex);
            int outIndex = SevenZFile.assertFitsIntoNonNegativeInt("outIndex", SevenZFile.readUint64(header));
            if (totalOutStreams > (long)outIndex) continue;
            throw new IOException("outIndex is bigger than number of outStreams");
        }
        int numPackedStreams = SevenZFile.assertFitsIntoNonNegativeInt("numPackedStreams", totalInStreams - (long)numBindPairs);
        if (numPackedStreams == 1) {
            if (inStreamsBound.nextClearBit(0) == -1) {
                throw new IOException("Couldn't find stream's bind pair index");
            }
        } else {
            for (int i = 0; i < numPackedStreams; ++i) {
                int packedStreamIndex = SevenZFile.assertFitsIntoNonNegativeInt("packedStreamIndex", SevenZFile.readUint64(header));
                if ((long)packedStreamIndex < totalInStreams) continue;
                throw new IOException("packedStreamIndex is bigger than number of totalInStreams");
            }
        }
        return (int)totalOutStreams;
    }

    private Folder readFolder(ByteBuffer header) throws IOException {
        Folder folder = new Folder();
        long numCoders = SevenZFile.readUint64(header);
        Coder[] coders = new Coder[(int)numCoders];
        long totalInStreams = 0L;
        long totalOutStreams = 0L;
        for (int i = 0; i < coders.length; ++i) {
            coders[i] = new Coder();
            int bits = SevenZFile.getUnsignedByte(header);
            int idSize = bits & 0xF;
            boolean isSimple = (bits & 0x10) == 0;
            boolean hasAttributes = (bits & 0x20) != 0;
            boolean moreAlternativeMethods = (bits & 0x80) != 0;
            coders[i].decompressionMethodId = new byte[idSize];
            SevenZFile.get(header, coders[i].decompressionMethodId);
            if (isSimple) {
                coders[i].numInStreams = 1L;
                coders[i].numOutStreams = 1L;
            } else {
                coders[i].numInStreams = SevenZFile.readUint64(header);
                coders[i].numOutStreams = SevenZFile.readUint64(header);
            }
            totalInStreams += coders[i].numInStreams;
            totalOutStreams += coders[i].numOutStreams;
            if (hasAttributes) {
                long propertiesSize = SevenZFile.readUint64(header);
                coders[i].properties = new byte[(int)propertiesSize];
                SevenZFile.get(header, coders[i].properties);
            }
            if (!moreAlternativeMethods) continue;
            throw new IOException("Alternative methods are unsupported, please report. The reference implementation doesn't support them either.");
        }
        folder.coders = coders;
        folder.totalInputStreams = totalInStreams;
        folder.totalOutputStreams = totalOutStreams;
        long numBindPairs = totalOutStreams - 1L;
        BindPair[] bindPairs = new BindPair[(int)numBindPairs];
        for (int i = 0; i < bindPairs.length; ++i) {
            bindPairs[i] = new BindPair();
            bindPairs[i].inIndex = SevenZFile.readUint64(header);
            bindPairs[i].outIndex = SevenZFile.readUint64(header);
        }
        folder.bindPairs = bindPairs;
        long numPackedStreams = totalInStreams - numBindPairs;
        long[] packedStreams = new long[(int)numPackedStreams];
        if (numPackedStreams == 1L) {
            int i;
            for (i = 0; i < (int)totalInStreams && folder.findBindPairForInStream(i) >= 0; ++i) {
            }
            packedStreams[0] = i;
        } else {
            for (int i = 0; i < (int)numPackedStreams; ++i) {
                packedStreams[i] = SevenZFile.readUint64(header);
            }
        }
        folder.packedStreams = packedStreams;
        return folder;
    }

    private BitSet readAllOrBits(ByteBuffer header, int size) throws IOException {
        BitSet bits;
        int areAllDefined = SevenZFile.getUnsignedByte(header);
        if (areAllDefined != 0) {
            bits = new BitSet(size);
            for (int i = 0; i < size; ++i) {
                bits.set(i, true);
            }
        } else {
            bits = this.readBits(header, size);
        }
        return bits;
    }

    private BitSet readBits(ByteBuffer header, int size) throws IOException {
        BitSet bits = new BitSet(size);
        int mask = 0;
        int cache = 0;
        for (int i = 0; i < size; ++i) {
            if (mask == 0) {
                mask = 128;
                cache = SevenZFile.getUnsignedByte(header);
            }
            bits.set(i, (cache & mask) != 0);
            mask >>>= 1;
        }
        return bits;
    }

    private void sanityCheckFilesInfo(ByteBuffer header, ArchiveStatistics stats) throws IOException {
        int propertyType;
        stats.numberOfEntries = SevenZFile.assertFitsIntoNonNegativeInt("numFiles", SevenZFile.readUint64(header));
        int emptyStreams = -1;
        block12: while ((propertyType = SevenZFile.getUnsignedByte(header)) != 0) {
            long size = SevenZFile.readUint64(header);
            switch (propertyType) {
                case 14: {
                    emptyStreams = this.readBits(header, stats.numberOfEntries).cardinality();
                    continue block12;
                }
                case 15: {
                    if (emptyStreams == -1) {
                        throw new IOException("Header format error: kEmptyStream must appear before kEmptyFile");
                    }
                    this.readBits(header, emptyStreams);
                    continue block12;
                }
                case 16: {
                    if (emptyStreams == -1) {
                        throw new IOException("Header format error: kEmptyStream must appear before kAnti");
                    }
                    this.readBits(header, emptyStreams);
                    continue block12;
                }
                case 17: {
                    int external = SevenZFile.getUnsignedByte(header);
                    if (external != 0) {
                        throw new IOException("Not implemented");
                    }
                    int namesLength = SevenZFile.assertFitsIntoNonNegativeInt("file names length", size - 1L);
                    if ((namesLength & 1) != 0) {
                        throw new IOException("File names length invalid");
                    }
                    int filesSeen = 0;
                    for (int i = 0; i < namesLength; i += 2) {
                        char c = SevenZFile.getChar(header);
                        if (c != '\u0000') continue;
                        ++filesSeen;
                    }
                    if (filesSeen == stats.numberOfEntries) continue block12;
                    throw new IOException("Invalid number of file names (" + filesSeen + " instead of " + stats.numberOfEntries + ")");
                }
                case 18: {
                    int timesDefined = this.readAllOrBits(header, stats.numberOfEntries).cardinality();
                    int external = SevenZFile.getUnsignedByte(header);
                    if (external != 0) {
                        throw new IOException("Not implemented");
                    }
                    if (SevenZFile.skipBytesFully(header, 8 * timesDefined) >= (long)(8 * timesDefined)) continue block12;
                    throw new IOException("invalid creation dates size");
                }
                case 19: {
                    int timesDefined = this.readAllOrBits(header, stats.numberOfEntries).cardinality();
                    int external = SevenZFile.getUnsignedByte(header);
                    if (external != 0) {
                        throw new IOException("Not implemented");
                    }
                    if (SevenZFile.skipBytesFully(header, 8 * timesDefined) >= (long)(8 * timesDefined)) continue block12;
                    throw new IOException("invalid access dates size");
                }
                case 20: {
                    int timesDefined = this.readAllOrBits(header, stats.numberOfEntries).cardinality();
                    int external = SevenZFile.getUnsignedByte(header);
                    if (external != 0) {
                        throw new IOException("Not implemented");
                    }
                    if (SevenZFile.skipBytesFully(header, 8 * timesDefined) >= (long)(8 * timesDefined)) continue block12;
                    throw new IOException("invalid modification dates size");
                }
                case 21: {
                    int attributesDefined = this.readAllOrBits(header, stats.numberOfEntries).cardinality();
                    int external = SevenZFile.getUnsignedByte(header);
                    if (external != 0) {
                        throw new IOException("Not implemented");
                    }
                    if (SevenZFile.skipBytesFully(header, 4 * attributesDefined) >= (long)(4 * attributesDefined)) continue block12;
                    throw new IOException("invalid windows attributes size");
                }
                case 24: {
                    throw new IOException("kStartPos is unsupported, please report");
                }
                case 25: {
                    if (SevenZFile.skipBytesFully(header, size) >= size) continue block12;
                    throw new IOException("Incomplete kDummy property");
                }
            }
            if (SevenZFile.skipBytesFully(header, size) >= size) continue;
            throw new IOException("Incomplete property of type " + propertyType);
        }
        stats.numberOfEntriesWithStream = stats.numberOfEntries - (emptyStreams > 0 ? emptyStreams : 0);
    }

    private void readFilesInfo(ByteBuffer header, Archive archive) throws IOException {
        int propertyType;
        int numFilesInt = (int)SevenZFile.readUint64(header);
        HashMap<Integer, SevenZArchiveEntry> fileMap = new HashMap<Integer, SevenZArchiveEntry>();
        BitSet isEmptyStream = null;
        BitSet isEmptyFile = null;
        BitSet isAnti = null;
        block11: while ((propertyType = SevenZFile.getUnsignedByte(header)) != 0) {
            long size = SevenZFile.readUint64(header);
            switch (propertyType) {
                case 14: {
                    isEmptyStream = this.readBits(header, numFilesInt);
                    break;
                }
                case 15: {
                    isEmptyFile = this.readBits(header, isEmptyStream.cardinality());
                    break;
                }
                case 16: {
                    isAnti = this.readBits(header, isEmptyStream.cardinality());
                    break;
                }
                case 17: {
                    SevenZFile.getUnsignedByte(header);
                    byte[] names = new byte[(int)(size - 1L)];
                    int namesLength = names.length;
                    SevenZFile.get(header, names);
                    int nextFile = 0;
                    int nextName = 0;
                    for (int i = 0; i < namesLength; i += 2) {
                        if (names[i] != 0 || names[i + 1] != 0) continue;
                        this.checkEntryIsInitialized(fileMap, nextFile);
                        ((SevenZArchiveEntry)fileMap.get(nextFile)).setName(new String(names, nextName, i - nextName, StandardCharsets.UTF_16LE));
                        nextName = i + 2;
                        ++nextFile;
                    }
                    if (nextName == namesLength && nextFile == numFilesInt) continue block11;
                    throw new IOException("Error parsing file names");
                }
                case 18: {
                    int i;
                    BitSet timesDefined = this.readAllOrBits(header, numFilesInt);
                    SevenZFile.getUnsignedByte(header);
                    for (i = 0; i < numFilesInt; ++i) {
                        this.checkEntryIsInitialized(fileMap, i);
                        SevenZArchiveEntry entryAtIndex = (SevenZArchiveEntry)fileMap.get(i);
                        entryAtIndex.setHasCreationDate(timesDefined.get(i));
                        if (!entryAtIndex.getHasCreationDate()) continue;
                        entryAtIndex.setCreationDate(SevenZFile.getLong(header));
                    }
                    continue block11;
                }
                case 19: {
                    int i;
                    BitSet timesDefined = this.readAllOrBits(header, numFilesInt);
                    SevenZFile.getUnsignedByte(header);
                    for (i = 0; i < numFilesInt; ++i) {
                        this.checkEntryIsInitialized(fileMap, i);
                        SevenZArchiveEntry entryAtIndex = (SevenZArchiveEntry)fileMap.get(i);
                        entryAtIndex.setHasAccessDate(timesDefined.get(i));
                        if (!entryAtIndex.getHasAccessDate()) continue;
                        entryAtIndex.setAccessDate(SevenZFile.getLong(header));
                    }
                    continue block11;
                }
                case 20: {
                    int i;
                    BitSet timesDefined = this.readAllOrBits(header, numFilesInt);
                    SevenZFile.getUnsignedByte(header);
                    for (i = 0; i < numFilesInt; ++i) {
                        this.checkEntryIsInitialized(fileMap, i);
                        SevenZArchiveEntry entryAtIndex = (SevenZArchiveEntry)fileMap.get(i);
                        entryAtIndex.setHasLastModifiedDate(timesDefined.get(i));
                        if (!entryAtIndex.getHasLastModifiedDate()) continue;
                        entryAtIndex.setLastModifiedDate(SevenZFile.getLong(header));
                    }
                    continue block11;
                }
                case 21: {
                    int i;
                    BitSet attributesDefined = this.readAllOrBits(header, numFilesInt);
                    SevenZFile.getUnsignedByte(header);
                    for (i = 0; i < numFilesInt; ++i) {
                        this.checkEntryIsInitialized(fileMap, i);
                        SevenZArchiveEntry entryAtIndex = (SevenZArchiveEntry)fileMap.get(i);
                        entryAtIndex.setHasWindowsAttributes(attributesDefined.get(i));
                        if (!entryAtIndex.getHasWindowsAttributes()) continue;
                        entryAtIndex.setWindowsAttributes(SevenZFile.getInt(header));
                    }
                    continue block11;
                }
                case 25: {
                    SevenZFile.skipBytesFully(header, size);
                    break;
                }
                default: {
                    SevenZFile.skipBytesFully(header, size);
                }
            }
        }
        int nonEmptyFileCounter = 0;
        int emptyFileCounter = 0;
        for (int i = 0; i < numFilesInt; ++i) {
            SevenZArchiveEntry entryAtIndex = (SevenZArchiveEntry)fileMap.get(i);
            if (entryAtIndex == null) continue;
            entryAtIndex.setHasStream(isEmptyStream == null || !isEmptyStream.get(i));
            if (entryAtIndex.hasStream()) {
                if (archive.subStreamsInfo == null) {
                    throw new IOException("Archive contains file with streams but no subStreamsInfo");
                }
                entryAtIndex.setDirectory(false);
                entryAtIndex.setAntiItem(false);
                entryAtIndex.setHasCrc(archive.subStreamsInfo.hasCrc.get(nonEmptyFileCounter));
                entryAtIndex.setCrcValue(archive.subStreamsInfo.crcs[nonEmptyFileCounter]);
                entryAtIndex.setSize(archive.subStreamsInfo.unpackSizes[nonEmptyFileCounter]);
                if (entryAtIndex.getSize() < 0L) {
                    throw new IOException("broken archive, entry with negative size");
                }
                ++nonEmptyFileCounter;
                continue;
            }
            entryAtIndex.setDirectory(isEmptyFile == null || !isEmptyFile.get(emptyFileCounter));
            entryAtIndex.setAntiItem(isAnti != null && isAnti.get(emptyFileCounter));
            entryAtIndex.setHasCrc(false);
            entryAtIndex.setSize(0L);
            ++emptyFileCounter;
        }
        ArrayList<SevenZArchiveEntry> entries = new ArrayList<SevenZArchiveEntry>();
        for (SevenZArchiveEntry e : fileMap.values()) {
            if (e == null) continue;
            entries.add(e);
        }
        archive.files = entries.toArray(SevenZArchiveEntry.EMPTY_SEVEN_Z_ARCHIVE_ENTRY_ARRAY);
        this.calculateStreamMap(archive);
    }

    private void checkEntryIsInitialized(Map<Integer, SevenZArchiveEntry> archiveEntries, int index) {
        if (archiveEntries.get(index) == null) {
            archiveEntries.put(index, new SevenZArchiveEntry());
        }
    }

    private void calculateStreamMap(Archive archive) throws IOException {
        StreamMap streamMap = new StreamMap();
        int nextFolderPackStreamIndex = 0;
        int numFolders = archive.folders != null ? archive.folders.length : 0;
        streamMap.folderFirstPackStreamIndex = new int[numFolders];
        for (int i = 0; i < numFolders; ++i) {
            streamMap.folderFirstPackStreamIndex[i] = nextFolderPackStreamIndex;
            nextFolderPackStreamIndex += archive.folders[i].packedStreams.length;
        }
        long nextPackStreamOffset = 0L;
        int numPackSizes = archive.packSizes.length;
        streamMap.packStreamOffsets = new long[numPackSizes];
        for (int i = 0; i < numPackSizes; ++i) {
            streamMap.packStreamOffsets[i] = nextPackStreamOffset;
            nextPackStreamOffset += archive.packSizes[i];
        }
        streamMap.folderFirstFileIndex = new int[numFolders];
        streamMap.fileFolderIndex = new int[archive.files.length];
        int nextFolderIndex = 0;
        int nextFolderUnpackStreamIndex = 0;
        for (int i = 0; i < archive.files.length; ++i) {
            if (!archive.files[i].hasStream() && nextFolderUnpackStreamIndex == 0) {
                streamMap.fileFolderIndex[i] = -1;
                continue;
            }
            if (nextFolderUnpackStreamIndex == 0) {
                while (nextFolderIndex < archive.folders.length) {
                    streamMap.folderFirstFileIndex[nextFolderIndex] = i;
                    if (archive.folders[nextFolderIndex].numUnpackSubStreams > 0) break;
                    ++nextFolderIndex;
                }
                if (nextFolderIndex >= archive.folders.length) {
                    throw new IOException("Too few folders in archive");
                }
            }
            streamMap.fileFolderIndex[i] = nextFolderIndex;
            if (!archive.files[i].hasStream() || ++nextFolderUnpackStreamIndex < archive.folders[nextFolderIndex].numUnpackSubStreams) continue;
            ++nextFolderIndex;
            nextFolderUnpackStreamIndex = 0;
        }
        archive.streamMap = streamMap;
    }

    private void buildDecodingStream(int entryIndex, boolean isRandomAccess) throws IOException {
        if (this.archive.streamMap == null) {
            throw new IOException("Archive doesn't contain stream information to read entries");
        }
        int folderIndex = this.archive.streamMap.fileFolderIndex[entryIndex];
        if (folderIndex < 0) {
            this.deferredBlockStreams.clear();
            return;
        }
        SevenZArchiveEntry file = this.archive.files[entryIndex];
        boolean isInSameFolder = false;
        if (this.currentFolderIndex == folderIndex) {
            if (entryIndex > 0) {
                file.setContentMethods(this.archive.files[entryIndex - 1].getContentMethods());
            }
            if (isRandomAccess && file.getContentMethods() == null) {
                int folderFirstFileIndex = this.archive.streamMap.folderFirstFileIndex[folderIndex];
                SevenZArchiveEntry folderFirstFile = this.archive.files[folderFirstFileIndex];
                file.setContentMethods(folderFirstFile.getContentMethods());
            }
            isInSameFolder = true;
        } else {
            this.currentFolderIndex = folderIndex;
            this.reopenFolderInputStream(folderIndex, file);
        }
        boolean haveSkippedEntries = false;
        if (isRandomAccess) {
            haveSkippedEntries = this.skipEntriesWhenNeeded(entryIndex, isInSameFolder, folderIndex);
        }
        if (isRandomAccess && this.currentEntryIndex == entryIndex && !haveSkippedEntries) {
            return;
        }
        InputStream fileStream = new BoundedInputStream(this.currentFolderInputStream, file.getSize());
        if (file.getHasCrc()) {
            fileStream = new CRC32VerifyingInputStream(fileStream, file.getSize(), file.getCrcValue());
        }
        this.deferredBlockStreams.add(fileStream);
    }

    private void reopenFolderInputStream(int folderIndex, SevenZArchiveEntry file) throws IOException {
        this.deferredBlockStreams.clear();
        if (this.currentFolderInputStream != null) {
            this.currentFolderInputStream.close();
            this.currentFolderInputStream = null;
        }
        Folder folder = this.archive.folders[folderIndex];
        int firstPackStreamIndex = this.archive.streamMap.folderFirstPackStreamIndex[folderIndex];
        long folderOffset = 32L + this.archive.packPos + this.archive.streamMap.packStreamOffsets[firstPackStreamIndex];
        this.currentFolderInputStream = this.buildDecoderStack(folder, folderOffset, firstPackStreamIndex, file);
    }

    private boolean skipEntriesWhenNeeded(int entryIndex, boolean isInSameFolder, int folderIndex) throws IOException {
        SevenZArchiveEntry file = this.archive.files[entryIndex];
        if (this.currentEntryIndex == entryIndex && !this.hasCurrentEntryBeenRead()) {
            return false;
        }
        int filesToSkipStartIndex = this.archive.streamMap.folderFirstFileIndex[this.currentFolderIndex];
        if (isInSameFolder) {
            if (this.currentEntryIndex < entryIndex) {
                filesToSkipStartIndex = this.currentEntryIndex + 1;
            } else {
                this.reopenFolderInputStream(folderIndex, file);
            }
        }
        for (int i = filesToSkipStartIndex; i < entryIndex; ++i) {
            SevenZArchiveEntry fileToSkip = this.archive.files[i];
            InputStream fileStreamToSkip = new BoundedInputStream(this.currentFolderInputStream, fileToSkip.getSize());
            if (fileToSkip.getHasCrc()) {
                fileStreamToSkip = new CRC32VerifyingInputStream(fileStreamToSkip, fileToSkip.getSize(), fileToSkip.getCrcValue());
            }
            this.deferredBlockStreams.add(fileStreamToSkip);
            fileToSkip.setContentMethods(file.getContentMethods());
        }
        return true;
    }

    private boolean hasCurrentEntryBeenRead() {
        boolean hasCurrentEntryBeenRead = false;
        if (!this.deferredBlockStreams.isEmpty()) {
            InputStream currentEntryInputStream = this.deferredBlockStreams.get(this.deferredBlockStreams.size() - 1);
            if (currentEntryInputStream instanceof CRC32VerifyingInputStream) {
                boolean bl = hasCurrentEntryBeenRead = ((CRC32VerifyingInputStream)currentEntryInputStream).getBytesRemaining() != this.archive.files[this.currentEntryIndex].getSize();
            }
            if (currentEntryInputStream instanceof BoundedInputStream) {
                hasCurrentEntryBeenRead = ((BoundedInputStream)currentEntryInputStream).getBytesRemaining() != this.archive.files[this.currentEntryIndex].getSize();
            }
        }
        return hasCurrentEntryBeenRead;
    }

    private InputStream buildDecoderStack(Folder folder, long folderOffset, int firstPackStreamIndex, SevenZArchiveEntry entry) throws IOException {
        this.channel.position(folderOffset);
        InputStream inputStreamStack = new FilterInputStream(new BufferedInputStream(new BoundedSeekableByteChannelInputStream(this.channel, this.archive.packSizes[firstPackStreamIndex]))){

            @Override
            public int read() throws IOException {
                int r = this.in.read();
                if (r >= 0) {
                    this.count(1);
                }
                return r;
            }

            @Override
            public int read(byte[] b) throws IOException {
                return this.read(b, 0, b.length);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (len == 0) {
                    return 0;
                }
                int r = this.in.read(b, off, len);
                if (r >= 0) {
                    this.count(r);
                }
                return r;
            }

            private void count(int c) {
                SevenZFile.this.compressedBytesReadFromCurrentEntry = SevenZFile.this.compressedBytesReadFromCurrentEntry + (long)c;
            }
        };
        LinkedList<SevenZMethodConfiguration> methods = new LinkedList<SevenZMethodConfiguration>();
        for (Coder coder : folder.getOrderedCoders()) {
            if (coder.numInStreams != 1L || coder.numOutStreams != 1L) {
                throw new IOException("Multi input/output stream coders are not yet supported");
            }
            SevenZMethod method = SevenZMethod.byId(coder.decompressionMethodId);
            inputStreamStack = Coders.addDecoder(this.fileName, inputStreamStack, folder.getUnpackSizeForCoder(coder), coder, this.password, this.options.getMaxMemoryLimitInKb());
            methods.addFirst(new SevenZMethodConfiguration(method, Coders.findByMethod(method).getOptionsFromCoder(coder, inputStreamStack)));
        }
        entry.setContentMethods(methods);
        if (folder.hasCrc) {
            return new CRC32VerifyingInputStream(inputStreamStack, folder.getUnpackSize(), folder.crc);
        }
        return inputStreamStack;
    }

    public int read() throws IOException {
        int b = this.getCurrentStream().read();
        if (b >= 0) {
            ++this.uncompressedBytesReadFromCurrentEntry;
        }
        return b;
    }

    private InputStream getCurrentStream() throws IOException {
        if (this.archive.files[this.currentEntryIndex].getSize() == 0L) {
            return new ByteArrayInputStream(ByteUtils.EMPTY_BYTE_ARRAY);
        }
        if (this.deferredBlockStreams.isEmpty()) {
            throw new IllegalStateException("No current 7z entry (call getNextEntry() first).");
        }
        while (this.deferredBlockStreams.size() > 1) {
            try (InputStream stream = this.deferredBlockStreams.remove(0);){
                IOUtils.skip(stream, Long.MAX_VALUE);
            }
            this.compressedBytesReadFromCurrentEntry = 0L;
        }
        return this.deferredBlockStreams.get(0);
    }

    public InputStream getInputStream(SevenZArchiveEntry entry) throws IOException {
        int entryIndex = -1;
        for (int i = 0; i < this.archive.files.length; ++i) {
            if (entry != this.archive.files[i]) continue;
            entryIndex = i;
            break;
        }
        if (entryIndex < 0) {
            throw new IllegalArgumentException("Can not find " + entry.getName() + " in " + this.fileName);
        }
        this.buildDecodingStream(entryIndex, true);
        this.currentEntryIndex = entryIndex;
        this.currentFolderIndex = this.archive.streamMap.fileFolderIndex[entryIndex];
        return this.getCurrentStream();
    }

    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        int cnt = this.getCurrentStream().read(b, off, len);
        if (cnt > 0) {
            this.uncompressedBytesReadFromCurrentEntry += (long)cnt;
        }
        return cnt;
    }

    public InputStreamStatistics getStatisticsForCurrentEntry() {
        return new InputStreamStatistics(){

            @Override
            public long getCompressedCount() {
                return SevenZFile.this.compressedBytesReadFromCurrentEntry;
            }

            @Override
            public long getUncompressedCount() {
                return SevenZFile.this.uncompressedBytesReadFromCurrentEntry;
            }
        };
    }

    private static long readUint64(ByteBuffer in) throws IOException {
        long firstByte = SevenZFile.getUnsignedByte(in);
        int mask = 128;
        long value = 0L;
        for (int i = 0; i < 8; ++i) {
            if ((firstByte & (long)mask) == 0L) {
                return value | (firstByte & (long)(mask - 1)) << 8 * i;
            }
            long nextByte = SevenZFile.getUnsignedByte(in);
            value |= nextByte << 8 * i;
            mask >>>= 1;
        }
        return value;
    }

    private static char getChar(ByteBuffer buf) throws IOException {
        if (buf.remaining() < 2) {
            throw new EOFException();
        }
        return buf.getChar();
    }

    private static int getInt(ByteBuffer buf) throws IOException {
        if (buf.remaining() < 4) {
            throw new EOFException();
        }
        return buf.getInt();
    }

    private static long getLong(ByteBuffer buf) throws IOException {
        if (buf.remaining() < 8) {
            throw new EOFException();
        }
        return buf.getLong();
    }

    private static void get(ByteBuffer buf, byte[] to) throws IOException {
        if (buf.remaining() < to.length) {
            throw new EOFException();
        }
        buf.get(to);
    }

    private static int getUnsignedByte(ByteBuffer buf) throws IOException {
        if (!buf.hasRemaining()) {
            throw new EOFException();
        }
        return buf.get() & 0xFF;
    }

    public static boolean matches(byte[] signature, int length) {
        if (length < sevenZSignature.length) {
            return false;
        }
        for (int i = 0; i < sevenZSignature.length; ++i) {
            if (signature[i] == sevenZSignature[i]) continue;
            return false;
        }
        return true;
    }

    private static long skipBytesFully(ByteBuffer input, long bytesToSkip) throws IOException {
        if (bytesToSkip < 1L) {
            return 0L;
        }
        int current = input.position();
        int maxSkip = input.remaining();
        if ((long)maxSkip < bytesToSkip) {
            bytesToSkip = maxSkip;
        }
        input.position(current + (int)bytesToSkip);
        return bytesToSkip;
    }

    private void readFully(ByteBuffer buf) throws IOException {
        buf.rewind();
        IOUtils.readFully(this.channel, buf);
        buf.flip();
    }

    public String toString() {
        return this.archive.toString();
    }

    public String getDefaultName() {
        if (DEFAULT_FILE_NAME.equals(this.fileName) || this.fileName == null) {
            return null;
        }
        String lastSegment = new File(this.fileName).getName();
        int dotPos = lastSegment.lastIndexOf(".");
        if (dotPos > 0) {
            return lastSegment.substring(0, dotPos);
        }
        return lastSegment + "~";
    }

    private static byte[] utf16Decode(char[] chars) throws IOException {
        if (chars == null) {
            return null;
        }
        ByteBuffer encoded = PASSWORD_ENCODER.encode(CharBuffer.wrap(chars));
        if (encoded.hasArray()) {
            return encoded.array();
        }
        byte[] e = new byte[encoded.remaining()];
        encoded.get(e);
        return e;
    }

    private static int assertFitsIntoNonNegativeInt(String what, long value) throws IOException {
        if (value > Integer.MAX_VALUE || value < 0L) {
            throw new IOException("Cannot handle " + what + " " + value);
        }
        return (int)value;
    }

    private static class ArchiveStatistics {
        private int numberOfPackedStreams;
        private long numberOfCoders;
        private long numberOfOutStreams;
        private long numberOfInStreams;
        private long numberOfUnpackSubStreams;
        private int numberOfFolders;
        private BitSet folderHasCrc;
        private int numberOfEntries;
        private int numberOfEntriesWithStream;

        private ArchiveStatistics() {
        }

        public String toString() {
            return "Archive with " + this.numberOfEntries + " entries in " + this.numberOfFolders + " folders. Estimated size " + this.estimateSize() / 1024L + " kB.";
        }

        long estimateSize() {
            long lowerBound = 16L * (long)this.numberOfPackedStreams + (long)(this.numberOfPackedStreams / 8) + (long)this.numberOfFolders * this.folderSize() + this.numberOfCoders * this.coderSize() + (this.numberOfOutStreams - (long)this.numberOfFolders) * this.bindPairSize() + 8L * (this.numberOfInStreams - this.numberOfOutStreams + (long)this.numberOfFolders) + 8L * this.numberOfOutStreams + (long)this.numberOfEntries * this.entrySize() + this.streamMapSize();
            return 2L * lowerBound;
        }

        void assertValidity(int maxMemoryLimitInKb) throws IOException {
            if (this.numberOfEntriesWithStream > 0 && this.numberOfFolders == 0) {
                throw new IOException("archive with entries but no folders");
            }
            if ((long)this.numberOfEntriesWithStream > this.numberOfUnpackSubStreams) {
                throw new IOException("archive doesn't contain enough substreams for entries");
            }
            long memoryNeededInKb = this.estimateSize() / 1024L;
            if ((long)maxMemoryLimitInKb < memoryNeededInKb) {
                throw new MemoryLimitException(memoryNeededInKb, maxMemoryLimitInKb);
            }
        }

        private long folderSize() {
            return 30L;
        }

        private long coderSize() {
            return 22L;
        }

        private long bindPairSize() {
            return 16L;
        }

        private long entrySize() {
            return 100L;
        }

        private long streamMapSize() {
            return 8 * this.numberOfFolders + 8 * this.numberOfPackedStreams + 4 * this.numberOfEntries;
        }
    }
}

