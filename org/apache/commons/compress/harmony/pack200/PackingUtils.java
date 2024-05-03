/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.commons.compress.harmony.pack200;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.commons.compress.harmony.pack200.Archive;
import org.apache.commons.compress.harmony.pack200.PackingOptions;

public class PackingUtils {
    private static PackingLogger packingLogger = new PackingLogger("org.harmony.apache.pack200", null);

    public static void config(PackingOptions options) throws IOException {
        String logFileName = options.getLogFile();
        if (logFileName != null) {
            FileHandler fileHandler = new FileHandler(logFileName, false);
            fileHandler.setFormatter(new SimpleFormatter());
            packingLogger.addHandler(fileHandler);
            packingLogger.setUseParentHandlers(false);
        }
        packingLogger.setVerbose(options.isVerbose());
    }

    public static void log(String message) {
        packingLogger.log(Level.INFO, message);
    }

    public static void copyThroughJar(JarInputStream jarInputStream, OutputStream outputStream) throws IOException {
        JarEntry jarEntry;
        Manifest manifest = jarInputStream.getManifest();
        JarOutputStream jarOutputStream = new JarOutputStream(outputStream, manifest);
        jarOutputStream.setComment("PACK200");
        PackingUtils.log("Packed META-INF/MANIFEST.MF");
        byte[] bytes = new byte[16384];
        while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
            int bytesRead;
            jarOutputStream.putNextEntry(jarEntry);
            while ((bytesRead = jarInputStream.read(bytes)) != -1) {
                jarOutputStream.write(bytes, 0, bytesRead);
            }
            PackingUtils.log("Packed " + jarEntry.getName());
        }
        jarInputStream.close();
        jarOutputStream.close();
    }

    public static void copyThroughJar(JarFile jarFile, OutputStream outputStream) throws IOException {
        JarOutputStream jarOutputStream = new JarOutputStream(outputStream);
        jarOutputStream.setComment("PACK200");
        byte[] bytes = new byte[16384];
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            int bytesRead;
            JarEntry jarEntry = entries.nextElement();
            jarOutputStream.putNextEntry(jarEntry);
            InputStream inputStream = jarFile.getInputStream(jarEntry);
            while ((bytesRead = inputStream.read(bytes)) != -1) {
                jarOutputStream.write(bytes, 0, bytesRead);
            }
            jarOutputStream.closeEntry();
            PackingUtils.log("Packed " + jarEntry.getName());
        }
        jarFile.close();
        jarOutputStream.close();
    }

    public static List getPackingFileListFromJar(JarInputStream jarInputStream, boolean keepFileOrder) throws IOException {
        JarEntry jarEntry;
        ArrayList<Archive.PackingFile> packingFileList = new ArrayList<Archive.PackingFile>();
        Manifest manifest = jarInputStream.getManifest();
        if (manifest != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            manifest.write(baos);
            packingFileList.add(new Archive.PackingFile("META-INF/MANIFEST.MF", baos.toByteArray(), 0L));
        }
        while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
            byte[] bytes = PackingUtils.readJarEntry(jarEntry, new BufferedInputStream(jarInputStream));
            packingFileList.add(new Archive.PackingFile(bytes, jarEntry));
        }
        if (!keepFileOrder) {
            PackingUtils.reorderPackingFiles(packingFileList);
        }
        return packingFileList;
    }

    public static List getPackingFileListFromJar(JarFile jarFile, boolean keepFileOrder) throws IOException {
        ArrayList<Archive.PackingFile> packingFileList = new ArrayList<Archive.PackingFile>();
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            byte[] bytes = PackingUtils.readJarEntry(jarEntry, new BufferedInputStream(jarFile.getInputStream(jarEntry)));
            packingFileList.add(new Archive.PackingFile(bytes, jarEntry));
        }
        if (!keepFileOrder) {
            PackingUtils.reorderPackingFiles(packingFileList);
        }
        return packingFileList;
    }

    private static byte[] readJarEntry(JarEntry jarEntry, InputStream inputStream) throws IOException {
        byte[] bytes;
        long size = jarEntry.getSize();
        if (size > Integer.MAX_VALUE) {
            throw new RuntimeException("Large Class!");
        }
        if (size < 0L) {
            size = 0L;
        }
        if ((long)inputStream.read(bytes = new byte[(int)size]) != size) {
            throw new RuntimeException("Error reading from stream");
        }
        return bytes;
    }

    private static void reorderPackingFiles(List packingFileList) {
        Iterator iterator = packingFileList.iterator();
        while (iterator.hasNext()) {
            Archive.PackingFile packingFile = (Archive.PackingFile)iterator.next();
            if (!packingFile.isDirectory()) continue;
            iterator.remove();
        }
        Collections.sort(packingFileList, (arg0, arg1) -> {
            if (arg0 instanceof Archive.PackingFile && arg1 instanceof Archive.PackingFile) {
                String fileName1;
                String fileName0 = ((Archive.PackingFile)arg0).getName();
                if (fileName0.equals(fileName1 = ((Archive.PackingFile)arg1).getName())) {
                    return 0;
                }
                if ("META-INF/MANIFEST.MF".equals(fileName0)) {
                    return -1;
                }
                if ("META-INF/MANIFEST.MF".equals(fileName1)) {
                    return 1;
                }
                return fileName0.compareTo(fileName1);
            }
            throw new IllegalArgumentException();
        });
    }

    static {
        LogManager.getLogManager().addLogger(packingLogger);
    }

    private static class PackingLogger
    extends Logger {
        private boolean verbose = false;

        protected PackingLogger(String name, String resourceBundleName) {
            super(name, resourceBundleName);
        }

        @Override
        public void log(LogRecord logRecord) {
            if (this.verbose) {
                super.log(logRecord);
            }
        }

        public void setVerbose(boolean isVerbose) {
            this.verbose = isVerbose;
        }
    }
}

