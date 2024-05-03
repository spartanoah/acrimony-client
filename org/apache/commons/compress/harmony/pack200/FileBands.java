/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.ClassReader
 */
package org.apache.commons.compress.harmony.pack200;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import org.apache.commons.compress.harmony.pack200.Archive;
import org.apache.commons.compress.harmony.pack200.BandSet;
import org.apache.commons.compress.harmony.pack200.CPUTF8;
import org.apache.commons.compress.harmony.pack200.Codec;
import org.apache.commons.compress.harmony.pack200.CpBands;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.pack200.PackingOptions;
import org.apache.commons.compress.harmony.pack200.PackingUtils;
import org.apache.commons.compress.harmony.pack200.SegmentHeader;
import org.objectweb.asm.ClassReader;

public class FileBands
extends BandSet {
    private final CPUTF8[] fileName;
    private int[] file_name;
    private final int[] file_modtime;
    private final long[] file_size;
    private final int[] file_options;
    private final byte[][] file_bits;
    private final List fileList;
    private final PackingOptions options;
    private final CpBands cpBands;

    public FileBands(CpBands cpBands, SegmentHeader segmentHeader, PackingOptions options, Archive.SegmentUnit segmentUnit, int effort) {
        super(effort, segmentHeader);
        this.fileList = segmentUnit.getFileList();
        this.options = options;
        this.cpBands = cpBands;
        int size = this.fileList.size();
        this.fileName = new CPUTF8[size];
        this.file_modtime = new int[size];
        this.file_size = new long[size];
        this.file_options = new int[size];
        int totalSize = 0;
        this.file_bits = new byte[size][];
        int archiveModtime = segmentHeader.getArchive_modtime();
        HashSet<String> classNames = new HashSet<String>();
        for (ClassReader reader : segmentUnit.getClassList()) {
            classNames.add(reader.getClassName());
        }
        CPUTF8 emptyString = cpBands.getCPUtf8("");
        int latestModtime = Integer.MIN_VALUE;
        boolean isLatest = !"keep".equals(options.getModificationTime());
        for (int i = 0; i < size; ++i) {
            Archive.PackingFile packingFile = (Archive.PackingFile)this.fileList.get(i);
            String name = packingFile.getName();
            if (name.endsWith(".class") && !options.isPassFile(name)) {
                int n = i;
                this.file_options[n] = this.file_options[n] | 2;
                this.fileName[i] = classNames.contains(name.substring(0, name.length() - 6)) ? emptyString : cpBands.getCPUtf8(name);
            } else {
                this.fileName[i] = cpBands.getCPUtf8(name);
            }
            if (options.isKeepDeflateHint() && packingFile.isDefalteHint()) {
                int n = i;
                this.file_options[n] = this.file_options[n] | 1;
            }
            byte[] bytes = packingFile.getContents();
            this.file_size[i] = bytes.length;
            totalSize = (int)((long)totalSize + this.file_size[i]);
            long modtime = (packingFile.getModtime() + (long)TimeZone.getDefault().getRawOffset()) / 1000L;
            this.file_modtime[i] = (int)(modtime - (long)archiveModtime);
            if (isLatest && latestModtime < this.file_modtime[i]) {
                latestModtime = this.file_modtime[i];
            }
            this.file_bits[i] = packingFile.getContents();
        }
        if (isLatest) {
            for (int index = 0; index < size; ++index) {
                this.file_modtime[index] = latestModtime;
            }
        }
    }

    public void finaliseBands() {
        this.file_name = new int[this.fileName.length];
        for (int i = 0; i < this.file_name.length; ++i) {
            Archive.PackingFile packingFile;
            String name;
            if (this.fileName[i].equals(this.cpBands.getCPUtf8("")) && this.options.isPassFile(name = (packingFile = (Archive.PackingFile)this.fileList.get(i)).getName())) {
                this.fileName[i] = this.cpBands.getCPUtf8(name);
                int n = i;
                this.file_options[n] = this.file_options[n] & 0xFFFFFFFD;
            }
            this.file_name[i] = this.fileName[i].getIndex();
        }
    }

    @Override
    public void pack(OutputStream out) throws IOException, Pack200Exception {
        PackingUtils.log("Writing file bands...");
        byte[] encodedBand = this.encodeBandInt("file_name", this.file_name, Codec.UNSIGNED5);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from file_name[" + this.file_name.length + "]");
        encodedBand = this.encodeFlags("file_size", this.file_size, Codec.UNSIGNED5, Codec.UNSIGNED5, this.segmentHeader.have_file_size_hi());
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from file_size[" + this.file_size.length + "]");
        if (this.segmentHeader.have_file_modtime()) {
            encodedBand = this.encodeBandInt("file_modtime", this.file_modtime, Codec.DELTA5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from file_modtime[" + this.file_modtime.length + "]");
        }
        if (this.segmentHeader.have_file_options()) {
            encodedBand = this.encodeBandInt("file_options", this.file_options, Codec.UNSIGNED5);
            out.write(encodedBand);
            PackingUtils.log("Wrote " + encodedBand.length + " bytes from file_options[" + this.file_options.length + "]");
        }
        encodedBand = this.encodeBandInt("file_bits", this.flatten(this.file_bits), Codec.BYTE1);
        out.write(encodedBand);
        PackingUtils.log("Wrote " + encodedBand.length + " bytes from file_bits[" + this.file_bits.length + "]");
    }

    private int[] flatten(byte[][] bytes) {
        int total = 0;
        for (int i = 0; i < bytes.length; ++i) {
            total += bytes[i].length;
        }
        int[] band = new int[total];
        int index = 0;
        for (int i = 0; i < bytes.length; ++i) {
            for (int j = 0; j < bytes[i].length; ++j) {
                band[index++] = bytes[i][j] & 0xFF;
            }
        }
        return band;
    }
}

