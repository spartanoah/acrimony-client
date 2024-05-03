/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.unpack200;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.harmony.pack200.Pack200Exception;
import org.apache.commons.compress.harmony.unpack200.Segment;

public class Archive {
    private InputStream inputStream;
    private final JarOutputStream outputStream;
    private boolean removePackFile;
    private int logLevel = 1;
    private FileOutputStream logFile;
    private boolean overrideDeflateHint;
    private boolean deflateHint;
    private String inputFileName;
    private String outputFileName;

    public Archive(String inputFile, String outputFile) throws FileNotFoundException, IOException {
        this.inputFileName = inputFile;
        this.outputFileName = outputFile;
        this.inputStream = new FileInputStream(inputFile);
        this.outputStream = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
    }

    public Archive(InputStream inputStream, JarOutputStream outputStream) throws IOException {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void unpack() throws Pack200Exception, IOException {
        this.outputStream.setComment("PACK200");
        try {
            if (!this.inputStream.markSupported()) {
                this.inputStream = new BufferedInputStream(this.inputStream);
                if (!this.inputStream.markSupported()) {
                    throw new IllegalStateException();
                }
            }
            this.inputStream.mark(2);
            if ((this.inputStream.read() & 0xFF | (this.inputStream.read() & 0xFF) << 8) == 35615) {
                this.inputStream.reset();
                this.inputStream = new BufferedInputStream(new GZIPInputStream(this.inputStream));
            } else {
                this.inputStream.reset();
            }
            this.inputStream.mark(4);
            int[] magic = new int[]{202, 254, 208, 13};
            int[] word = new int[4];
            for (int i = 0; i < word.length; ++i) {
                word[i] = this.inputStream.read();
            }
            boolean compressedWithE0 = false;
            for (int m = 0; m < magic.length; ++m) {
                if (word[m] == magic[m]) continue;
                compressedWithE0 = true;
            }
            this.inputStream.reset();
            if (compressedWithE0) {
                JarEntry jarEntry;
                JarInputStream jarInputStream = new JarInputStream(this.inputStream);
                while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                    this.outputStream.putNextEntry(jarEntry);
                    byte[] bytes = new byte[16384];
                    int bytesRead = jarInputStream.read(bytes);
                    while (bytesRead != -1) {
                        this.outputStream.write(bytes, 0, bytesRead);
                        bytesRead = jarInputStream.read(bytes);
                    }
                    this.outputStream.closeEntry();
                }
            } else {
                int i = 0;
                while (this.available(this.inputStream)) {
                    Segment segment = new Segment();
                    segment.setLogLevel(this.logLevel);
                    segment.setLogStream(this.logFile != null ? this.logFile : System.out);
                    segment.setPreRead(false);
                    if (++i == 1) {
                        segment.log(2, "Unpacking from " + this.inputFileName + " to " + this.outputFileName);
                    }
                    segment.log(2, "Reading segment " + i);
                    if (this.overrideDeflateHint) {
                        segment.overrideDeflateHint(this.deflateHint);
                    }
                    segment.unpack(this.inputStream, this.outputStream);
                    this.outputStream.flush();
                    if (!(this.inputStream instanceof FileInputStream)) continue;
                    this.inputFileName = ((FileInputStream)this.inputStream).getFD().toString();
                }
            }
        } finally {
            try {
                this.inputStream.close();
            } catch (Exception magic) {}
            try {
                this.outputStream.close();
            } catch (Exception magic) {}
            if (this.logFile != null) {
                try {
                    this.logFile.close();
                } catch (Exception magic) {}
            }
        }
        if (this.removePackFile) {
            boolean deleted = false;
            if (this.inputFileName != null) {
                File file = new File(this.inputFileName);
                deleted = file.delete();
            }
            if (!deleted) {
                throw new Pack200Exception("Failed to delete the input file.");
            }
        }
    }

    private boolean available(InputStream inputStream) throws IOException {
        inputStream.mark(1);
        int check = inputStream.read();
        inputStream.reset();
        return check != -1;
    }

    public void setRemovePackFile(boolean removePackFile) {
        this.removePackFile = removePackFile;
    }

    public void setVerbose(boolean verbose) {
        if (verbose) {
            this.logLevel = 2;
        } else if (this.logLevel == 2) {
            this.logLevel = 1;
        }
    }

    public void setQuiet(boolean quiet) {
        if (quiet) {
            this.logLevel = 0;
        } else if (this.logLevel == 0) {
            this.logLevel = 0;
        }
    }

    public void setLogFile(String logFileName) throws FileNotFoundException {
        this.logFile = new FileOutputStream(logFileName);
    }

    public void setLogFile(String logFileName, boolean append) throws FileNotFoundException {
        this.logFile = new FileOutputStream(logFileName, append);
    }

    public void setDeflateHint(boolean deflateHint) {
        this.overrideDeflateHint = true;
        this.deflateHint = deflateHint;
    }
}

