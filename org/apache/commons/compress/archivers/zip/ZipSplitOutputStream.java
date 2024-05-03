/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.archivers.zip;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.FileNameUtils;

class ZipSplitOutputStream
extends OutputStream {
    private OutputStream outputStream;
    private File zipFile;
    private final long splitSize;
    private int currentSplitSegmentIndex;
    private long currentSplitSegmentBytesWritten;
    private boolean finished;
    private final byte[] singleByte = new byte[1];
    private static final long ZIP_SEGMENT_MIN_SIZE = 65536L;
    private static final long ZIP_SEGMENT_MAX_SIZE = 0xFFFFFFFFL;

    public ZipSplitOutputStream(File zipFile, long splitSize) throws IllegalArgumentException, IOException {
        if (splitSize < 65536L || splitSize > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("zip split segment size should between 64K and 4,294,967,295");
        }
        this.zipFile = zipFile;
        this.splitSize = splitSize;
        this.outputStream = Files.newOutputStream(zipFile.toPath(), new OpenOption[0]);
        this.writeZipSplitSignature();
    }

    public void prepareToWriteUnsplittableContent(long unsplittableContentSize) throws IllegalArgumentException, IOException {
        if (unsplittableContentSize > this.splitSize) {
            throw new IllegalArgumentException("The unsplittable content size is bigger than the split segment size");
        }
        long bytesRemainingInThisSegment = this.splitSize - this.currentSplitSegmentBytesWritten;
        if (bytesRemainingInThisSegment < unsplittableContentSize) {
            this.openNewSplitSegment();
        }
    }

    @Override
    public void write(int i) throws IOException {
        this.singleByte[0] = (byte)(i & 0xFF);
        this.write(this.singleByte);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len <= 0) {
            return;
        }
        if (this.currentSplitSegmentBytesWritten >= this.splitSize) {
            this.openNewSplitSegment();
            this.write(b, off, len);
        } else if (this.currentSplitSegmentBytesWritten + (long)len > this.splitSize) {
            int bytesToWriteForThisSegment = (int)this.splitSize - (int)this.currentSplitSegmentBytesWritten;
            this.write(b, off, bytesToWriteForThisSegment);
            this.openNewSplitSegment();
            this.write(b, off + bytesToWriteForThisSegment, len - bytesToWriteForThisSegment);
        } else {
            this.outputStream.write(b, off, len);
            this.currentSplitSegmentBytesWritten += (long)len;
        }
    }

    @Override
    public void close() throws IOException {
        if (!this.finished) {
            this.finish();
        }
    }

    private void finish() throws IOException {
        if (this.finished) {
            throw new IOException("This archive has already been finished");
        }
        String zipFileBaseName = FileNameUtils.getBaseName(this.zipFile.getName());
        File lastZipSplitSegmentFile = new File(this.zipFile.getParentFile(), zipFileBaseName + ".zip");
        this.outputStream.close();
        if (!this.zipFile.renameTo(lastZipSplitSegmentFile)) {
            throw new IOException("Failed to rename " + this.zipFile + " to " + lastZipSplitSegmentFile);
        }
        this.finished = true;
    }

    private void openNewSplitSegment() throws IOException {
        File newFile;
        if (this.currentSplitSegmentIndex == 0) {
            this.outputStream.close();
            newFile = this.createNewSplitSegmentFile(1);
            if (!this.zipFile.renameTo(newFile)) {
                throw new IOException("Failed to rename " + this.zipFile + " to " + newFile);
            }
        }
        newFile = this.createNewSplitSegmentFile(null);
        this.outputStream.close();
        this.outputStream = Files.newOutputStream(newFile.toPath(), new OpenOption[0]);
        this.currentSplitSegmentBytesWritten = 0L;
        this.zipFile = newFile;
        ++this.currentSplitSegmentIndex;
    }

    private void writeZipSplitSignature() throws IOException {
        this.outputStream.write(ZipArchiveOutputStream.DD_SIG);
        this.currentSplitSegmentBytesWritten += (long)ZipArchiveOutputStream.DD_SIG.length;
    }

    private File createNewSplitSegmentFile(Integer zipSplitSegmentSuffixIndex) throws IOException {
        int newZipSplitSegmentSuffixIndex = zipSplitSegmentSuffixIndex == null ? this.currentSplitSegmentIndex + 2 : zipSplitSegmentSuffixIndex;
        String baseName = FileNameUtils.getBaseName(this.zipFile.getName());
        String extension = ".z";
        extension = newZipSplitSegmentSuffixIndex <= 9 ? extension + "0" + newZipSplitSegmentSuffixIndex : extension + newZipSplitSegmentSuffixIndex;
        File newFile = new File(this.zipFile.getParent(), baseName + extension);
        if (newFile.exists()) {
            throw new IOException("split zip segment " + baseName + extension + " already exists");
        }
        return newFile;
    }

    public int getCurrentSplitSegmentIndex() {
        return this.currentSplitSegmentIndex;
    }

    public long getCurrentSplitSegmentBytesWritten() {
        return this.currentSplitSegmentBytesWritten;
    }
}

