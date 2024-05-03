/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.harmony.pack200.Pack200ClassReader;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.pack200.PackingOptions;
import org.apache.commons.compress.harmony.pack200.PackingUtils;
import org.apache.commons.compress.harmony.pack200.Segment;

public class Archive {
    private final JarInputStream jarInputStream;
    private final OutputStream outputStream;
    private JarFile jarFile;
    private long currentSegmentSize;
    private final PackingOptions options;

    public Archive(JarInputStream inputStream, OutputStream outputStream, PackingOptions options) throws IOException {
        this.jarInputStream = inputStream;
        if (options == null) {
            options = new PackingOptions();
        }
        this.options = options;
        if (options.isGzip()) {
            outputStream = new GZIPOutputStream(outputStream);
        }
        this.outputStream = new BufferedOutputStream(outputStream);
        PackingUtils.config(options);
    }

    public Archive(JarFile jarFile, OutputStream outputStream, PackingOptions options) throws IOException {
        if (options == null) {
            options = new PackingOptions();
        }
        this.options = options;
        if (options.isGzip()) {
            outputStream = new GZIPOutputStream(outputStream);
        }
        this.outputStream = new BufferedOutputStream(outputStream);
        this.jarFile = jarFile;
        this.jarInputStream = null;
        PackingUtils.config(options);
    }

    public void pack() throws Pack200Exception, IOException {
        if (0 == this.options.getEffort()) {
            this.doZeroEffortPack();
        } else {
            this.doNormalPack();
        }
    }

    private void doZeroEffortPack() throws IOException, Pack200Exception {
        PackingUtils.log("Start to perform a zero-effort packing");
        if (this.jarInputStream != null) {
            PackingUtils.copyThroughJar(this.jarInputStream, this.outputStream);
        } else {
            PackingUtils.copyThroughJar(this.jarFile, this.outputStream);
        }
    }

    private void doNormalPack() throws IOException, Pack200Exception {
        PackingUtils.log("Start to perform a normal packing");
        List packingFileList = this.jarInputStream != null ? PackingUtils.getPackingFileListFromJar(this.jarInputStream, this.options.isKeepFileOrder()) : PackingUtils.getPackingFileListFromJar(this.jarFile, this.options.isKeepFileOrder());
        List segmentUnitList = this.splitIntoSegments(packingFileList);
        int previousByteAmount = 0;
        int packedByteAmount = 0;
        int segmentSize = segmentUnitList.size();
        for (int index = 0; index < segmentSize; ++index) {
            SegmentUnit segmentUnit = (SegmentUnit)segmentUnitList.get(index);
            new Segment().pack(segmentUnit, this.outputStream, this.options);
            previousByteAmount += segmentUnit.getByteAmount();
            packedByteAmount += segmentUnit.getPackedByteAmount();
        }
        PackingUtils.log("Total: Packed " + previousByteAmount + " input bytes of " + packingFileList.size() + " files into " + packedByteAmount + " bytes in " + segmentSize + " segments");
        this.outputStream.close();
    }

    private List splitIntoSegments(List packingFileList) throws IOException, Pack200Exception {
        ArrayList<SegmentUnit> segmentUnitList = new ArrayList<SegmentUnit>();
        ArrayList classes = new ArrayList();
        ArrayList files = new ArrayList();
        long segmentLimit = this.options.getSegmentLimit();
        int size = packingFileList.size();
        for (int index = 0; index < size; ++index) {
            PackingFile packingFile = (PackingFile)packingFileList.get(index);
            if (!this.addJarEntry(packingFile, classes, files)) {
                segmentUnitList.add(new SegmentUnit(classes, files));
                classes = new ArrayList();
                files = new ArrayList();
                this.currentSegmentSize = 0L;
                this.addJarEntry(packingFile, classes, files);
                this.currentSegmentSize = 0L;
                continue;
            }
            if (segmentLimit != 0L || this.estimateSize(packingFile) <= 0L) continue;
            segmentUnitList.add(new SegmentUnit(classes, files));
            classes = new ArrayList();
            files = new ArrayList();
        }
        if (classes.size() > 0 || files.size() > 0) {
            segmentUnitList.add(new SegmentUnit(classes, files));
        }
        return segmentUnitList;
    }

    private boolean addJarEntry(PackingFile packingFile, List javaClasses, List files) throws IOException, Pack200Exception {
        String name;
        long segmentLimit = this.options.getSegmentLimit();
        if (segmentLimit != -1L && segmentLimit != 0L) {
            long packedSize = this.estimateSize(packingFile);
            if (packedSize + this.currentSegmentSize > segmentLimit && this.currentSegmentSize > 0L) {
                return false;
            }
            this.currentSegmentSize += packedSize;
        }
        if ((name = packingFile.getName()).endsWith(".class") && !this.options.isPassFile(name)) {
            Pack200ClassReader classParser = new Pack200ClassReader(packingFile.contents);
            classParser.setFileName(name);
            javaClasses.add(classParser);
            PackingFile.access$002(packingFile, new byte[0]);
        }
        files.add(packingFile);
        return true;
    }

    private long estimateSize(PackingFile packingFile) {
        String name = packingFile.getName();
        if (name.startsWith("META-INF") || name.startsWith("/META-INF")) {
            return 0L;
        }
        long fileSize = packingFile.contents.length;
        if (fileSize < 0L) {
            fileSize = 0L;
        }
        return (long)name.length() + fileSize + 5L;
    }

    static class PackingFile {
        private final String name;
        private byte[] contents;
        private final long modtime;
        private final boolean deflateHint;
        private final boolean isDirectory;

        public PackingFile(String name, byte[] contents, long modtime) {
            this.name = name;
            this.contents = contents;
            this.modtime = modtime;
            this.deflateHint = false;
            this.isDirectory = false;
        }

        public PackingFile(byte[] bytes, JarEntry jarEntry) {
            this.name = jarEntry.getName();
            this.contents = bytes;
            this.modtime = jarEntry.getTime();
            this.deflateHint = jarEntry.getMethod() == 8;
            this.isDirectory = jarEntry.isDirectory();
        }

        public byte[] getContents() {
            return this.contents;
        }

        public String getName() {
            return this.name;
        }

        public long getModtime() {
            return this.modtime;
        }

        public void setContents(byte[] contents) {
            this.contents = contents;
        }

        public boolean isDefalteHint() {
            return this.deflateHint;
        }

        public boolean isDirectory() {
            return this.isDirectory;
        }

        public String toString() {
            return this.name;
        }

        static /* synthetic */ byte[] access$002(PackingFile x0, byte[] x1) {
            x0.contents = x1;
            return x1;
        }
    }

    static class SegmentUnit {
        private final List classList;
        private final List fileList;
        private int byteAmount = 0;
        private int packedByteAmount = 0;

        public SegmentUnit(List classes, List files) {
            this.classList = classes;
            this.fileList = files;
            for (Pack200ClassReader classReader : this.classList) {
                this.byteAmount += classReader.b.length;
            }
            for (PackingFile file : this.fileList) {
                this.byteAmount += file.contents.length;
            }
        }

        public List getClassList() {
            return this.classList;
        }

        public int classListSize() {
            return this.classList.size();
        }

        public int fileListSize() {
            return this.fileList.size();
        }

        public List getFileList() {
            return this.fileList;
        }

        public int getByteAmount() {
            return this.byteAmount;
        }

        public int getPackedByteAmount() {
            return this.packedByteAmount;
        }

        public void addPackedByteAmount(int amount) {
            this.packedByteAmount += amount;
        }
    }
}

