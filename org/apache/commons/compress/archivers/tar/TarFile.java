/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.archivers.tar;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveSparseEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveSparseZeroInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveStructSparse;
import org.apache.commons.compress.archivers.tar.TarUtils;
import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.apache.commons.compress.utils.ArchiveUtils;
import org.apache.commons.compress.utils.BoundedArchiveInputStream;
import org.apache.commons.compress.utils.BoundedInputStream;
import org.apache.commons.compress.utils.BoundedSeekableByteChannelInputStream;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

public class TarFile
implements Closeable {
    private static final int SMALL_BUFFER_SIZE = 256;
    private final byte[] smallBuf = new byte[256];
    private final SeekableByteChannel archive;
    private final ZipEncoding zipEncoding;
    private final LinkedList<TarArchiveEntry> entries = new LinkedList();
    private final int blockSize;
    private final boolean lenient;
    private final int recordSize;
    private final ByteBuffer recordBuffer;
    private final List<TarArchiveStructSparse> globalSparseHeaders = new ArrayList<TarArchiveStructSparse>();
    private boolean hasHitEOF;
    private TarArchiveEntry currEntry;
    private Map<String, String> globalPaxHeaders = new HashMap<String, String>();
    private final Map<String, List<InputStream>> sparseInputStreams = new HashMap<String, List<InputStream>>();

    public TarFile(byte[] content) throws IOException {
        this(new SeekableInMemoryByteChannel(content));
    }

    public TarFile(byte[] content, String encoding) throws IOException {
        this(new SeekableInMemoryByteChannel(content), 10240, 512, encoding, false);
    }

    public TarFile(byte[] content, boolean lenient) throws IOException {
        this(new SeekableInMemoryByteChannel(content), 10240, 512, null, lenient);
    }

    public TarFile(File archive) throws IOException {
        this(archive.toPath());
    }

    public TarFile(File archive, String encoding) throws IOException {
        this(archive.toPath(), encoding);
    }

    public TarFile(File archive, boolean lenient) throws IOException {
        this(archive.toPath(), lenient);
    }

    public TarFile(Path archivePath) throws IOException {
        this(Files.newByteChannel(archivePath, new OpenOption[0]), 10240, 512, null, false);
    }

    public TarFile(Path archivePath, String encoding) throws IOException {
        this(Files.newByteChannel(archivePath, new OpenOption[0]), 10240, 512, encoding, false);
    }

    public TarFile(Path archivePath, boolean lenient) throws IOException {
        this(Files.newByteChannel(archivePath, new OpenOption[0]), 10240, 512, null, lenient);
    }

    public TarFile(SeekableByteChannel content) throws IOException {
        this(content, 10240, 512, null, false);
    }

    public TarFile(SeekableByteChannel archive, int blockSize, int recordSize, String encoding, boolean lenient) throws IOException {
        TarArchiveEntry entry;
        this.archive = archive;
        this.hasHitEOF = false;
        this.zipEncoding = ZipEncodingHelper.getZipEncoding(encoding);
        this.recordSize = recordSize;
        this.recordBuffer = ByteBuffer.allocate(this.recordSize);
        this.blockSize = blockSize;
        this.lenient = lenient;
        while ((entry = this.getNextTarEntry()) != null) {
            this.entries.add(entry);
        }
    }

    private TarArchiveEntry getNextTarEntry() throws IOException {
        ByteBuffer headerBuf;
        if (this.isAtEOF()) {
            return null;
        }
        if (this.currEntry != null) {
            this.repositionForwardTo(this.currEntry.getDataOffset() + this.currEntry.getSize());
            this.throwExceptionIfPositionIsNotInArchive();
            this.skipRecordPadding();
        }
        if (null == (headerBuf = this.getRecord())) {
            this.currEntry = null;
            return null;
        }
        try {
            this.currEntry = new TarArchiveEntry(headerBuf.array(), this.zipEncoding, this.lenient, this.archive.position());
        } catch (IllegalArgumentException e) {
            throw new IOException("Error detected parsing the header", e);
        }
        if (this.currEntry.isGNULongLinkEntry()) {
            byte[] longLinkData = this.getLongNameData();
            if (longLinkData == null) {
                return null;
            }
            this.currEntry.setLinkName(this.zipEncoding.decode(longLinkData));
        }
        if (this.currEntry.isGNULongNameEntry()) {
            byte[] longNameData = this.getLongNameData();
            if (longNameData == null) {
                return null;
            }
            String name = this.zipEncoding.decode(longNameData);
            this.currEntry.setName(name);
            if (this.currEntry.isDirectory() && !name.endsWith("/")) {
                this.currEntry.setName(name + "/");
            }
        }
        if (this.currEntry.isGlobalPaxHeader()) {
            this.readGlobalPaxHeaders();
        }
        try {
            if (this.currEntry.isPaxHeader()) {
                this.paxHeaders();
            } else if (!this.globalPaxHeaders.isEmpty()) {
                this.applyPaxHeadersToCurrentEntry(this.globalPaxHeaders, this.globalSparseHeaders);
            }
        } catch (NumberFormatException e) {
            throw new IOException("Error detected parsing the pax header", e);
        }
        if (this.currEntry.isOldGNUSparse()) {
            this.readOldGNUSparse();
        }
        return this.currEntry;
    }

    private void readOldGNUSparse() throws IOException {
        if (this.currEntry.isExtended()) {
            TarArchiveSparseEntry entry;
            do {
                ByteBuffer headerBuf;
                if ((headerBuf = this.getRecord()) == null) {
                    throw new IOException("premature end of tar archive. Didn't find extended_header after header with extended flag.");
                }
                entry = new TarArchiveSparseEntry(headerBuf.array());
                this.currEntry.getSparseHeaders().addAll(entry.getSparseHeaders());
                this.currEntry.setDataOffset(this.currEntry.getDataOffset() + (long)this.recordSize);
            } while (entry.isExtended());
        }
        this.buildSparseInputStreams();
    }

    private void buildSparseInputStreams() throws IOException {
        ArrayList<InputStream> streams = new ArrayList<InputStream>();
        List<TarArchiveStructSparse> sparseHeaders = this.currEntry.getOrderedSparseHeaders();
        TarArchiveSparseZeroInputStream zeroInputStream = new TarArchiveSparseZeroInputStream();
        long offset = 0L;
        long numberOfZeroBytesInSparseEntry = 0L;
        for (TarArchiveStructSparse sparseHeader : sparseHeaders) {
            long zeroBlockSize = sparseHeader.getOffset() - offset;
            if (zeroBlockSize < 0L) {
                throw new IOException("Corrupted struct sparse detected");
            }
            if (zeroBlockSize > 0L) {
                streams.add(new BoundedInputStream(zeroInputStream, zeroBlockSize));
                numberOfZeroBytesInSparseEntry += zeroBlockSize;
            }
            if (sparseHeader.getNumbytes() > 0L) {
                long start = this.currEntry.getDataOffset() + sparseHeader.getOffset() - numberOfZeroBytesInSparseEntry;
                if (start + sparseHeader.getNumbytes() < start) {
                    throw new IOException("Unreadable TAR archive, sparse block offset or length too big");
                }
                streams.add(new BoundedSeekableByteChannelInputStream(start, sparseHeader.getNumbytes(), this.archive));
            }
            offset = sparseHeader.getOffset() + sparseHeader.getNumbytes();
        }
        this.sparseInputStreams.put(this.currEntry.getName(), streams);
    }

    private void applyPaxHeadersToCurrentEntry(Map<String, String> headers, List<TarArchiveStructSparse> sparseHeaders) throws IOException {
        this.currEntry.updateEntryFromPaxHeaders(headers);
        this.currEntry.setSparseHeaders(sparseHeaders);
    }

    private void paxHeaders() throws IOException {
        Map<String, String> headers;
        List<TarArchiveStructSparse> sparseHeaders = new ArrayList<TarArchiveStructSparse>();
        try (InputStream input = this.getInputStream(this.currEntry);){
            headers = TarUtils.parsePaxHeaders(input, sparseHeaders, this.globalPaxHeaders, this.currEntry.getSize());
        }
        if (headers.containsKey("GNU.sparse.map")) {
            sparseHeaders = new ArrayList<TarArchiveStructSparse>(TarUtils.parseFromPAX01SparseHeaders(headers.get("GNU.sparse.map")));
        }
        this.getNextTarEntry();
        if (this.currEntry == null) {
            throw new IOException("premature end of tar archive. Didn't find any entry after PAX header.");
        }
        this.applyPaxHeadersToCurrentEntry(headers, sparseHeaders);
        if (this.currEntry.isPaxGNU1XSparse()) {
            input = this.getInputStream(this.currEntry);
            var4_3 = null;
            try {
                sparseHeaders = TarUtils.parsePAX1XSparseHeaders(input, this.recordSize);
            } catch (Throwable throwable) {
                var4_3 = throwable;
                throw throwable;
            } finally {
                if (input != null) {
                    if (var4_3 != null) {
                        try {
                            input.close();
                        } catch (Throwable throwable) {
                            var4_3.addSuppressed(throwable);
                        }
                    } else {
                        input.close();
                    }
                }
            }
            this.currEntry.setSparseHeaders(sparseHeaders);
            this.currEntry.setDataOffset(this.currEntry.getDataOffset() + (long)this.recordSize);
        }
        this.buildSparseInputStreams();
    }

    private void readGlobalPaxHeaders() throws IOException {
        try (InputStream input = this.getInputStream(this.currEntry);){
            this.globalPaxHeaders = TarUtils.parsePaxHeaders(input, this.globalSparseHeaders, this.globalPaxHeaders, this.currEntry.getSize());
        }
        this.getNextTarEntry();
        if (this.currEntry == null) {
            throw new IOException("Error detected parsing the pax header");
        }
    }

    private byte[] getLongNameData() throws IOException {
        int length;
        ByteArrayOutputStream longName = new ByteArrayOutputStream();
        try (InputStream in = this.getInputStream(this.currEntry);){
            while ((length = in.read(this.smallBuf)) >= 0) {
                longName.write(this.smallBuf, 0, length);
            }
        }
        this.getNextTarEntry();
        if (this.currEntry == null) {
            return null;
        }
        byte[] longNameData = longName.toByteArray();
        for (length = longNameData.length; length > 0 && longNameData[length - 1] == 0; --length) {
        }
        if (length != longNameData.length) {
            byte[] l = new byte[length];
            System.arraycopy(longNameData, 0, l, 0, length);
            longNameData = l;
        }
        return longNameData;
    }

    private void skipRecordPadding() throws IOException {
        if (!this.isDirectory() && this.currEntry.getSize() > 0L && this.currEntry.getSize() % (long)this.recordSize != 0L) {
            long numRecords = this.currEntry.getSize() / (long)this.recordSize + 1L;
            long padding = numRecords * (long)this.recordSize - this.currEntry.getSize();
            this.repositionForwardBy(padding);
            this.throwExceptionIfPositionIsNotInArchive();
        }
    }

    private void repositionForwardTo(long newPosition) throws IOException {
        long currPosition = this.archive.position();
        if (newPosition < currPosition) {
            throw new IOException("trying to move backwards inside of the archive");
        }
        this.archive.position(newPosition);
    }

    private void repositionForwardBy(long offset) throws IOException {
        this.repositionForwardTo(this.archive.position() + offset);
    }

    private void throwExceptionIfPositionIsNotInArchive() throws IOException {
        if (this.archive.size() < this.archive.position()) {
            throw new IOException("Truncated TAR archive");
        }
    }

    private ByteBuffer getRecord() throws IOException {
        ByteBuffer headerBuf = this.readRecord();
        this.setAtEOF(this.isEOFRecord(headerBuf));
        if (this.isAtEOF() && headerBuf != null) {
            this.tryToConsumeSecondEOFRecord();
            this.consumeRemainderOfLastBlock();
            headerBuf = null;
        }
        return headerBuf;
    }

    private void tryToConsumeSecondEOFRecord() throws IOException {
        boolean shouldReset = true;
        try {
            shouldReset = !this.isEOFRecord(this.readRecord());
        } finally {
            if (shouldReset) {
                this.archive.position(this.archive.position() - (long)this.recordSize);
            }
        }
    }

    private void consumeRemainderOfLastBlock() throws IOException {
        long bytesReadOfLastBlock = this.archive.position() % (long)this.blockSize;
        if (bytesReadOfLastBlock > 0L) {
            this.repositionForwardBy((long)this.blockSize - bytesReadOfLastBlock);
        }
    }

    private ByteBuffer readRecord() throws IOException {
        this.recordBuffer.rewind();
        int readNow = this.archive.read(this.recordBuffer);
        if (readNow != this.recordSize) {
            return null;
        }
        return this.recordBuffer;
    }

    public List<TarArchiveEntry> getEntries() {
        return new ArrayList<TarArchiveEntry>(this.entries);
    }

    private boolean isEOFRecord(ByteBuffer headerBuf) {
        return headerBuf == null || ArchiveUtils.isArrayZero(headerBuf.array(), this.recordSize);
    }

    protected final boolean isAtEOF() {
        return this.hasHitEOF;
    }

    protected final void setAtEOF(boolean b) {
        this.hasHitEOF = b;
    }

    private boolean isDirectory() {
        return this.currEntry != null && this.currEntry.isDirectory();
    }

    public InputStream getInputStream(TarArchiveEntry entry) throws IOException {
        try {
            return new BoundedTarEntryInputStream(entry, this.archive);
        } catch (RuntimeException ex) {
            throw new IOException("Corrupted TAR archive. Can't read entry", ex);
        }
    }

    @Override
    public void close() throws IOException {
        this.archive.close();
    }

    private final class BoundedTarEntryInputStream
    extends BoundedArchiveInputStream {
        private final SeekableByteChannel channel;
        private final TarArchiveEntry entry;
        private long entryOffset;
        private int currentSparseInputStreamIndex;

        BoundedTarEntryInputStream(TarArchiveEntry entry, SeekableByteChannel channel) throws IOException {
            super(entry.getDataOffset(), entry.getRealSize());
            if (channel.size() - entry.getSize() < entry.getDataOffset()) {
                throw new IOException("entry size exceeds archive size");
            }
            this.entry = entry;
            this.channel = channel;
        }

        @Override
        protected int read(long pos, ByteBuffer buf) throws IOException {
            if (this.entryOffset >= this.entry.getRealSize()) {
                return -1;
            }
            int totalRead = this.entry.isSparse() ? this.readSparse(this.entryOffset, buf, buf.limit()) : this.readArchive(pos, buf);
            if (totalRead == -1) {
                if (buf.array().length > 0) {
                    throw new IOException("Truncated TAR archive");
                }
                TarFile.this.setAtEOF(true);
            } else {
                this.entryOffset += (long)totalRead;
                buf.flip();
            }
            return totalRead;
        }

        private int readSparse(long pos, ByteBuffer buf, int numToRead) throws IOException {
            byte[] bufArray;
            List entrySparseInputStreams = (List)TarFile.this.sparseInputStreams.get(this.entry.getName());
            if (entrySparseInputStreams == null || entrySparseInputStreams.isEmpty()) {
                return this.readArchive(this.entry.getDataOffset() + pos, buf);
            }
            if (this.currentSparseInputStreamIndex >= entrySparseInputStreams.size()) {
                return -1;
            }
            InputStream currentInputStream = (InputStream)entrySparseInputStreams.get(this.currentSparseInputStreamIndex);
            int readLen = currentInputStream.read(bufArray = new byte[numToRead]);
            if (readLen != -1) {
                buf.put(bufArray, 0, readLen);
            }
            if (this.currentSparseInputStreamIndex == entrySparseInputStreams.size() - 1) {
                return readLen;
            }
            if (readLen == -1) {
                ++this.currentSparseInputStreamIndex;
                return this.readSparse(pos, buf, numToRead);
            }
            if (readLen < numToRead) {
                ++this.currentSparseInputStreamIndex;
                int readLenOfNext = this.readSparse(pos + (long)readLen, buf, numToRead - readLen);
                if (readLenOfNext == -1) {
                    return readLen;
                }
                return readLen + readLenOfNext;
            }
            return readLen;
        }

        private int readArchive(long pos, ByteBuffer buf) throws IOException {
            this.channel.position(pos);
            return this.channel.read(buf);
        }
    }
}

