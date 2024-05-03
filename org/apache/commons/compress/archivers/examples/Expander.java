/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.archivers.examples;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.Iterator;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.examples.CloseableConsumer;
import org.apache.commons.compress.archivers.examples.CloseableConsumerAdapter;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

public class Expander {
    public void expand(File archive, File targetDirectory) throws IOException, ArchiveException {
        String format = null;
        try (BufferedInputStream i = new BufferedInputStream(Files.newInputStream(archive.toPath(), new OpenOption[0]));){
            format = ArchiveStreamFactory.detect(i);
        }
        this.expand(format, archive, targetDirectory);
    }

    public void expand(String format, File archive, File targetDirectory) throws IOException, ArchiveException {
        if (this.prefersSeekableByteChannel(format)) {
            try (FileChannel c = FileChannel.open(archive.toPath(), StandardOpenOption.READ);){
                this.expand(format, c, targetDirectory, CloseableConsumer.CLOSING_CONSUMER);
            }
            return;
        }
        try (BufferedInputStream i = new BufferedInputStream(Files.newInputStream(archive.toPath(), new OpenOption[0]));){
            this.expand(format, i, targetDirectory, CloseableConsumer.CLOSING_CONSUMER);
        }
    }

    @Deprecated
    public void expand(InputStream archive, File targetDirectory) throws IOException, ArchiveException {
        this.expand(archive, targetDirectory, CloseableConsumer.NULL_CONSUMER);
    }

    public void expand(InputStream archive, File targetDirectory, CloseableConsumer closeableConsumer) throws IOException, ArchiveException {
        try (CloseableConsumerAdapter c = new CloseableConsumerAdapter(closeableConsumer);){
            this.expand(c.track(ArchiveStreamFactory.DEFAULT.createArchiveInputStream(archive)), targetDirectory);
        }
    }

    @Deprecated
    public void expand(String format, InputStream archive, File targetDirectory) throws IOException, ArchiveException {
        this.expand(format, archive, targetDirectory, CloseableConsumer.NULL_CONSUMER);
    }

    public void expand(String format, InputStream archive, File targetDirectory, CloseableConsumer closeableConsumer) throws IOException, ArchiveException {
        try (CloseableConsumerAdapter c = new CloseableConsumerAdapter(closeableConsumer);){
            this.expand(c.track(ArchiveStreamFactory.DEFAULT.createArchiveInputStream(format, archive)), targetDirectory);
        }
    }

    @Deprecated
    public void expand(String format, SeekableByteChannel archive, File targetDirectory) throws IOException, ArchiveException {
        this.expand(format, archive, targetDirectory, CloseableConsumer.NULL_CONSUMER);
    }

    public void expand(String format, SeekableByteChannel archive, File targetDirectory, CloseableConsumer closeableConsumer) throws IOException, ArchiveException {
        block16: {
            try (CloseableConsumerAdapter c = new CloseableConsumerAdapter(closeableConsumer);){
                if (!this.prefersSeekableByteChannel(format)) {
                    this.expand(format, c.track(Channels.newInputStream(archive)), targetDirectory);
                    break block16;
                }
                if ("tar".equalsIgnoreCase(format)) {
                    this.expand(c.track(new TarFile(archive)), targetDirectory);
                    break block16;
                }
                if ("zip".equalsIgnoreCase(format)) {
                    this.expand(c.track(new ZipFile(archive)), targetDirectory);
                    break block16;
                }
                if ("7z".equalsIgnoreCase(format)) {
                    this.expand(c.track(new SevenZFile(archive)), targetDirectory);
                    break block16;
                }
                throw new ArchiveException("Don't know how to handle format " + format);
            }
        }
    }

    public void expand(ArchiveInputStream archive, File targetDirectory) throws IOException, ArchiveException {
        this.expand(() -> {
            ArchiveEntry next = archive.getNextEntry();
            while (next != null && !archive.canReadEntryData(next)) {
                next = archive.getNextEntry();
            }
            return next;
        }, (ArchiveEntry entry, OutputStream out) -> IOUtils.copy(archive, out), targetDirectory);
    }

    public void expand(TarFile archive, File targetDirectory) throws IOException, ArchiveException {
        Iterator<TarArchiveEntry> entryIterator = archive.getEntries().iterator();
        this.expand(() -> entryIterator.hasNext() ? (ArchiveEntry)entryIterator.next() : null, (ArchiveEntry entry, OutputStream out) -> {
            try (InputStream in = archive.getInputStream((TarArchiveEntry)entry);){
                IOUtils.copy(in, out);
            }
        }, targetDirectory);
    }

    public void expand(ZipFile archive, File targetDirectory) throws IOException, ArchiveException {
        Enumeration<ZipArchiveEntry> entries = archive.getEntries();
        this.expand(() -> {
            ZipArchiveEntry next;
            ZipArchiveEntry zipArchiveEntry = next = entries.hasMoreElements() ? (ZipArchiveEntry)entries.nextElement() : null;
            while (next != null && !archive.canReadEntryData(next)) {
                next = entries.hasMoreElements() ? (ZipArchiveEntry)entries.nextElement() : null;
            }
            return next;
        }, (ArchiveEntry entry, OutputStream out) -> {
            try (InputStream in = archive.getInputStream((ZipArchiveEntry)entry);){
                IOUtils.copy(in, out);
            }
        }, targetDirectory);
    }

    public void expand(SevenZFile archive, File targetDirectory) throws IOException, ArchiveException {
        this.expand(archive::getNextEntry, (ArchiveEntry entry, OutputStream out) -> {
            int n;
            byte[] buffer = new byte[8192];
            while (-1 != (n = archive.read(buffer))) {
                out.write(buffer, 0, n);
            }
        }, targetDirectory);
    }

    private boolean prefersSeekableByteChannel(String format) {
        return "tar".equalsIgnoreCase(format) || "zip".equalsIgnoreCase(format) || "7z".equalsIgnoreCase(format);
    }

    private void expand(ArchiveEntrySupplier supplier, EntryWriter writer, File targetDirectory) throws IOException {
        String targetDirPath = targetDirectory.getCanonicalPath();
        if (!targetDirPath.endsWith(File.separator)) {
            targetDirPath = targetDirPath + File.separator;
        }
        ArchiveEntry nextEntry = supplier.getNextReadableEntry();
        while (nextEntry != null) {
            File f = new File(targetDirectory, nextEntry.getName());
            if (!f.getCanonicalPath().startsWith(targetDirPath)) {
                throw new IOException("Expanding " + nextEntry.getName() + " would create file outside of " + targetDirectory);
            }
            if (nextEntry.isDirectory()) {
                if (!f.isDirectory() && !f.mkdirs()) {
                    throw new IOException("Failed to create directory " + f);
                }
            } else {
                File parent = f.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }
                try (OutputStream o = Files.newOutputStream(f.toPath(), new OpenOption[0]);){
                    writer.writeEntryDataTo(nextEntry, o);
                }
            }
            nextEntry = supplier.getNextReadableEntry();
        }
    }

    private static interface EntryWriter {
        public void writeEntryDataTo(ArchiveEntry var1, OutputStream var2) throws IOException;
    }

    private static interface ArchiveEntrySupplier {
        public ArchiveEntry getNextReadableEntry() throws IOException;
    }
}

