/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.archivers.zip;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.compress.archivers.zip.ZipLong;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.compress.utils.MultiReadOnlySeekableByteChannel;

public class ZipSplitReadOnlySeekableByteChannel
extends MultiReadOnlySeekableByteChannel {
    private static final int ZIP_SPLIT_SIGNATURE_LENGTH = 4;
    private final ByteBuffer zipSplitSignatureByteBuffer = ByteBuffer.allocate(4);

    public ZipSplitReadOnlySeekableByteChannel(List<SeekableByteChannel> channels) throws IOException {
        super(channels);
        this.assertSplitSignature(channels);
    }

    private void assertSplitSignature(List<SeekableByteChannel> channels) throws IOException {
        SeekableByteChannel channel = channels.get(0);
        channel.position(0L);
        this.zipSplitSignatureByteBuffer.rewind();
        channel.read(this.zipSplitSignatureByteBuffer);
        ZipLong signature = new ZipLong(this.zipSplitSignatureByteBuffer.array());
        if (!signature.equals(ZipLong.DD_SIG)) {
            channel.position(0L);
            throw new IOException("The first zip split segment does not begin with split zip file signature");
        }
        channel.position(0L);
    }

    public static SeekableByteChannel forOrderedSeekableByteChannels(SeekableByteChannel ... channels) throws IOException {
        if (Objects.requireNonNull(channels, "channels must not be null").length == 1) {
            return channels[0];
        }
        return new ZipSplitReadOnlySeekableByteChannel(Arrays.asList(channels));
    }

    public static SeekableByteChannel forOrderedSeekableByteChannels(SeekableByteChannel lastSegmentChannel, Iterable<SeekableByteChannel> channels) throws IOException {
        Objects.requireNonNull(channels, "channels");
        Objects.requireNonNull(lastSegmentChannel, "lastSegmentChannel");
        ArrayList<SeekableByteChannel> channelsList = new ArrayList<SeekableByteChannel>();
        for (SeekableByteChannel channel : channels) {
            channelsList.add(channel);
        }
        channelsList.add(lastSegmentChannel);
        return ZipSplitReadOnlySeekableByteChannel.forOrderedSeekableByteChannels(channelsList.toArray(new SeekableByteChannel[0]));
    }

    public static SeekableByteChannel buildFromLastSplitSegment(File lastSegmentFile) throws IOException {
        String extension = FileNameUtils.getExtension(lastSegmentFile.getCanonicalPath());
        if (!extension.equalsIgnoreCase("zip")) {
            throw new IllegalArgumentException("The extension of last zip split segment should be .zip");
        }
        File parent = lastSegmentFile.getParentFile();
        String fileBaseName = FileNameUtils.getBaseName(lastSegmentFile.getCanonicalPath());
        ArrayList<File> splitZipSegments = new ArrayList<File>();
        Pattern pattern = Pattern.compile(Pattern.quote(fileBaseName) + ".[zZ][0-9]+");
        File[] children = parent.listFiles();
        if (children != null) {
            for (File file : children) {
                if (!pattern.matcher(file.getName()).matches()) continue;
                splitZipSegments.add(file);
            }
        }
        splitZipSegments.sort(new ZipSplitSegmentComparator());
        return ZipSplitReadOnlySeekableByteChannel.forFiles(lastSegmentFile, splitZipSegments);
    }

    public static SeekableByteChannel forFiles(File ... files) throws IOException {
        ArrayList<SeekableByteChannel> channels = new ArrayList<SeekableByteChannel>();
        for (File f : Objects.requireNonNull(files, "files must not be null")) {
            channels.add(Files.newByteChannel(f.toPath(), StandardOpenOption.READ));
        }
        if (channels.size() == 1) {
            return (SeekableByteChannel)channels.get(0);
        }
        return new ZipSplitReadOnlySeekableByteChannel(channels);
    }

    public static SeekableByteChannel forFiles(File lastSegmentFile, Iterable<File> files) throws IOException {
        Objects.requireNonNull(files, "files");
        Objects.requireNonNull(lastSegmentFile, "lastSegmentFile");
        ArrayList<File> filesList = new ArrayList<File>();
        for (File f : files) {
            filesList.add(f);
        }
        filesList.add(lastSegmentFile);
        return ZipSplitReadOnlySeekableByteChannel.forFiles(filesList.toArray(new File[0]));
    }

    private static class ZipSplitSegmentComparator
    implements Comparator<File>,
    Serializable {
        private static final long serialVersionUID = 20200123L;

        private ZipSplitSegmentComparator() {
        }

        @Override
        public int compare(File file1, File file2) {
            String extension1 = FileNameUtils.getExtension(file1.getPath());
            String extension2 = FileNameUtils.getExtension(file2.getPath());
            if (!extension1.startsWith("z")) {
                return -1;
            }
            if (!extension2.startsWith("z")) {
                return 1;
            }
            Integer splitSegmentNumber1 = Integer.parseInt(extension1.substring(1));
            Integer splitSegmentNumber2 = Integer.parseInt(extension2.substring(1));
            return splitSegmentNumber1.compareTo(splitSegmentNumber2);
        }
    }
}

