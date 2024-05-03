/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.archivers.examples;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.Objects;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.examples.CloseableConsumer;
import org.apache.commons.compress.archivers.examples.CloseableConsumerAdapter;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

public class Archiver {
    public static final EnumSet<FileVisitOption> EMPTY_FileVisitOption = EnumSet.noneOf(FileVisitOption.class);

    public void create(ArchiveOutputStream target, File directory) throws IOException, ArchiveException {
        this.create(target, directory.toPath(), EMPTY_FileVisitOption, new LinkOption[0]);
    }

    public void create(ArchiveOutputStream target, Path directory, EnumSet<FileVisitOption> fileVisitOptions, LinkOption ... linkOptions) throws IOException {
        Files.walkFileTree(directory, fileVisitOptions, Integer.MAX_VALUE, new ArchiverFileVisitor(target, directory, linkOptions));
        target.finish();
    }

    public void create(ArchiveOutputStream target, Path directory) throws IOException {
        this.create(target, directory, EMPTY_FileVisitOption, new LinkOption[0]);
    }

    public void create(SevenZOutputFile target, File directory) throws IOException {
        this.create(target, directory.toPath());
    }

    public void create(final SevenZOutputFile target, final Path directory) throws IOException {
        Files.walkFileTree(directory, new ArchiverFileVisitor(null, directory, new LinkOption[0]){

            @Override
            protected FileVisitResult visit(Path path, BasicFileAttributes attrs, boolean isFile) throws IOException {
                Objects.requireNonNull(path);
                Objects.requireNonNull(attrs);
                String name = directory.relativize(path).toString().replace('\\', '/');
                if (!name.isEmpty()) {
                    SevenZArchiveEntry archiveEntry = target.createArchiveEntry(path, isFile || name.endsWith("/") ? name : name + "/", new LinkOption[0]);
                    target.putArchiveEntry(archiveEntry);
                    if (isFile) {
                        target.write(path, new OpenOption[0]);
                    }
                    target.closeArchiveEntry();
                }
                return FileVisitResult.CONTINUE;
            }
        });
        target.finish();
    }

    public void create(String format, File target, File directory) throws IOException, ArchiveException {
        this.create(format, target.toPath(), directory.toPath());
    }

    @Deprecated
    public void create(String format, OutputStream target, File directory) throws IOException, ArchiveException {
        this.create(format, target, directory, CloseableConsumer.NULL_CONSUMER);
    }

    public void create(String format, OutputStream target, File directory, CloseableConsumer closeableConsumer) throws IOException, ArchiveException {
        try (CloseableConsumerAdapter c = new CloseableConsumerAdapter(closeableConsumer);){
            this.create(c.track(ArchiveStreamFactory.DEFAULT.createArchiveOutputStream(format, target)), directory);
        }
    }

    public void create(String format, Path target, Path directory) throws IOException, ArchiveException {
        if (this.prefersSeekableByteChannel(format)) {
            try (FileChannel channel = FileChannel.open(target, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);){
                this.create(format, (SeekableByteChannel)channel, directory);
                return;
            }
        }
        try (ArchiveOutputStream outputStream = ArchiveStreamFactory.DEFAULT.createArchiveOutputStream(format, Files.newOutputStream(target, new OpenOption[0]));){
            this.create(outputStream, directory, EMPTY_FileVisitOption, new LinkOption[0]);
        }
    }

    @Deprecated
    public void create(String format, SeekableByteChannel target, File directory) throws IOException, ArchiveException {
        this.create(format, target, directory, CloseableConsumer.NULL_CONSUMER);
    }

    public void create(String format, SeekableByteChannel target, File directory, CloseableConsumer closeableConsumer) throws IOException, ArchiveException {
        block15: {
            try (CloseableConsumerAdapter c = new CloseableConsumerAdapter(closeableConsumer);){
                if (!this.prefersSeekableByteChannel(format)) {
                    this.create(format, c.track(Channels.newOutputStream(target)), directory);
                    break block15;
                }
                if ("zip".equalsIgnoreCase(format)) {
                    this.create((ArchiveOutputStream)c.track(new ZipArchiveOutputStream(target)), directory);
                    break block15;
                }
                if ("7z".equalsIgnoreCase(format)) {
                    this.create(c.track(new SevenZOutputFile(target)), directory);
                    break block15;
                }
                throw new ArchiveException("Don't know how to handle format " + format);
            }
        }
    }

    public void create(String format, SeekableByteChannel target, Path directory) throws IOException {
        if ("7z".equalsIgnoreCase(format)) {
            try (SevenZOutputFile sevenZFile = new SevenZOutputFile(target);){
                this.create(sevenZFile, directory);
            }
        } else if ("zip".equalsIgnoreCase(format)) {
            try (ZipArchiveOutputStream archiveOutputStream = new ZipArchiveOutputStream(target);){
                this.create(archiveOutputStream, directory, EMPTY_FileVisitOption, new LinkOption[0]);
            }
        } else {
            throw new IllegalStateException(format);
        }
    }

    private boolean prefersSeekableByteChannel(String format) {
        return "zip".equalsIgnoreCase(format) || "7z".equalsIgnoreCase(format);
    }

    private static class ArchiverFileVisitor
    extends SimpleFileVisitor<Path> {
        private final ArchiveOutputStream target;
        private final Path directory;
        private final LinkOption[] linkOptions;

        private ArchiverFileVisitor(ArchiveOutputStream target, Path directory, LinkOption ... linkOptions) {
            this.target = target;
            this.directory = directory;
            this.linkOptions = linkOptions == null ? IOUtils.EMPTY_LINK_OPTIONS : (LinkOption[])linkOptions.clone();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return this.visit(dir, attrs, false);
        }

        protected FileVisitResult visit(Path path, BasicFileAttributes attrs, boolean isFile) throws IOException {
            Objects.requireNonNull(path);
            Objects.requireNonNull(attrs);
            String name = this.directory.relativize(path).toString().replace('\\', '/');
            if (!name.isEmpty()) {
                ArchiveEntry archiveEntry = this.target.createArchiveEntry(path, isFile || name.endsWith("/") ? name : name + "/", this.linkOptions);
                this.target.putArchiveEntry(archiveEntry);
                if (isFile) {
                    Files.copy(path, (OutputStream)this.target);
                }
                this.target.closeArchiveEntry();
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            return this.visit(file, attrs, true);
        }
    }
}

